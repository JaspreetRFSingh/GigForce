package com.gigforce.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationLog, UUID> {
    List<NotificationLog> findTop20ByTenantIdOrderByCreatedAtDesc(String tenantId);
}
