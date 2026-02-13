package com.gigforce.notification;

import com.gigforce.common.ApiResponse;
import com.gigforce.multitenancy.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app event log")
public class NotificationController {

    private final NotificationRepository repo;

    @GetMapping
    @Operation(summary = "Last 20 notifications for this tenant")
    public ApiResponse<List<NotificationLog>> recent() {
        return ApiResponse.ok(repo.findTop20ByTenantIdOrderByCreatedAtDesc(TenantContext.get()));
    }
}
