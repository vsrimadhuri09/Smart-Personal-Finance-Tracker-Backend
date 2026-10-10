package com.financetracker.service;

import com.financetracker.dto.response.AnalyticsResponse;
import com.financetracker.entity.User;
import com.financetracker.entity.enums.TransactionType;
import com.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;

    public List<AnalyticsResponse.MonthlyData> getMonthlyData(User user) {
        List<Object[]> incomeData = transactionRepository.getMonthlyTotalByType(user.getId(), TransactionType.INCOME);
        List<Object[]> expenseData = transactionRepository.getMonthlyTotalByType(user.getId(), TransactionType.EXPENSE);
        List<Object[]> investmentData = transactionRepository.getMonthlyTotalByType(user.getId(), TransactionType.INVESTMENT);

        // Build a map of month-year -> values
        java.util.Map<String, BigDecimal[]> map = new java.util.LinkedHashMap<>();

        for (Object[] row : incomeData) {
            String key = row[0] + "-" + row[1];
            map.computeIfAbsent(key, k -> new BigDecimal[3]);
            map.get(key)[0] = (BigDecimal) row[2];
        }
        for (Object[] row : expenseData) {
            String key = row[0] + "-" + row[1];
            map.computeIfAbsent(key, k -> new BigDecimal[3]);
            map.get(key)[1] = (BigDecimal) row[2];
        }
        for (Object[] row : investmentData) {
            String key = row[0] + "-" + row[1];
            map.computeIfAbsent(key, k -> new BigDecimal[3]);
            map.get(key)[2] = (BigDecimal) row[2];
        }

        List<AnalyticsResponse.MonthlyData> result = new ArrayList<>();
        for (java.util.Map.Entry<String, BigDecimal[]> entry : map.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int monthNum = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);
            BigDecimal[] vals = entry.getValue();
            result.add(AnalyticsResponse.MonthlyData.builder()
                    .month(Month.of(monthNum).name().substring(0, 3) + " " + year)
                    .income(vals[0] != null ? vals[0] : BigDecimal.ZERO)
                    .expenses(vals[1] != null ? vals[1] : BigDecimal.ZERO)
                    .investments(vals[2] != null ? vals[2] : BigDecimal.ZERO)
                    .build());
        }
        return result;
    }

    public List<AnalyticsResponse.CategoryData> getCategoryData(User user) {
        List<Object[]> data = transactionRepository.getExpenseByCategory(user.getId());
        List<AnalyticsResponse.CategoryData> result = new ArrayList<>();
        for (Object[] row : data) {
            result.add(AnalyticsResponse.CategoryData.builder()
                    .category(row[0].toString())
                    .amount((BigDecimal) row[1])
                    .build());
        }
        return result;
    }

    public List<AnalyticsResponse.SavingsData> getSavingsData(User user) {
        List<Object[]> incomeData = transactionRepository.getMonthlyTotalByType(user.getId(), TransactionType.INCOME);
        List<Object[]> expenseData = transactionRepository.getMonthlyTotalByType(user.getId(), TransactionType.EXPENSE);

        java.util.Map<String, BigDecimal[]> map = new java.util.LinkedHashMap<>();
        for (Object[] row : incomeData) {
            String key = row[0] + "-" + row[1];
            map.computeIfAbsent(key, k -> new BigDecimal[2]);
            map.get(key)[0] = (BigDecimal) row[2];
        }
        for (Object[] row : expenseData) {
            String key = row[0] + "-" + row[1];
            map.computeIfAbsent(key, k -> new BigDecimal[2]);
            map.get(key)[1] = (BigDecimal) row[2];
        }

        List<AnalyticsResponse.SavingsData> result = new ArrayList<>();
        for (java.util.Map.Entry<String, BigDecimal[]> entry : map.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int monthNum = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);
            BigDecimal income = entry.getValue()[0] != null ? entry.getValue()[0] : BigDecimal.ZERO;
            BigDecimal expense = entry.getValue()[1] != null ? entry.getValue()[1] : BigDecimal.ZERO;
            result.add(AnalyticsResponse.SavingsData.builder()
                    .month(Month.of(monthNum).name().substring(0, 3) + " " + year)
                    .savings(income.subtract(expense))
                    .build());
        }
        return result;
    }
}
