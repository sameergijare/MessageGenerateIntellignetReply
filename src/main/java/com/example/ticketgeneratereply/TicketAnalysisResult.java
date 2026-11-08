package com.example.ticketgeneratereply;


/**
 * Immutable object used to ingest structural analysis straight from an AI LLM Model pass.
 */
public record TicketAnalysisResult(
        String inferredPriority,
        String categoryTag,
        Double confidenceScore
        /*String primaryUrgencyReason,
        List<String> keyPhrases*/
) {}

