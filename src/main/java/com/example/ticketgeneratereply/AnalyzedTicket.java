package com.example.ticketgeneratereply;


import com.example.ticketgeneratereply.db.Ticket;

import java.util.List;

public class AnalyzedTicket {
    private Ticket.TicketPriority priority;
    private TicketCategory category;
    private String sentimentSummary;
    private List<String> extractedKeywords;
    // Getters and setters
}


