package com.aiassistant.security;

import com.aiassistant.repository.ClientRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AdminKeyFilter extends OncePerRequestFilter {

    private final ClientRepository clientRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Only apply this filter to client management endpoints.
        if (request.getServletPath().startsWith("/api/clients/")) {
            String providedKey = request.getHeader("X-Admin-Key");

            if (providedKey == null || providedKey.isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"message\":\"Unauthorized: Missing Admin Key.\"}");
                return;
            }

            // Check if a client exists with the provided admin key.
            if (clientRepository.findByAdminKey(providedKey).isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"message\":\"Unauthorized: Invalid Admin Key.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
