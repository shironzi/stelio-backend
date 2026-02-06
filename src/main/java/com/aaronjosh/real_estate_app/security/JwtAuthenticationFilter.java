package com.aaronjosh.real_estate_app.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aaronjosh.real_estate_app.dto.auth.UserDetails;
import com.aaronjosh.real_estate_app.models.UserEntity;

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

        final String authHeader = req.getHeader("Authorization");

        return (authHeader == null && (path.equals("/api/properties/") && method.equalsIgnoreCase("GET"))
                || (path.startsWith("/api/image/") && method.equalsIgnoreCase("GET"))
                || path.equals("/api/auth/register") || path.equals("/api/auth/login"));
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

            UserEntity user = jwtService.getUserByToken(jwt);

            if (!jwtService.isAccessTokenValid(jwt, user)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("Invalid or expired token.");
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = new UserDetails();

                userDetails.setId(user.getId());
                userDetails.setEmail(user.getEmail());
                userDetails.setFirstname(user.getFirstname());
                userDetails.setLastname(user.getLastname());
                userDetails.setRole(user.getRole());

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, user.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(req, res);
        } catch (JwtException | InsufficientAuthenticationException e) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("Authentication failed: " + e.getMessage());
        }
    }

}
