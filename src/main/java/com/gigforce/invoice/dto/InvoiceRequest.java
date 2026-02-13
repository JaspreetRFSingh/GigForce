package com.gigforce.invoice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceRequest(
    @NotBlank String clientName,
    String clientEmail,
    LocalDate dueDate,
    BigDecimal taxRate,
    @NotEmpty @Valid List<InvoiceItemRequest> items
) {}
