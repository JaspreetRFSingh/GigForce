package com.gigforce.auth;

import com.gigforce.auth.dto.*;
import com.gigforce.auth.model.*;
import com.gigforce.auth.repository.UserRepository;
import com.gigforce.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final TenantRepository      tenantRepository;
    private final PasswordEncoder       passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService            jwtUtil;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email()))
            throw new IllegalArgumentException("Email already registered: " + req.email());
        if (!tenantRepository.existsByIdAndActiveTrue(req.tenantId()))
            throw new IllegalArgumentException("Invalid or inactive tenant: " + req.tenantId());

        User user = User.builder()
            .name(req.name()).email(req.email())
            .password(passwordEncoder.encode(req.password()))
            .tenantId(req.tenantId()).role(UserRole.MEMBER)
            .build();
        userRepository.save(user);
        return buildResponse(user, jwtUtil.generateToken(user, user.getTenantId(), user.getRole().name()));
    }

    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User user = userRepository.findByEmail(req.email())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return buildResponse(user, jwtUtil.generateToken(user, user.getTenantId(), user.getRole().name()));
    }

    private AuthResponse buildResponse(User user, String token) {
        return new AuthResponse(token, jwtUtil.getExpiration() / 1000,
            user.getEmail(), user.getName(), user.getTenantId(), user.getRole().name());
    }
}
