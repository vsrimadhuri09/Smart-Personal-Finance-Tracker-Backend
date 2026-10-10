package com.financetracker.controller;

import com.financetracker.dto.response.AnalyticsResponse;
import com.financetracker.entity.User;
import com.financetracker.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Analytics", description = "Analytics data")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly income vs expenses vs investments")
    public ResponseEntity<List<AnalyticsResponse.MonthlyData>> getMonthlyData(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getMonthlyData(user));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get expense breakdown by category")
    public ResponseEntity<List<AnalyticsResponse.CategoryData>> getCategoryData(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getCategoryData(user));
    }

    @GetMapping("/savings")
    @Operation(summary = "Get monthly savings trend")
    public ResponseEntity<List<AnalyticsResponse.SavingsData>> getSavingsData(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getSavingsData(user));
    }
}
