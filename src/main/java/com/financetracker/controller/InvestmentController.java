package com.financetracker.controller;

import com.financetracker.dto.request.InvestmentRequest;
import com.financetracker.dto.request.InvestmentSuggestionRequest;
import com.financetracker.dto.response.InvestmentResponse;
import com.financetracker.entity.User;
import com.financetracker.service.InvestmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Investments", description = "Investment portfolio management and suggestions")
public class InvestmentController {

    private final InvestmentService investmentService;

    @GetMapping
    @Operation(summary = "Get all investments for authenticated user")
    public ResponseEntity<List<InvestmentResponse>> getAllInvestments(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(investmentService.getAllInvestments(user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get investment by ID")
    public ResponseEntity<InvestmentResponse> getInvestmentById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(investmentService.getInvestmentById(user, id));
    }

    @PostMapping
    @Operation(summary = "Create an investment")
    public ResponseEntity<InvestmentResponse> createInvestment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody InvestmentRequest request) {
        return ResponseEntity.ok(investmentService.createInvestment(user, request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an investment")
    public ResponseEntity<InvestmentResponse> updateInvestment(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody InvestmentRequest request) {
        return ResponseEntity.ok(investmentService.updateInvestment(user, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an investment")
    public ResponseEntity<Void> deleteInvestment(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        investmentService.deleteInvestment(user, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Get personalized investment suggestions from the user's current finances")
    public ResponseEntity<List<String>> getInvestmentSuggestionsFromProfile(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "MODERATE") String riskLevel,
            @RequestParam(defaultValue = "WEALTH_CREATION") String investmentGoal) {
        return ResponseEntity.ok(
                investmentService.getInvestmentSuggestionsForUser(user, riskLevel, investmentGoal)
        );
    }

    @PostMapping("/suggestions")
    @Operation(summary = "Get personalized investment suggestions from custom parameters")
    public ResponseEntity<List<String>> getInvestmentSuggestions(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody InvestmentSuggestionRequest request) {
        return ResponseEntity.ok(investmentService.getInvestmentSuggestions(request));
    }
}
