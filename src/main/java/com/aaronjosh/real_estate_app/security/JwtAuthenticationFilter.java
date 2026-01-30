package com.aaronjosh.real_estate_app.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.repositories.UserRepository;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    AntPathMatcher matcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String path = req.getServletPath();
            String method = req.getMethod();
            final String authHeader = req.getHeader("Authorization");

            if (path.equals("/api/property/") &&
                    method.equalsIgnoreCase("GET") && authHeader == null) {
                filterChain.doFilter(req, res);
                return;
            }

            if (matcher.match("/api/image/**", path) && method.equalsIgnoreCase("GET") &&
                    authHeader == null) {
                filterChain.doFilter(req, res);
                return;
            }

            if (path.equals("/api/auth/register") || path.equals("/api/auth/login")) {
                filterChain.doFilter(req, res);
                return;
            }

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new InsufficientAuthenticationException("Missing or invalid Authorization header");
            }

            final String jwt = authHeader.substring(7);
            final String email = jwtService.extractEmail(jwt);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserEntity userEntity = userRepository.findByEmail(email)
                        .orElseThrow(() -> new BadCredentialsException(email));

                if (jwtService.isTokenValid(jwt, userEntity)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEntity, null, userEntity.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            }

            filterChain.doFilter(req, res);
        } catch (JwtException e) {
            throw new JwtException(e.getMessage());
        }
    }

}
