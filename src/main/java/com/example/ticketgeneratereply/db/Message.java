package com.example.ticketgeneratereply.db;



import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(nullable = false)
    private String senderEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SenderType senderType;

    @Column(name = "created_at", nullable = false)
    private final Instant createdAt = Instant.now();

    // Fields for AI Copilot draft evaluation
    @Column(columnDefinition = "TEXT", name = "ai_proposed_draft")
    private String aiProposedDraft;

    @Column(name = "is_draft_approved")
    private Boolean isDraftApproved = false;

    public enum SenderType { CUSTOMER, AGENT, SYSTEM_AI }

    // Getters and Setters
    public java.util.UUID getId() { return id; }
    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    public SenderType getSenderType() { return senderType; }
    public void setSenderType(SenderType senderType) { this.senderType = senderType; }
    public Instant getCreatedAt() { return createdAt; }
    public String getAiProposedDraft() { return aiProposedDraft; }
    public void setAiProposedDraft(String aiProposedDraft) { this.aiProposedDraft = aiProposedDraft; }
    public Boolean getIsDraftApproved() { return isDraftApproved; }
    public void setIsDraftApproved(Boolean isDraftApproved) { this.isDraftApproved = isDraftApproved; }
}

