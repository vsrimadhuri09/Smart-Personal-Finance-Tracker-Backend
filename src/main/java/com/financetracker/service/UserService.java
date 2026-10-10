package com.financetracker.service;

import com.financetracker.dto.request.ChangePasswordRequest;
import com.financetracker.dto.request.UpdateProfileRequest;
import com.financetracker.dto.response.DashboardResponse;
import com.financetracker.dto.response.UserResponse;
import com.financetracker.entity.User;
import com.financetracker.entity.enums.TransactionType;
import com.financetracker.exception.BadRequestException;
import com.financetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final SavingsService savingsService;
    private final InvestmentService investmentService;

    public UserResponse getProfile(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public UserResponse updateProfile(User user, UpdateProfileRequest request) {
        user.setName(request.getName());
        userRepository.save(user);
        return getProfile(user);
    }

    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public DashboardResponse getDashboard(User user) {
        BigDecimal totalIncome = transactionService.getTotalByType(user, TransactionType.INCOME);
        BigDecimal totalExpenses = transactionService.getTotalByType(user, TransactionType.EXPENSE);
        BigDecimal totalInvestments = transactionService.getTotalByType(user, TransactionType.INVESTMENT);

        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        BigDecimal monthlyIncome = transactionService.getMonthlyTotalByType(user, TransactionType.INCOME, month, year);
        BigDecimal monthlyExpenses = transactionService.getMonthlyTotalByType(user, TransactionType.EXPENSE, month, year);

        BigDecimal balance = totalIncome.subtract(totalExpenses).subtract(totalInvestments);
        BigDecimal monthlySavings = monthlyIncome.subtract(monthlyExpenses);

        return DashboardResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .totalInvestments(totalInvestments)
                .currentBalance(balance)
                .monthlySavings(monthlySavings)
                .recentTransactions(
                        transactionService.getRecentTransactions(user)
                                .stream().map(transactionService::mapToResponse)
                                .collect(Collectors.toList())
                )
                .budgetAlerts(budgetService.getExceededBudgets(user))
                .savingsSuggestions(savingsService.getSavingsSuggestions(user))
                .build();
    }
}
