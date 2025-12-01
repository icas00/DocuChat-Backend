package com.aiassistant.security;

import com.aiassistant.model.Client;
import com.aiassistant.repository.ClientRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class AdminKeyFilter extends OncePerRequestFilter {

    private final ClientRepository clientRepository;
    private static final List<String> EXCLUDED_PATHS = List.of("/api/clients/create", "/api/clients/admin/data");
    private static final Pattern CLIENT_ID_PATTERN = Pattern.compile("/api/clients/(\\d+)/.*");

    // Cache valid admin keys for 5 minutes to reduce DB load
    private final Cache<String, Long> adminKeyCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // If the path is a public one (like /create), or not an admin path, skip the
        // filter.
        if (!path.startsWith("/api/clients/") || EXCLUDED_PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String providedKey = request.getHeader("X-Admin-Key");

        if (providedKey == null || providedKey.isBlank()) {
            sendError(response, "Unauthorized: Missing Admin Key.");
            return;
        }

        Long authenticatedClientId = adminKeyCache.getIfPresent(providedKey);

        if (authenticatedClientId == null) {
            Optional<Client> clientOpt = clientRepository.findByAdminKey(providedKey);
            if (clientOpt.isEmpty()) {
                sendError(response, "Unauthorized: Invalid Admin Key.");
                return;
            }
            authenticatedClientId = clientOpt.get().getId();
            adminKeyCache.put(providedKey, authenticatedClientId);
        }

        // IDOR Check: Ensure the authenticated client is accessing their own data
        Matcher matcher = CLIENT_ID_PATTERN.matcher(path);
        if (matcher.matches()) {
            Long targetClientId = Long.parseLong(matcher.group(1));
            if (!authenticatedClientId.equals(targetClientId)) {
                sendError(response, "Forbidden: You do not have permission to access this client's data.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
