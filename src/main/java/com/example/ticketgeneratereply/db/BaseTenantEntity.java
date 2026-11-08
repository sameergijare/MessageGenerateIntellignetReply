package com.example.ticketgeneratereply.db;


import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

/**
 * Global Multi-Tenancy Definition.
 * This Hibernate filter ensures that no query can accidentally cross-contaminate data between companies.
 */
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class),defaultCondition = "tenant_id = :tenantId")
@Filter(name = "tenantFilter")
@MappedSuperclass
public abstract class BaseTenantEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}


