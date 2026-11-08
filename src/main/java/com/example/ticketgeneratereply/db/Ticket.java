package com.example.ticketgeneratereply.db;



import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "tickets")
@Filter(name = "tenantFilter")
public class Ticket extends BaseTenantEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(nullable = false)
    private String subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id")
    private User assignedAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority = TicketPriority.MEDIUM;

    @Column(name = "ai_classification")
    private String aiClassification; // Categorized niche (e.g., "Billing/Refunds")

    @Column(name = "ai_confidence_score")
    private Double aiConfidenceScore; // Calculation value for human routing

    @Column(name = "created_at", nullable = false, updatable = false)
    private final Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Message> messages = new ArrayList<>();

    public enum TicketStatus { NEW, AI_PROPOSED, OPEN, PENDING, RESOLVED }
    public enum TicketPriority { LOW, MEDIUM, HIGH, URGENT }

    // Helpers to guarantee bidirectional mapping consistency
    public void addMessage(Message message) {
        messages.add(message);
        message.setTicket(this);
    }

    // Getters and Setters
    public java.util.UUID getId() { return id; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public User getAssignedAgent() { return assignedAgent; }
    public void setAssignedAgent(User assignedAgent) { this.assignedAgent = assignedAgent; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }
    public String getAiClassification() { return aiClassification; }
    public void setAiClassification(String aiClassification) { this.aiClassification = aiClassification; }
    public Double getAiConfidenceScore() { return aiConfidenceScore; }
    public void setAiConfidenceScore(Double aiConfidenceScore) { this.aiConfidenceScore = aiConfidenceScore; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Message> getMessages() { return messages; }
}

