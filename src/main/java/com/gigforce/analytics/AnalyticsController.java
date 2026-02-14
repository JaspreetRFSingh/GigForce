package com.gigforce.analytics;

import com.gigforce.analytics.dto.DashboardStats;
import com.gigforce.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Dashboard and revenue statistics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Full dashboard: contacts, deal pipeline, invoice revenue")
    public ApiResponse<DashboardStats> dashboard() {
        return ApiResponse.ok(analyticsService.getDashboardStats());
    }
}
