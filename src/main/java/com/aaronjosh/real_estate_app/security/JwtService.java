package com.aaronjosh.real_estate_app.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.aaronjosh.real_estate_app.dto.auth.UserDetails;
import com.aaronjosh.real_estate_app.models.BlacklistedTokens;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.models.UserEntity.Role;
import com.aaronjosh.real_estate_app.repositories.BlacklistedTokensRepo;
import com.aaronjosh.real_estate_app.repositories.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Autowired
    private BlacklistedTokensRepo blacklistedTokensRepo;

    @Autowired
    private UserRepository userRepo;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /*
     * Generate Jwt Token for a user.
     * - Includes claims: userId, email and role
     * - Token expires in 24 hours
     */
    public String generateAccessToken(UserEntity user) {
        long currentTime = System.currentTimeMillis(); // current time
        long expiration = currentTime + (24 * 60 * 60 * 1000); // Expires 24 hrs

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("firstname", user.getFirstname())
                .claim("lastname", user.getLastname())
                .issuedAt(new Date())
                .expiration(new Date(expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /*
     * Extracts a specific claim from a JWT token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Extracts role from token
    public Role extractRole(String token) {
        return Role.valueOf(extractAllClaims(token).get("role", String.class));
    }

    public List<GrantedAuthority> extractAuthorities(String token) {
        String role = extractRole(token).toString();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public UserDetails extractUserDetails(String token) {
        UserDetails userDetails = new UserDetails();
        Claims claims = extractAllClaims(token);

        userDetails.setId(UUID.fromString(claims.getSubject()));
        userDetails.setEmail(claims.get("email", String.class));
        userDetails.setFirstname(claims.get("firstname", String.class));
        userDetails.setLastname(claims.get("lastname", String.class));
        userDetails.setRole(Role.valueOf(claims.get("role", String.class)));
        return userDetails;
    }

    /*
     * Validate JWT token against a given user.
     * - Token must be belong to email.
     * - Token must not be expired.
     * - User must be exists
     */
    public boolean isAccessTokenValid(String token) {
        Claims claims = extractAllClaims(token);

        boolean isBlacklisted = blacklistedTokensRepo.findByToken(token).isPresent();
        boolean isExpired = claims.getExpiration().before(new Date());
        UUID userId = UUID.fromString(claims.getSubject());
        String email = claims.get("email", String.class);

        return userRepo.findById(userId)
                .map(user -> !isExpired &&
                        !isBlacklisted &&
                        user.getEmail().equals(email))
                .orElse(false);
    }

    /*
     * blacklisting token on logout
     */
    public void revokeToken(String token) {
        // Returns the token expiration date and time.
        Date date = extractAllClaims(token).getExpiration();
        LocalDateTime expirationDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()),
                ZoneId.systemDefault());

        BlacklistedTokens revokedToken = new BlacklistedTokens();
        revokedToken.setToken(token);
        revokedToken.setExpiresAt(expirationDate);

        blacklistedTokensRepo.save(revokedToken);
    }
}
