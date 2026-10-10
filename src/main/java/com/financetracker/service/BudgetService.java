package com.financetracker.service;

import com.financetracker.dto.request.BudgetRequest;
import com.financetracker.dto.response.BudgetResponse;
import com.financetracker.entity.Budget;
import com.financetracker.entity.User;
import com.financetracker.exception.BadRequestException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.repository.BudgetRepository;
import com.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;

    public List<BudgetResponse> getAllBudgets(User user) {
        return budgetRepository.findByUserId(user.getId())
                .stream().map(b -> mapToResponse(b, user))
                .collect(Collectors.toList());
    }

    public List<BudgetResponse> getBudgetsByMonthYear(User user, int month, int year) {
        return budgetRepository.findByUserIdAndMonthAndYear(user.getId(), month, year)
                .stream().map(b -> mapToResponse(b, user))
                .collect(Collectors.toList());
    }

    public BudgetResponse getBudgetById(User user, Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied");
        }
        return mapToResponse(budget, user);
    }

    public BudgetResponse createBudget(User user, BudgetRequest request) {
        boolean exists = budgetRepository.findByUserIdAndCategoryAndMonthAndYear(
                user.getId(), request.getCategory(), request.getMonth(), request.getYear()).isPresent();
        if (exists) {
            throw new BadRequestException("Budget already exists for this category and month");
        }
        Budget budget = Budget.builder()
                .user(user)
                .category(request.getCategory())
                .budgetAmount(request.getBudgetAmount())
                .month(request.getMonth())
                .year(request.getYear())
                .build();
        return mapToResponse(budgetRepository.save(budget), user);
    }

    public BudgetResponse updateBudget(User user, Long id, BudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied");
        }
        budget.setBudgetAmount(request.getBudgetAmount());
        return mapToResponse(budgetRepository.save(budget), user);
    }

    public void deleteBudget(User user, Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied");
        }
        budgetRepository.delete(budget);
    }

    public List<BudgetResponse> getExceededBudgets(User user) {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        return getBudgetsByMonthYear(user, month, year)
                .stream().filter(BudgetResponse::isExceeded)
                .collect(Collectors.toList());
    }

    private BudgetResponse mapToResponse(Budget budget, User user) {
        BigDecimal spent = transactionRepository.sumExpenseByCategory(
                user.getId(), budget.getCategory(), budget.getMonth(), budget.getYear());
        if (spent == null) spent = BigDecimal.ZERO;
        BigDecimal remaining = budget.getBudgetAmount().subtract(spent);
        double percentage = 0;
        if (budget.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentage = spent.divide(budget.getBudgetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }
        return BudgetResponse.builder()
                .id(budget.getId())
                .category(budget.getCategory())
                .budgetAmount(budget.getBudgetAmount())
                .spentAmount(spent)
                .remainingAmount(remaining)
                .percentageUsed(percentage)
                .exceeded(spent.compareTo(budget.getBudgetAmount()) > 0)
                .month(budget.getMonth())
                .year(budget.getYear())
                .build();
    }
}
