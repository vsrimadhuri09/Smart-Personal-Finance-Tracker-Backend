package com.financetracker.dto.response;

import com.financetracker.entity.enums.PaymentMethod;
import com.financetracker.entity.enums.TransactionCategory;
import com.financetracker.entity.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionCategory category;
    private String description;
    private PaymentMethod paymentMethod;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;
}
