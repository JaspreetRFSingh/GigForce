package com.gigforce.invoice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record InvoiceItemRequest(
    @NotBlank String description,
    @Positive int quantity,
    @Positive BigDecimal unitPrice
) {}
