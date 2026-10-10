package com.financetracker.dto.response;

import com.financetracker.entity.enums.TransactionCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BudgetResponse {
    private Long id;
    private TransactionCategory category;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private Double percentageUsed;
    private boolean exceeded;
    private Integer month;
    private Integer year;
}
