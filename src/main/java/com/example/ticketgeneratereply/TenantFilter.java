package com.example.ticketgeneratereply;



import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Intercepts incoming HTTP requests to resolve and extract the Tenant Identifier.
 */
@Component
@Order(1)
public class TenantFilter implements Filter {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Strategy: Resolve via custom header. (Alternative: parse the request server host name url)
        String tenantId = httpRequest.getHeader(TENANT_HEADER);

        if (tenantId == null || tenantId.trim().isEmpty()) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.getWriter().write("Missing required Multi-Tenant Context Routing Header.");
            return;
        }

        try {
            TenantContext.setCurrentTenantId(tenantId);
            chain.doFilter(request, response);
        } finally {
            // Vital: Clear context to avoid leakage into recycled thread pools
            TenantContext.clear();
        }
    }
}
