package com.example.ticketgeneratereply.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Look for your specific required multi-tenant header
        String tenantId = request.getHeader("X-Tenant-ID");

        if (tenantId == null || tenantId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing required Multi-Tenant Context Routing Header.");
            return false; // Blocks the request
        }

        // Optional: Save the tenant context to a ThreadLocal or Context Holder here
        return true; // Continues the request
    }
}

