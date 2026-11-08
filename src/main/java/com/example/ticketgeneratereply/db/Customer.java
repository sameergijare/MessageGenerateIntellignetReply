package com.example.ticketgeneratereply.db;



import jakarta.persistence.*;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "customers")
@Filter(name = "tenantFilter")
public class Customer extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    // Getters and Setters
    public java.util.UUID getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

