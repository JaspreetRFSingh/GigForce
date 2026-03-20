package com.gigforce.auth;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String  generateToken(UserDetails user, String tenantId, String role);
    String  extractUsername(String token);
    String  extractTenantId(String token);
    boolean isTokenValid(String token, UserDetails userDetails);
    long    getExpiration();
}
