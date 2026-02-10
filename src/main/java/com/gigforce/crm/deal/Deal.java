package com.gigforce.crm.deal;

import com.gigforce.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "deals")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class Deal extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(name = "deal_value", precision = 15, scale = 2)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DealStage stage;

    @Column(name = "contact_id")
    private UUID contactId;

    @Column(name = "closed_at")
    private LocalDate closedAt;

    @Column(length = 500)
    private String notes;
}
