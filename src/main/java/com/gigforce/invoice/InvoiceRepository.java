package com.gigforce.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice>    findAllByTenantIdOrderByCreatedAtDesc(String tenantId);
    Optional<Invoice> findByIdAndTenantId(UUID id, String tenantId);
    long             countByTenantIdAndStatus(String tenantId, InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.total),0) FROM Invoice i WHERE i.tenantId=:tenantId AND i.status=:status")
    BigDecimal sumPaidTotalByTenantId(String tenantId, InvoiceStatus status);

    long countByTenantId(String tenantId);
}
