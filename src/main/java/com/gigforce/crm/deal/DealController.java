package com.gigforce.crm.deal;

import com.gigforce.common.ApiResponse;
import com.gigforce.crm.deal.dto.DealRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@Tag(name = "Deals", description = "Sales pipeline (6 stages)")
public class DealController {

    private final DealService dealService;

    @GetMapping
    @Operation(summary = "List all deals for the current tenant")
    public ApiResponse<List<Deal>> list() { return ApiResponse.ok(dealService.findAll()); }

    @GetMapping("/{id}")
    public ApiResponse<Deal> get(@PathVariable UUID id) { return ApiResponse.ok(dealService.findById(id)); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new deal")
    public ApiResponse<Deal> create(@Valid @RequestBody DealRequest req) {
        return ApiResponse.ok("Deal created", dealService.create(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update deal details")
    public ApiResponse<Deal> update(@PathVariable UUID id, @Valid @RequestBody DealRequest req) {
        return ApiResponse.ok("Deal updated", dealService.update(id, req));
    }

    @PatchMapping("/{id}/stage")
    @Operation(summary = "Advance the deal stage")
    public ApiResponse<Deal> stage(@PathVariable UUID id, @RequestParam DealStage stage) {
        return ApiResponse.ok("Stage updated", dealService.updateStage(id, stage));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { dealService.delete(id); }
}
