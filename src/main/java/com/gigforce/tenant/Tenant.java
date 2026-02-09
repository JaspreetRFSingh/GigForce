package com.gigforce.tenant;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tenants")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Tenant {
    /** Slug ID used in X-Tenant-ID header (e.g. "acme"). */
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    /** STARTER | PRO | ENTERPRISE */
    @Column(nullable = false)
    private String plan;

    @Column(nullable = false)
    private boolean active;
}
