package com.example.ticketgeneratereply;


import com.example.ticketgeneratereply.db.BaseTenantEntity;
import jakarta.persistence.PrePersist;

/**
 * Intercepts every JPA save operation to automatically append the thread-local Active Tenant ID.
 */
public class TenantEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof BaseTenantEntity tenantEntity) {
            // TenantContext is a ThreadLocal wrapper variable handling your user's current session
            String currentTenant = TenantContext.getCurrentTenantId();
            if (currentTenant == null) {
                throw new IllegalStateException("Database write attempted without a validated UK Tenant Context.");
            }
            tenantEntity.setTenantId(currentTenant);
        }
    }
}

