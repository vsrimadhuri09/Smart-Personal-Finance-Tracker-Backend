package com.financetracker.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvestmentRequest {
    @NotBlank(message = "Investment name/description is required")
    private String name;

    @NotBlank(message = "Investment type is required")
    private String type;

    @NotNull(message = "Invested amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal investedAmount;

    @DecimalMin(value = "0.00", message = "Current amount cannot be negative")
    private BigDecimal currentAmount;

    private LocalDate investmentDate;
}
