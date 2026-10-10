package com.financetracker.dto.request;

import com.financetracker.entity.enums.PaymentMethod;
import com.financetracker.entity.enums.TransactionCategory;
import com.financetracker.entity.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotNull(message = "Category is required")
    private TransactionCategory category;

    private String description;

    private PaymentMethod paymentMethod;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;
}
