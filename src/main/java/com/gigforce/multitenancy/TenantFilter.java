package com.gigforce.multitenancy;

import com.gigforce.tenant.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-ID";

    private static final Set<String> EXEMPT = Set.of(
        "/api/auth", "/h2-console", "/v3/api-docs", "/swagger-ui"
    );

    private final TenantRepository tenantRepository;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain chain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (EXEMPT.stream().anyMatch(path::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        String tenantId = request.getHeader(TENANT_HEADER);
        if (tenantId == null || tenantId.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "Missing required header: " + TENANT_HEADER);
            return;
        }
        if (!tenantRepository.existsByIdAndActiveTrue(tenantId)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                "Unknown or inactive tenant: " + tenantId);
            return;
        }

        try {
            TenantContext.set(tenantId);
            log.debug("Tenant context set: {}", tenantId);
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
