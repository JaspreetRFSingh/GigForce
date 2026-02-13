package com.gigforce.invoice;

import com.gigforce.common.ApiResponse;
import com.gigforce.invoice.dto.InvoiceRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice lifecycle: DRAFT -> SENT -> PAID | OVERDUE")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @Operation(summary = "List invoices for the current tenant")
    public ApiResponse<List<Invoice>> list() { return ApiResponse.ok(invoiceService.findAll()); }

    @GetMapping("/{id}")
    public ApiResponse<Invoice> get(@PathVariable UUID id) { return ApiResponse.ok(invoiceService.findById(id)); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a draft invoice with line items")
    public ApiResponse<Invoice> create(@Valid @RequestBody InvoiceRequest req) {
        return ApiResponse.ok("Invoice created", invoiceService.create(req));
    }

    @PatchMapping("/{id}/send")
    @Operation(summary = "Mark invoice as SENT")
    public ApiResponse<Invoice> send(@PathVariable UUID id)    { return ApiResponse.ok("Invoice sent",    invoiceService.markSent(id)); }

    @PatchMapping("/{id}/pay")
    @Operation(summary = "Mark invoice as PAID")
    public ApiResponse<Invoice> pay(@PathVariable UUID id)     { return ApiResponse.ok("Invoice paid",    invoiceService.markPaid(id)); }

    @PatchMapping("/{id}/overdue")
    @Operation(summary = "Mark invoice as OVERDUE")
    public ApiResponse<Invoice> overdue(@PathVariable UUID id) { return ApiResponse.ok("Marked overdue",  invoiceService.markOverdue(id)); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a DRAFT invoice")
    public void delete(@PathVariable UUID id)                  { invoiceService.delete(id); }
}
