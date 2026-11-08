package com.example.ticketgeneratereply.db;



import jakarta.persistence.*;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "knowledge_embeddings")
@Filter(name = "tenantFilter")
public class KnowledgeEmbedding extends BaseTenantEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(nullable = false)
    private String referenceTitle; // e.g., "Refund Policy v2"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String rawContentChunk; // The parsed text snippet fed to LLM context

    /**
     * Stores the high-dimensional mathematical vector.
     * Maps cleanly to pgvector. Requires a 1536 dimension array for text-embedding-3-small / OpenAI.
     */
    @Column(columnDefinition = "vector(384)", nullable = false)
    private float[] embedding;

    // Getters and Setters
    public java.util.UUID getId() { return id; }
    public String getReferenceTitle() { return referenceTitle; }
    public void setReferenceTitle(String referenceTitle) { this.referenceTitle = referenceTitle; }
    public String getRawContentChunk() { return rawContentChunk; }
    public void setRawContentChunk(String rawContentChunk) { this.rawContentChunk = rawContentChunk; }
    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
}

