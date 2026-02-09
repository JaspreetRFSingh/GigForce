package com.gigforce.tenant;

import com.gigforce.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenants", description = "Tenant management")
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    @Operation(summary = "List all tenants")
    public ApiResponse<List<Tenant>> list() { return ApiResponse.ok(tenantService.findAll()); }

    @GetMapping("/{id}")
    @Operation(summary = "Get a tenant by ID")
    public ApiResponse<Tenant> get(@PathVariable String id) { return ApiResponse.ok(tenantService.findById(id)); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new tenant")
    public ApiResponse<Tenant> create(@RequestBody Tenant t) { return ApiResponse.ok("Tenant created", tenantService.create(t)); }

    @PutMapping("/{id}")
    @Operation(summary = "Update a tenant")
    public ApiResponse<Tenant> update(@PathVariable String id, @RequestBody Tenant t) {
        return ApiResponse.ok("Tenant updated", tenantService.update(id, t));
    }
}
