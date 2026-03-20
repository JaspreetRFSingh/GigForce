package com.gigforce.auth;

import com.gigforce.auth.dto.LoginRequest;
import com.gigforce.auth.dto.RegisterRequest;
import com.gigforce.auth.model.User;
import com.gigforce.auth.model.UserRole;
import com.gigforce.auth.repository.UserRepository;
import com.gigforce.auth.JwtService;
import com.gigforce.tenant.TenantRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock UserRepository        userRepository;
    @Mock TenantRepository      tenantRepository;
    @Mock PasswordEncoder       passwordEncoder;
    @Mock AuthenticationManager authManager;
    @Mock JwtService            jwtUtil;

    @InjectMocks AuthService authService;

    @BeforeEach
    void setUp() {
        when(tenantRepository.existsByIdAndActiveTrue("acme")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-pw");
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("mock-jwt");
        when(jwtUtil.getExpiration()).thenReturn(86400000L);
    }

    @Test
    @DisplayName("register: new user gets a JWT token")
    void register_newUser_returnsToken() {
        when(userRepository.existsByEmail("new@acme.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var resp = authService.register(new RegisterRequest("New User", "new@acme.com", "pass1234", "acme"));

        assertThat(resp.token()).isEqualTo("mock-jwt");
        assertThat(resp.tenantId()).isEqualTo("acme");
        assertThat(resp.role()).isEqualTo("MEMBER");
    }

    @Test
    @DisplayName("register: duplicate email throws")
    void register_duplicateEmail_throws() {
        when(userRepository.existsByEmail("dup@acme.com")).thenReturn(true);
        assertThatThrownBy(() ->
            authService.register(new RegisterRequest("Dup", "dup@acme.com", "pass1234", "acme")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("register: invalid tenant throws")
    void register_invalidTenant_throws() {
        when(tenantRepository.existsByIdAndActiveTrue("ghost")).thenReturn(false);
        assertThatThrownBy(() ->
            authService.register(new RegisterRequest("X", "x@ghost.com", "pass1234", "ghost")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid");
    }

    @Test
    @DisplayName("login: valid credentials return JWT")
    void login_validCredentials_returnsToken() {
        User user = User.builder().email("admin@acme.com").name("Admin")
            .tenantId("acme").role(UserRole.OWNER).password("hashed").build();
        when(userRepository.findByEmail("admin@acme.com")).thenReturn(Optional.of(user));

        var resp = authService.login(new LoginRequest("admin@acme.com", "password123"));

        assertThat(resp.token()).isEqualTo("mock-jwt");
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
