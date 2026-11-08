package com.example.ticketgeneratereply.controller;

import com.example.ticketgeneratereply.TenantContext;
import com.example.ticketgeneratereply.db.Ticket;
import com.example.ticketgeneratereply.db.TicketRepository;
import com.example.ticketgeneratereply.db.Customer;
import com.example.ticketgeneratereply.db.KnowledgeEmbedding;
import com.example.ticketgeneratereply.db.KnowledgeEmbeddingRepository;
import com.example.ticketgeneratereply.db.Message;
import com.example.ticketgeneratereply.service.AITicketingService;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
/*import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;*/

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class SmokeTestController {
    //@Autowired
    private final AITicketingService aiTicketingService;
    //Autowired
    private final KnowledgeEmbeddingRepository embeddingRepository;
    //@Autowired
    private final TicketRepository ticketRepository;
    //@Autowired
    private final EmbeddingModel embeddingModel;

    public SmokeTestController(AITicketingService aiTicketingService,
                               KnowledgeEmbeddingRepository embeddingRepository,
                               TicketRepository ticketRepository,
                               EmbeddingModel embeddingModel) {
        this.aiTicketingService = aiTicketingService;
        this.embeddingRepository = embeddingRepository;
        this.ticketRepository = ticketRepository;
        this.embeddingModel = embeddingModel;
    }

    @GetMapping
    @RequestMapping("/smoketest")
    @PreAuthorize("T(com.example.demo.TenantContext).getCurrentTenantId() != null")
    public ResponseEntity<Map<String, Object>> runDiagnostic() {
        try {
            // 1. Manually set a mock tenant context for this thread execution
            String testTenant = "uk-retail-test-ltd";
            TenantContext.setCurrentTenantId(testTenant);

            // 2. Setup mock data objects
            Customer customer = new Customer();
            customer.setName("John Doe");
            customer.setEmail("john@example.uk");
            customer.setTenantId(testTenant);

            Ticket ticket = new Ticket();
            ticket.setSubject("Billing Issue: Overcharged on monthly subscription invoice");
            ticket.setCustomer(customer);
            ticket.setTenantId(testTenant);

            Message incomingMessage = new Message();
            incomingMessage.setBody("Hello, I noticed my corporate account was billed £199 instead of our contracted £49 tier this morning. Can you please check this and issue a refund?");
            incomingMessage.setSenderEmail("john@example.uk");
            incomingMessage.setSenderType(Message.SenderType.CUSTOMER);
            ticket.addMessage(incomingMessage);

            // 3. Inject a vector knowledge chunk directly into pgvector to simulate a matched FAQ
            KnowledgeEmbedding policyKb = new KnowledgeEmbedding();
            policyKb.setReferenceTitle("Subscription & Refund Policy Framework");
            policyKb.setRawContentChunk("Standard account subscription tiers cost £49/month. Enterprise accounts cost £199/month. If a billing dispute arises regarding mismatched tiers, support agents can approve refunds up to £150 after checking the signed contract agreement.");
            policyKb.setTenantId(testTenant);

            // Generate vectors via OpenAI embedding model API
            float[] vectorValues = embeddingModel.embed(policyKb.getRawContentChunk()).content().vector();
            policyKb.setEmbedding(vectorValues);
            embeddingRepository.save(policyKb);

            // 4. Run the entire pipeline (Classification -> Vector Distance Search -> Anthropic Prompt Compilation)
            String response = aiTicketingService.processIncomingTicket(ticket, incomingMessage);

            // Clear context safely
            TenantContext.clear();
            if(response.equalsIgnoreCase("ok")) {
                return ResponseEntity.ok(Map.of(
                        "status", "SUCCESS",
                        "database_connectivity", "CONNECTED (pgvector operational)",
                        "ai_classification_tag", ticket.getAiClassification(),
                        "ai_inferred_priority", ticket.getPriority().name(),
                        "ai_confidence_score", ticket.getAiConfidenceScore(),
                        "claude_drafted_response", incomingMessage.getAiProposedDraft(),
                        "timestamp", Instant.now().toString()
                ));
            }else{
                return ResponseEntity.ok(Map.of(response, "Fallback logic executed directly inside service context."));
            }

        } catch (Exception e) {
            TenantContext.clear();
            return ResponseEntity.status(500).body(Map.of(
                    "status", "FAILED",
                    "error_message", e.getMessage(),
                    "error_class", e.getClass().getName()
            ));
        }
    }

    @GetMapping
    @RequestMapping("/seed-knowledge")
    public ResponseEntity<String> seedTenantKnowledge(@RequestHeader("X-Tenant-ID") String tenantId) {
        System.out.println("Excuting manual on-demand data seed for tenant: " + tenantId);

        try {
            seedDocument(tenantId, "Password Reset Policy",
                    "To reset your password, navigate to the retail portal login screen and click on Forgot Password. Enter your registered email address.");

            seedDocument(tenantId, "Billing Discrepancies",
                    "If a profile reports a double charge on their credit card statement, duplicate system transactions are flagged automatically and refunded within 3 days.");

            return ResponseEntity.ok("On-demand knowledge base seeding completed for tenant: " + tenantId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Seeding failed: " + e.getMessage());
        }
    }

    private void seedDocument(String tenantId, String title, String text) {
        KnowledgeEmbedding doc = new KnowledgeEmbedding();
        doc.setTenantId(tenantId);
        doc.setReferenceTitle(title);
        doc.setRawContentChunk(text);

        // Pass the raw float[] array from your local Bge model straight to your entity
        float[] vectors = embeddingModel.embed(text).content().vector();
        doc.setEmbedding(vectors);

        embeddingRepository.save(doc);
    }
}
