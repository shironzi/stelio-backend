/*
 * Service layer for handling JWT token and validation.
 */

package com.aaronjosh.real_estate_app.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        long expiration = currentTime + (24 * 60 * 60 * 1000); // Expires at 15 mins

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .issuedAt(new Date())
                .expiration(new Date(expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /*
     * Extracts a specific claim from a JWT token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimFunction) {
        Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
        return claimFunction.apply(claims);
    }

    // Extracts role from token
    public Role extractRole(String token) {
        return extractClaim(token, claims -> Role.valueOf(claims.get("role", String.class)));
    }

    // Extract user entity from token
    public UserEntity getUserByToken(String token) {
        String email = extractClaim(token, claims -> claims.getSubject());

        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
        return user;
    }

    /*
     * Validate JWT token against a given user.
     * - Token must be belong to email.
     * - Token must not be expired.
     */
    public boolean isAccessTokenValid(String token, UserEntity user) {
        boolean isBlacklisted = blacklistedTokensRepo.findByToken(token).isPresent();
        boolean isExpired = extractClaim(token, Claims::getExpiration).before(new Date());

        return (!isExpired && !isBlacklisted);
    }

    /*
     * blacklisting token on logout
     */
    public void revokeToken(String token) {
        // Returns the token expiration date and time.
        Date date = extractClaim(token, Claims::getExpiration);
        LocalDateTime expirationDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()),
                ZoneId.systemDefault());

        BlacklistedTokens revokedToken = new BlacklistedTokens();
        revokedToken.setToken(token);
        revokedToken.setExpiresAt(expirationDate);

        blacklistedTokensRepo.save(revokedToken);
    }
}
