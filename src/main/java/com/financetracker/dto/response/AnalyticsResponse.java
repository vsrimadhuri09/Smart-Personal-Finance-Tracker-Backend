package com.financetracker.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalyticsResponse {
    private List<MonthlyData> monthlyData;
    private List<CategoryData> categoryData;
    private List<SavingsData> savingsData;

    @Data
    @Builder
    public static class MonthlyData {
        private String month;
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal investments;
    }

    @Data
    @Builder
    public static class CategoryData {
        private String category;
        private BigDecimal amount;
    }

    @Data
    @Builder
    public static class SavingsData {
        private String month;
        private BigDecimal savings;
    }
}
