package com.aiassistant.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminKeyFilter extends OncePerRequestFilter {

    @Value("${app.admin-key}")
    private String requiredAdminKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Only apply this filter to client management endpoints.
        if (request.getServletPath().startsWith("/api/clients")) {
            String providedKey = request.getHeader("X-Admin-Key");

            if (providedKey == null || !providedKey.equals(requiredAdminKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: Missing or invalid Admin Key.");
                return; // Stop processing the request.
            }
        }

        // If the path doesn't require an admin key, or if the key was valid, continue.
        filterChain.doFilter(request, response);
    }
}
