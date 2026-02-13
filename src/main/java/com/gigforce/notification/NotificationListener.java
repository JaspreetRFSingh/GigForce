package com.gigforce.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Async consumer for NotificationEvent.
 * Persists events to notification_logs and writes a structured log line.
 *
 * Migration path to Kafka: replace @EventListener/@Async with
 * @KafkaListener(topics = "gigforce-events", groupId = "notification-service")
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationRepository repo;

    @EventListener
    @Async
    public void onNotification(NotificationEvent event) {
        repo.save(NotificationLog.builder()
            .tenantId(event.getTenantId())
            .type(event.getType())
            .message(event.getMessage())
            .createdAt(Instant.now())
            .build());
        log.info("[{}][{}] {}", event.getTenantId(), event.getType(), event.getMessage());
    }
}
