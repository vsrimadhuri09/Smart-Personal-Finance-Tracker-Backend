package com.financetracker.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal totalInvestments;
    private BigDecimal currentBalance;
    private BigDecimal monthlySavings;
    private List<TransactionResponse> recentTransactions;
    private List<BudgetResponse> budgetAlerts;
    private List<String> savingsSuggestions;
}
