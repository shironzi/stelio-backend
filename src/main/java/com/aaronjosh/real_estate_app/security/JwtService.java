/*
 * Service layer for handling JWT token and validation.
 */

package com.aaronjosh.real_estate_app.security;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aaronjosh.real_estate_app.models.BlacklistedTokens;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.models.UserEntity.Role;
import com.aaronjosh.real_estate_app.repositories.BlacklistedTokensRepo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Autowired
    private BlacklistedTokensRepo blacklistedTokensRepo;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /*
     * Generate Jwt Token for a user.
     * - Includes claims: userId, email and role
     * - Token expires in 24 hours
     */
    public String generateToken(UserEntity user) {
        long currentTime = System.currentTimeMillis(); // current time
        long expiration = currentTime + (24 * 60 * 60 * 1000); // 24 hours

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

    /*
     * Extract email from Jwt token.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /*
     * Extracts user id from Jwt token.
     */

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /*
     * Extracts role from Jwt token.
     */

    public Role extractRole(String token) {
        return extractClaim(token, claims -> Role.valueOf(claims.get("role", String.class)));
    }

    /* Cheks if JWT token is expired. */
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /*
     * Validate JWT token against a given user.
     * - Token must be belong to email.
     * - Token must not be expired.
     */
    public boolean isTokenValid(String token, UserEntity user) {
        final String email = extractEmail(token);

        return (email.equals(user.getEmail()) && !isTokenExpired(token));
    }

    /*
     * blacklisting token on logout
     */
    public void revokeToken(String token) {

        BlacklistedTokens revokedToken = new BlacklistedTokens();
        revokedToken.setToken(token);

        blacklistedTokensRepo.save(revokedToken);
    }

    public Boolean isBlacklisted(String token) {
        return blacklistedTokensRepo.findByToken(token).isPresent();
    }
}
