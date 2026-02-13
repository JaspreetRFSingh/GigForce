package com.gigforce.notification;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Domain event published on significant actions (deal stage change, invoice paid, etc).
 *
 * Consumed asynchronously by NotificationListener — the Spring ApplicationEvent
 * equivalent of a Kafka topic. To migrate: replace @EventListener with
 * @KafkaListener and publish to an Azure Service Bus topic instead.
 */
@Getter
public class NotificationEvent extends ApplicationEvent {

    private final String tenantId;
    private final String type;     // DEAL | INVOICE | CONTACT | SYSTEM
    private final String message;

    public NotificationEvent(Object source, String tenantId, String type, String message) {
        super(source);
        this.tenantId = tenantId;
        this.type     = type;
        this.message  = message;
    }
}
