package com.financetracker.service;

import com.financetracker.entity.User;
import com.financetracker.entity.enums.TransactionCategory;
import com.financetracker.entity.enums.TransactionType;
import com.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingsService {

    private final TransactionRepository transactionRepository;

    public List<String> getSavingsSuggestions(User user) {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();

        BigDecimal income = transactionRepository.sumAmountByUserIdAndTypeAndMonthAndYear(
                user.getId(), TransactionType.INCOME, month, year);
        BigDecimal expenses = transactionRepository.sumAmountByUserIdAndTypeAndMonthAndYear(
                user.getId(), TransactionType.EXPENSE, month, year);
        BigDecimal foodExpense = transactionRepository.sumExpenseByCategory(
                user.getId(), TransactionCategory.FOOD, month, year);
        BigDecimal entertainmentExpense = transactionRepository.sumExpenseByCategory(
                user.getId(), TransactionCategory.ENTERTAINMENT, month, year);
        BigDecimal shoppingExpense = transactionRepository.sumExpenseByCategory(
                user.getId(), TransactionCategory.SHOPPING, month, year);

        if (income == null) income = BigDecimal.ZERO;
        if (expenses == null) expenses = BigDecimal.ZERO;
        if (foodExpense == null) foodExpense = BigDecimal.ZERO;
        if (entertainmentExpense == null) entertainmentExpense = BigDecimal.ZERO;
        if (shoppingExpense == null) shoppingExpense = BigDecimal.ZERO;

        List<String> suggestions = new ArrayList<>();

        if (income.compareTo(BigDecimal.ZERO) == 0) {
            suggestions.add("No income recorded this month. Add your income transactions to get personalized suggestions.");
            return suggestions;
        }

        // Rule 1: Expenses exceed income
        if (expenses.compareTo(income) > 0) {
            suggestions.add("⚠️ Warning: Your expenses (" + expenses + ") exceed your income (" + income + ") this month. Immediate action required!");
        }

        // Rule 2: Savings rate below 20%
        BigDecimal savings = income.subtract(expenses);
        BigDecimal savingsRate = savings.divide(income, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        if (savingsRate.compareTo(BigDecimal.valueOf(20)) < 0) {
            suggestions.add("💡 Your savings rate is " + savingsRate.setScale(1, RoundingMode.HALF_UP) + "%. Aim to save at least 20% of your income. Try reducing discretionary spending.");
        }

        // Rule 3: Food > 20% of income
        BigDecimal foodPercent = foodExpense.divide(income, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        if (foodPercent.compareTo(BigDecimal.valueOf(20)) > 0) {
            suggestions.add("🍔 Food expenses are " + foodPercent.setScale(1, RoundingMode.HALF_UP) + "% of your income. Consider meal planning and cooking at home to reduce food costs.");
        }

        // Rule 4: Entertainment > 10% of income
        BigDecimal entertainmentPercent = entertainmentExpense.divide(income, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        if (entertainmentPercent.compareTo(BigDecimal.valueOf(10)) > 0) {
            suggestions.add("🎬 Entertainment expenses are " + entertainmentPercent.setScale(1, RoundingMode.HALF_UP) + "% of your income. Consider cutting back on entertainment to save more.");
        }

        // Rule 5: Shopping > 15% of income
        BigDecimal shoppingPercent = shoppingExpense.divide(income, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        if (shoppingPercent.compareTo(BigDecimal.valueOf(15)) > 0) {
            suggestions.add("🛍️ Shopping expenses are " + shoppingPercent.setScale(1, RoundingMode.HALF_UP) + "% of your income. Make a shopping list and avoid impulse purchases.");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("✅ Great job! You are managing your finances well this month. Keep maintaining your savings rate above 20%.");
        }

        return suggestions;
    }
}
