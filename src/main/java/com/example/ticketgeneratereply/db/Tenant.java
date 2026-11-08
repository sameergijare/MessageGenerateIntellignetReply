package com.example.ticketgeneratereply.db;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @Column(name = "id", length = 50)
    private String id; // e.g., "acme-corp", "uk-retail-ltd"

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private final Instant createdAt = Instant.now();

    // Getters, Setters, Constructors
    public Tenant() {}
    public Tenant(String id, String companyName) {
        this.id = id;
        this.companyName = companyName;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public Instant getCreatedAt() { return createdAt; }
}

