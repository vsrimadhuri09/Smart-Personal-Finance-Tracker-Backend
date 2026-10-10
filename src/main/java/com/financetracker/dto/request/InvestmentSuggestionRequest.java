package com.financetracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvestmentSuggestionRequest {
    @NotNull
    private BigDecimal monthlyIncome;

    @NotNull
    private BigDecimal monthlyExpenses;

    @NotNull
    private BigDecimal availableSavings;

    @NotBlank
    private String riskLevel; // LOW, MODERATE, HIGH

    @NotBlank
    private String investmentGoal; // e.g., "EMERGENCY_FUND", "WEALTH_CREATION", "RETIREMENT"
}
