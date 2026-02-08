package com.gigforce.auth.dto;

public record AuthResponse(
    String token,
    long   expiresIn,
    String email,
    String name,
    String tenantId,
    String role
) {}
