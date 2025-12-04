package com.aaronjosh.real_estate_app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.aaronjosh.real_estate_app.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {
        @Autowired
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        // using bcrypt for the password hashing
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // removing csrf for rest api
                                .csrf(csrf -> csrf.disable())
                                .cors(Customizer.withDefaults())
                                // handling the session validity
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Setting the authorization on routes
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/api/auth/login", "/api/auth/register").anonymous()
                                                .requestMatchers(HttpMethod.POST, "/api/property", "/api/property/**")
                                                .hasRole("OWNER")
                                                .requestMatchers(HttpMethod.DELETE, "/api/property/**").hasRole("OWNER")
                                                .requestMatchers(HttpMethod.GET, "/api/property/my-properties")
                                                .hasRole("OWNER")
                                                .requestMatchers(HttpMethod.GET, "/api/property/", "/api/image/**")
                                                .permitAll()
                                                .anyRequest().authenticated())

                                // disable basic auth and default form login
                                .httpBasic(httpBasic -> httpBasic.disable())
                                .formLogin(form -> form.disable())

                                // exception handling with the bad credentials
                                .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, authException) -> {
                                        res.setContentType("application/json");

                                        if (authException instanceof BadCredentialsException) {
                                                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                                res.getWriter().write("""
                                                                {"error":"Forbidden","message":"%s"}
                                                                """.formatted(authException.getMessage()));
                                        } else if (authException instanceof InsufficientAuthenticationException) {
                                                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                res.getWriter().write("""
                                                                {"error":"Unauthorized","message":"%s"}
                                                                """.formatted(authException.getMessage()));
                                                System.out.println(authException.getMessage());
                                        } else {
                                                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                res.getWriter().write("""
                                                                {"error":"Unauthorized","message":"%s"}
                                                                """.formatted(authException.getMessage()));
                                        }
                                }))

                                // validates token
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
