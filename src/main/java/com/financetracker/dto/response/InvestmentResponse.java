package com.financetracker.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class InvestmentResponse {
    private Long id;
    private String name;
    private String type;
    private BigDecimal investedAmount;
    private BigDecimal currentAmount;
    private BigDecimal profitLoss;
    private Double returnPercentage;
    private LocalDate investmentDate;
    private LocalDateTime createdAt;
}
