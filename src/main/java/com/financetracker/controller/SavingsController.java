package com.financetracker.controller;

import com.financetracker.dto.request.SavingsGoalRequest;
import com.financetracker.dto.response.SavingsGoalResponse;
import com.financetracker.entity.User;
import com.financetracker.service.SavingsGoalService;
import com.financetracker.service.SavingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Savings", description = "Savings goals and recommendations")
public class SavingsController {

    private final SavingsService savingsService;
    private final SavingsGoalService savingsGoalService;

    @GetMapping
    @Operation(summary = "Get all savings goals for authenticated user")
    public ResponseEntity<List<SavingsGoalResponse>> getAllSavingsGoals(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(savingsGoalService.getAllSavingsGoals(user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get savings goal by ID")
    public ResponseEntity<SavingsGoalResponse> getSavingsGoalById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(savingsGoalService.getSavingsGoalById(user, id));
    }

    @PostMapping
    @Operation(summary = "Create a new savings goal")
    public ResponseEntity<SavingsGoalResponse> createSavingsGoal(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SavingsGoalRequest request) {
        return ResponseEntity.ok(savingsGoalService.createSavingsGoal(user, request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a savings goal")
    public ResponseEntity<SavingsGoalResponse> updateSavingsGoal(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody SavingsGoalRequest request) {
        return ResponseEntity.ok(savingsGoalService.updateSavingsGoal(user, id, request));
    }

    @PatchMapping("/{id}/amount")
    @Operation(summary = "Update saved amount for a goal")
    public ResponseEntity<SavingsGoalResponse> updateSavedAmount(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody Map<String, BigDecimal> payload) {
        BigDecimal amount = payload.get("currentAmount");
        return ResponseEntity.ok(savingsGoalService.updateCurrentAmount(user, id, amount));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a savings goal")
    public ResponseEntity<Void> deleteSavingsGoal(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        savingsGoalService.deleteSavingsGoal(user, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Get personalized savings suggestions")
    public ResponseEntity<List<String>> getSavingsSuggestions(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(savingsService.getSavingsSuggestions(user));
    }
}
