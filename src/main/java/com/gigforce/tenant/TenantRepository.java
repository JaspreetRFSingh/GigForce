package com.gigforce.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, String> {
    boolean existsByIdAndActiveTrue(String id);
}
