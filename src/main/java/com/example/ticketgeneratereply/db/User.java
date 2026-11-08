package com.example.ticketgeneratereply.db;



import jakarta.persistence.*;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "users")
@Filter(name = "tenantFilter")
public class User extends BaseTenantEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // e.g., AGENT, ADMIN

    public enum Role { AGENT, ADMIN }

    // Getters and Setters
    public java.util.UUID getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}

