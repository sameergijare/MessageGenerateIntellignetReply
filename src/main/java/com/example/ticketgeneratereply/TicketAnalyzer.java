package com.example.ticketgeneratereply;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface TicketAnalyzer {
    @UserMessage("Analyze the following support ticket and extract structured data: {{ticketText}}")
    AnalyzedTicket analyze(@V("ticketText") String ticketText);
}
