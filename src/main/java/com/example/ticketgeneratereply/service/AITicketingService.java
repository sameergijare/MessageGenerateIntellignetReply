package com.example.ticketgeneratereply.service;


import com.example.ticketgeneratereply.*;
import com.example.ticketgeneratereply.db.KnowledgeEmbeddingRepository;
import com.example.ticketgeneratereply.db.Message;
import com.example.ticketgeneratereply.db.Ticket;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;


@Service
public class AITicketingService {

    private final KnowledgeEmbeddingRepository embeddingRepository;
    private final EmbeddingModel embeddingModel;
    private final StreamingChatLanguageModel streamingModel;
    private static final Logger log = LoggerFactory.getLogger(AITicketingService.class);
    @Autowired
    private LocalStructuralAnalyzer localAnalyzer;

    interface StructuralAnalyzer {
        @SystemMessage("You are an advanced ticket routing engine. Categorize and analyze the customer request strictly.")
        @UserMessage("Analyze this customer message: {{text}}")
        TicketAnalysisResult extractMetadata(@V("text") String text);
    }

    interface ResponseDrafter {
        @SystemMessage("""
            You are an elite, professional customer support expert representing a business.
            Draft a polite, comprehensive response to the customer based *only* on the provided context documentation.
            If the context does not explicitly contain the resolution steps, draft a message stating that a senior human agent is manually reviewing their file.
            Do not mention that you are an AI or make up policies.
            
            Context Documentation:
            {{contextText}}
            """)
        @UserMessage("Customer Message: {{customerMessage}}")
        String draftResolution(@V("contextText") String contextText, @V("customerMessage") String customerMessage);
    }

    public AITicketingService(KnowledgeEmbeddingRepository embeddingRepository,
                              EmbeddingModel embeddingModel, StreamingChatLanguageModel streamingModel) {
        this.embeddingRepository = embeddingRepository;
        this.embeddingModel = embeddingModel;
        this.streamingModel = streamingModel;
    }

    @Retryable(
            includes = { RuntimeException.class },
            maxRetries = 2,
            delay = 1000,
            multiplier = 2.0
    )
    public String processIncomingTicket(Ticket ticket, Message incomingMessage) {
        try {
            // Phase 1: Local Structural Analysis (Offline, 0ms latency)
            TicketAnalysisResult analysis = localAnalyzer.extractMetadataLocally(incomingMessage.getBody(), TenantContext.getCurrentTenantId());

            ticket.setPriority(Ticket.TicketPriority.valueOf(analysis.inferredPriority().toString()));
            ticket.setAiClassification(analysis.categoryTag());
            ticket.setAiConfidenceScore(analysis.confidenceScore());

            //System.out.println("Structured analysis successfully completed: " + analysis);

            // Phase 2: RAG Pipeline - Defensive Vector Similarity Search
            float[] queryVector = embeddingModel.embed(incomingMessage.getBody()).content().vector();

            List<String> contextChunks = new java.util.ArrayList<>();
            try {
                // Execute using the Object[] raw query format
                List<Object[]> rows = embeddingRepository.findNearestMatchesRaw(
                        TenantContext.getCurrentTenantId(),
                        queryVector,
                        3
                );

                if (rows != null) {
                    for (Object[] row : rows) {
                        // row[3] corresponds exactly to 'raw_content_chunk' position in your SELECT statement
                        if (row.length > 3 && row[3] != null) {
                            contextChunks.add(row[3].toString());
                        }
                    }
                }
            } catch (Exception dbEx) {
                System.out.println("Knowledge database read exception caught safely: " + dbEx.getMessage());
            }

            // Aggregate the extracted documentation strings
            String aggregatedContext = contextChunks.isEmpty() ? "" : String.join("\n---\n", contextChunks);

            if (aggregatedContext.trim().isEmpty()) {
                System.out.println("Context is empty. Applying standard professional fallback message template...");
            }

            // Phase 3: Generative Draft Composition
            Assistant assistant = AiServices.builder(Assistant.class)
                    .streamingChatLanguageModel(streamingModel)
                    .build();
            String draftReply;
            if (aggregatedContext.trim().isEmpty()) {
                System.out.println("Context is empty. Applying standard professional fallback message template...");
                draftReply = "Thank you for contacting customer support. We have received your request and our engineering team is reviewing the issue. A senior support specialist will follow up with you shortly.";
            } else {
                try {
                    //ExecutorService llmExecutor = Executors.newVirtualThreadPerTaskExecutor();
                    CompletableFuture<String> future = null;
                    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                        future = CompletableFuture.supplyAsync(() -> generateDraftReply(aggregatedContext, incomingMessage.getBody(), assistant), executor);
                        draftReply = future.get(15, TimeUnit.MINUTES);
                    }catch (TimeoutException e) {
                        future.cancel(true);
                        log.warn("LLM draft generation timed out after 15 min. Applying fallback template.");
                        draftReply = "Thank you for contacting customer support. We have received your request and our engineering team is reviewing the issue. A senior support specialist will follow up with you shortly.";
                    }catch(Exception e){
                        System.out.println("Draft generation failed. Applying fallback message template. Error: " + e.getMessage());
                        draftReply = "Thank you for contacting customer support. We have received your request and our engineering team is reviewing the issue. A senior support specialist will follow up with you shortly.";
                    }
                }catch(Exception e){
                    log.error("Draft generation failed. Applying fallback message template. Error: {}", e.getMessage(), e);
                    System.out.println("Draft generation failed. Applying fallback message template. Error: " + e.getMessage());
                    draftReply = "Thank you for contacting customer support. We have received your request and our engineering team is reviewing the issue. A senior support specialist will follow up with you shortly.";
                }
            }

            incomingMessage.setAiProposedDraft(draftReply);


            if (analysis.confidenceScore() >= 0.85) {
                ticket.setStatus(Ticket.TicketStatus.AI_PROPOSED);
            } else {
                ticket.setStatus(Ticket.TicketStatus.OPEN);
            }

        } catch (RuntimeException e) {
            System.out.println("LangChain4j pipeline threw an exception. " + e.getMessage());
            String errorLog = e.getMessage() != null ? e.getMessage() : "";

            // Modified to catch API errors AND database empty returns safely
            if (errorLog.contains("503") || errorLog.contains("429") || errorLog.contains("UNAVAILABLE") || errorLog.contains("No results")) {
                System.out.println("Pipeline anomaly detected. Routing instantly to backup processing...");
                return executeBackupProcessing(ticket, e);
            }

            throw e;
        }
        return "Ok";
    }

    public String generateDraftReply(String aggregatedContent, String incomingMessageBody,Assistant assistant){
        CompletableFuture<String> futureDraft = new CompletableFuture<>();
        //StringBuilder sb = new StringBuilder();
        try {
            assistant.draftReply(aggregatedContent, incomingMessageBody)
                    .onPartialResponse(token -> {
                        // Tokens arrive incrementally, keeping the Ollama HTTP connection alive
                        //sb.append(token);
                        //System.out.print(token);
                    })
                    .onCompleteResponse(response -> {
                        // Extract the full String once Ollama finishes
                        //System.out.print(response.aiMessage().text());
                        futureDraft.complete(response.aiMessage().text());
                    })
                    .onError(futureDraft::completeExceptionally)
                    .start();
            return futureDraft.get(15, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            futureDraft.cancel(true);
            log.warn("Draft generation timed out. Applying fallback message template.");
            String draftReply = "Thank you for contacting customer support. We have received your request and our engineering team is reviewing the issue. A senior support specialist will follow up with you shortly.";
            return draftReply;
        } catch (Exception e) {
            System.out.print(e.getMessage());
            String draftReply = "Thank you for contacting customer support. We have received your request and our engineering team is reviewing the issue. A senior support specialist will follow up with you shortly.";
            return draftReply;
        }

    }
    private String executeBackupProcessing(Ticket ticket, Exception rootCause) {
        System.out.println("Executing fallback logic directly inside service context...");
        ticket.setPriority(Ticket.TicketPriority.MEDIUM);
        ticket.setStatus(Ticket.TicketStatus.OPEN);
        return "{\"status\":\"FALLBACK_PROCESSED\",\"error_handled\":\"" + rootCause.getClass().getSimpleName() + "\"}";
    }
}
