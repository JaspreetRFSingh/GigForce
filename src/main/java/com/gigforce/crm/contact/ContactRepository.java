package com.gigforce.crm.contact;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, UUID> {
    List<Contact>     findAllByTenantId(String tenantId);
    Optional<Contact> findByIdAndTenantId(UUID id, String tenantId);
    long              countByTenantId(String tenantId);
}
