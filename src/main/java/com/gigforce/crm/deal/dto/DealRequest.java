package com.gigforce.crm.deal.dto;

import com.gigforce.crm.deal.DealStage;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record DealRequest(
    @NotBlank(message = "Title is required") String title,
    @NotNull @Positive BigDecimal value,
    @NotNull DealStage stage,
    UUID contactId,
    String notes
) {}
