package com.bali.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates and validates backend JWT tokens.
 * After Firebase phone auth verification, the backend issues its own JWT
 * for subsequent API authorization.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generate a JWT token for an authenticated user.
     *
     * @param userId      the database user ID
     * @param identifier  the user's identifier (username or phone number)
     * @param role        the user's role (USER or ADMIN)
     * @return signed JWT string
     */
    public String generateToken(Long userId, String identifier, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        
        // Determine if identifier is phone number (starts with +) or username
        if (identifier != null && identifier.startsWith("+")) {
            claims.put("phoneNumber", identifier);
        } else {
            claims.put("username", identifier);
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract user ID from JWT token.
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extract username from JWT token.
     */
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("username");
    }

    /**
     * Extract phone number from JWT token.
     */
    public String extractPhoneNumber(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("phoneNumber");
    }

    /**
     * Extract role from JWT token.
     */
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        Object role = claims.get("role");
        return role == null ? null : role.toString();
    }

    /**
     * Validate JWT token.
     */
    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract all claims from a JWT token.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if a JWT token is expired.
     */
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
