package com.aaronjosh.real_estate_app.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    // Excludes the public path from filter chain
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest req) {
        String path = req.getServletPath();
        String method = req.getMethod();
        String authHeader = req.getHeader("Authorization");

        // Public endpoints
        boolean isPublicPropertiesGet = path.equals("/api/properties/") && method.equalsIgnoreCase("GET");
        boolean isPublicImageGet = path.startsWith("/api/image/") && method.equalsIgnoreCase("GET");
        boolean isAuthEndpoint = path.equals("/api/auth/register") || path.equals("/api/auth/login");
        boolean isWebhookEndpoint = path.startsWith("/api/webhook/");
        boolean isWebSocketEndpoint = path.startsWith("/ws");
        boolean isFileEndpoint = path.startsWith("/api/files") && method.equalsIgnoreCase("GET");

        boolean isAuthenticatedAppPath = authHeader != null && path.startsWith("/app");

        return isWebSocketEndpoint
                || isAuthenticatedAppPath
                || isPublicPropertiesGet
                || isPublicImageGet
                || isAuthEndpoint
                || isWebhookEndpoint
                || isFileEndpoint;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            final String authHeader = req.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new InsufficientAuthenticationException("Missing or invalid Authorization header");
            }

            final String jwt = authHeader.substring(7);

            if (!jwtService.isAccessTokenValid(jwt)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("Invalid or Expired token.");
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        jwtService.extractUserDetails(jwt), null, jwtService.extractAuthorities(jwt));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(req, res);
        } catch (JwtException e) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("Authentication failed");
        }
    }

}
