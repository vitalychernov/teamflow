package com.teamflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter — runs once per HTTP request.
 *
 * Flow for each incoming request:
 *   1. Read "Authorization: Bearer <token>" header
 *   2. Extract email from token
 *   3. Load user from DB (via UserDetailsService)
 *   4. Validate token (email matches + not expired)
 *   5. Set authentication in SecurityContextHolder
 *   6. Pass request to next filter in chain
 *
 * OncePerRequestFilter guarantees this filter runs exactly once,
 * even if the request is forwarded internally.
 *
 * If the token is missing or invalid, the filter just continues
 * without setting authentication — Spring Security will then
 * reject the request at the authorization step (401/403).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // If no Bearer token — skip JWT processing, continue filter chain.
        // Spring Security will handle unauthorized access downstream.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token (remove "Bearer " prefix — 7 characters)
        final String token = authHeader.substring(7);
        final String email = jwtService.extractEmail(token);

        // Only process if email is valid AND no authentication already set
        // (avoid re-authenticating on forwarded requests)
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtService.isTokenValid(token, userDetails)) {
                // Create authentication token with user's authorities
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                          // credentials not needed after auth
                                userDetails.getAuthorities()
                        );

                // Attach request details (IP address, session) for audit logging
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Store authentication in SecurityContext for this request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
