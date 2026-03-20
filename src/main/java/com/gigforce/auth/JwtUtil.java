package com.gigforce.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil implements JwtService {

    @Value("${app.jwt.secret}")     private String secret;
    @Value("${app.jwt.expiration}") private long   expiration;

    public String generateToken(UserDetails user, String tenantId, String role) {
        return Jwts.builder()
            .subject(user.getUsername())
            .claims(Map.of("tenantId", tenantId, "role", role))
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(signingKey())
            .compact();
    }

    public String  extractUsername(String token)  { return extractClaim(token, Claims::getSubject); }
    public String  extractTenantId(String token)  { return extractClaim(token, c -> c.get("tenantId", String.class)); }
    public long    getExpiration()                { return expiration; }

    public boolean isTokenValid(String token, UserDetails ud) {
        return extractUsername(token).equals(ud.getUsername()) && !isExpired(token);
    }

    private boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(Jwts.parser().verifyWith(signingKey()).build()
            .parseSignedClaims(token).getPayload());
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
