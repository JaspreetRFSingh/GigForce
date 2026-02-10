package com.gigforce.crm.deal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DealRepository extends JpaRepository<Deal, UUID> {
    List<Deal>    findAllByTenantId(String tenantId);
    Optional<Deal> findByIdAndTenantId(UUID id, String tenantId);
    long          countByTenantIdAndStage(String tenantId, DealStage stage);

    @Query("SELECT COALESCE(SUM(d.value),0) FROM Deal d WHERE d.tenantId=:tenantId AND d.stage='CLOSED_WON'")
    BigDecimal sumWonValueByTenantId(String tenantId);
}
