package com.gigforce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * GigForce — multi-tenant SaaS CRM for freelancers.
 *
 * Architecture highlights:
 *   • Row-level multi-tenancy via TenantContext (ThreadLocal)
 *   • Stateless JWT authentication (jjwt 0.12)
 *   • Event-driven notifications via Spring ApplicationEvent
 *   • H2 embedded DB — zero-install local dev
 *   • OpenAPI docs at /swagger-ui.html
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class GigForceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GigForceApplication.class, args);
    }
}
