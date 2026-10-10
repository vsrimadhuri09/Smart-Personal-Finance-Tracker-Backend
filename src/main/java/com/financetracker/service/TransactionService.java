package com.financetracker.service;

import com.financetracker.dto.request.TransactionRequest;
import com.financetracker.dto.response.TransactionResponse;
import com.financetracker.entity.Transaction;
import com.financetracker.entity.User;
import com.financetracker.entity.enums.TransactionCategory;
import com.financetracker.entity.enums.TransactionType;
import com.financetracker.exception.BadRequestException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public List<TransactionResponse> getAllTransactions(User user) {
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(user.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public TransactionResponse getTransactionById(User user, Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied");
        }
        return mapToResponse(transaction);
    }

    public TransactionResponse createTransaction(User user, TransactionRequest request) {
        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .description(request.getDescription())
                .paymentMethod(request.getPaymentMethod())
                .transactionDate(request.getTransactionDate())
                .build();
        return mapToResponse(transactionRepository.save(transaction));
    }

    public TransactionResponse updateTransaction(User user, Long id, TransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied");
        }
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setDescription(request.getDescription());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setTransactionDate(request.getTransactionDate());
        return mapToResponse(transactionRepository.save(transaction));
    }

    public void deleteTransaction(User user, Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied");
        }
        transactionRepository.delete(transaction);
    }

    public List<TransactionResponse> filterTransactions(User user, TransactionType type, TransactionCategory category, LocalDate from, LocalDate to, String keyword) {
        List<Transaction> transactions;
        if (keyword != null && !keyword.isBlank()) {
            transactions = transactionRepository.searchTransactions(user.getId(), keyword);
        } else if (from != null && to != null) {
            transactions = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(user.getId(), from, to);
        } else if (type != null) {
            transactions = transactionRepository.findByUserIdAndTypeOrderByTransactionDateDesc(user.getId(), type);
        } else if (category != null) {
            transactions = transactionRepository.findByUserIdAndCategoryOrderByTransactionDateDesc(user.getId(), category);
        } else {
            transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(user.getId());
        }
        return transactions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public BigDecimal getTotalByType(User user, TransactionType type) {
        return transactionRepository.sumAmountByUserIdAndType(user.getId(), type);
    }

    public BigDecimal getMonthlyTotalByType(User user, TransactionType type, int month, int year) {
        return transactionRepository.sumAmountByUserIdAndTypeAndMonthAndYear(user.getId(), type, month, year);
    }

    public BigDecimal getCategoryExpense(User user, TransactionCategory category, int month, int year) {
        return transactionRepository.sumExpenseByCategory(user.getId(), category, month, year);
    }

    public List<Transaction> getRecentTransactions(User user) {
        List<Transaction> all = transactionRepository.findByUserIdOrderByTransactionDateDesc(user.getId());
        return all.stream().limit(5).collect(Collectors.toList());
    }

    public TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .userId(t.getUser().getId())
                .amount(t.getAmount())
                .type(t.getType())
                .category(t.getCategory())
                .description(t.getDescription())
                .paymentMethod(t.getPaymentMethod())
                .transactionDate(t.getTransactionDate())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
