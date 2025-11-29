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
import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminKeyFilter extends OncePerRequestFilter {

    private final ClientRepository clientRepository;
    private static final List<String> EXCLUDED_PATHS = List.of("/api/clients/create");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // If the path is a public one (like /create), or not an admin path, skip the filter.
        if (!path.startsWith("/api/clients/") || EXCLUDED_PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String providedKey = request.getHeader("X-Admin-Key");

        if (providedKey == null || providedKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Unauthorized: Missing Admin Key.\"}");
            return;
        }

        if (clientRepository.findByAdminKey(providedKey).isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Unauthorized: Invalid Admin Key.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
