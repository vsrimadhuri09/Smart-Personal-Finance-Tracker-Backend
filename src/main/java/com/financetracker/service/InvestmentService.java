package com.financetracker.service;

import com.financetracker.dto.request.InvestmentRequest;
import com.financetracker.dto.request.InvestmentSuggestionRequest;
import com.financetracker.dto.response.InvestmentResponse;
import com.financetracker.entity.Investment;
import com.financetracker.entity.User;
import com.financetracker.entity.enums.TransactionType;
import com.financetracker.exception.BadRequestException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.repository.InvestmentRepository;
import com.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final TransactionRepository transactionRepository;

    public List<InvestmentResponse> getAllInvestments(User user) {
        return investmentRepository.findByUserIdOrderByInvestmentDateDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public InvestmentResponse getInvestmentById(User user, Long id) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));
        if (!investment.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied");
        }
        return mapToResponse(investment);
    }

    public InvestmentResponse createInvestment(User user, InvestmentRequest request) {
        Investment investment = Investment.builder()
                .user(user)
                .description(request.getName())
                .investmentType(request.getType())
                .amount(request.getInvestedAmount())
                .currentAmount(request.getCurrentAmount() != null ? request.getCurrentAmount() : request.getInvestedAmount())
                .investmentDate(request.getInvestmentDate() != null ? request.getInvestmentDate() : LocalDate.now())
                .build();
        return mapToResponse(investmentRepository.save(investment));
    }

    public InvestmentResponse updateInvestment(User user, Long id, InvestmentRequest request) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));
        if (!investment.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied");
        }
        investment.setDescription(request.getName());
        investment.setInvestmentType(request.getType());
        investment.setAmount(request.getInvestedAmount());
        investment.setCurrentAmount(request.getCurrentAmount() != null ? request.getCurrentAmount() : request.getInvestedAmount());
        if (request.getInvestmentDate() != null) {
            investment.setInvestmentDate(request.getInvestmentDate());
        }
        return mapToResponse(investmentRepository.save(investment));
    }

    public void deleteInvestment(User user, Long id) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));
        if (!investment.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied");
        }
        investmentRepository.delete(investment);
    }

    public BigDecimal getTotalInvested(User user) {
        BigDecimal total = investmentRepository.sumAmountByUserId(user.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalCurrentValue(User user) {
        BigDecimal total = investmentRepository.sumCurrentAmountByUserId(user.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<String> getInvestmentSuggestionsForUser(User user, String riskLevel, String investmentGoal) {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        BigDecimal income = transactionRepository.sumAmountByUserIdAndTypeAndMonthAndYear(
                user.getId(), TransactionType.INCOME, month, year);
        BigDecimal expenses = transactionRepository.sumAmountByUserIdAndTypeAndMonthAndYear(
                user.getId(), TransactionType.EXPENSE, month, year);
        if (income == null) income = BigDecimal.ZERO;
        if (expenses == null) expenses = BigDecimal.ZERO;

        InvestmentSuggestionRequest request = new InvestmentSuggestionRequest();
        request.setMonthlyIncome(income);
        request.setMonthlyExpenses(expenses);
        request.setAvailableSavings(getTotalInvested(user).add(income.subtract(expenses).max(BigDecimal.ZERO)));
        request.setRiskLevel(riskLevel != null ? riskLevel : "MODERATE");
        request.setInvestmentGoal(investmentGoal != null ? investmentGoal : "WEALTH_CREATION");
        return getInvestmentSuggestions(request);
    }

    public List<String> getInvestmentSuggestions(InvestmentSuggestionRequest request) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("📌 DISCLAIMER: These are educational suggestions only and not professional financial advice. Consult a certified financial advisor before investing.");
        suggestions.add("");

        BigDecimal income = request.getMonthlyIncome();
        BigDecimal expenses = request.getMonthlyExpenses();
        BigDecimal savings = request.getAvailableSavings();
        String riskLevel = request.getRiskLevel().toUpperCase();
        String goal = request.getInvestmentGoal().toUpperCase();

        BigDecimal monthlySurplus = income.subtract(expenses);
        BigDecimal emergencyFund = income.multiply(BigDecimal.valueOf(6));

        // Emergency fund first
        if (savings.compareTo(emergencyFund) < 0) {
            suggestions.add("🆘 Emergency Fund First: Build an emergency fund of at least 6 months of expenses (" + emergencyFund + "). Keep this in a liquid savings account or FD.");
        }

        if (riskLevel.equals("LOW")) {
            suggestions.add("🏦 Fixed Deposits (FD): Safe, guaranteed returns of 5-7% annually. Ideal for low-risk investors. Split across multiple banks for safety.");
            suggestions.add("📮 Public Provident Fund (PPF): Government-backed, tax-free returns. Lock-in 15 years. Good for long-term goals.");
            suggestions.add("🏛️ Government Bonds: Safe investment with fixed interest. Suitable if you want steady income.");
            suggestions.add("💰 Recurring Deposits (RD): Invest a fixed amount monthly. Great for building a savings habit.");
        } else if (riskLevel.equals("MODERATE")) {
            suggestions.add("📈 Balanced Mutual Funds: Mix of equity and debt. Moderate risk with potential 10-12% annual returns. Invest via SIP.");
            suggestions.add("🥇 Gold ETFs: Hedge against inflation. Allocate 5-10% of portfolio to gold.");
            suggestions.add("🏦 Fixed Deposits: Keep 30% in FDs for stability while investing rest in mutual funds.");
            suggestions.add("📊 Index Funds: Low-cost, diversified equity investment tracking Nifty 50 or Sensex.");
            suggestions.add("🏠 REITs: Real Estate Investment Trusts for real estate exposure without buying property.");
        } else if (riskLevel.equals("HIGH")) {
            suggestions.add("📊 Equity Mutual Funds: High-risk, high-reward. Potential returns of 12-18% over long term. Invest via monthly SIP.");
            suggestions.add("📉 Direct Stock Market: Research and invest in fundamentally strong companies. Diversify across sectors.");
            suggestions.add("🌍 International Funds: Geographic diversification by investing in global markets.");
            suggestions.add("🚀 Small Cap Funds: Higher risk but potentially higher returns over 7+ year horizon.");
        }

        // Goal-specific advice
        if (goal.contains("EMERGENCY")) {
            suggestions.add("✅ Goal Strategy: Focus on liquid instruments — savings account, liquid mutual funds, or short-term FDs.");
        } else if (goal.contains("WEALTH")) {
            suggestions.add("✅ Goal Strategy: SIP in equity mutual funds for 5+ years. Compounding works best with time. Start now!");
        } else if (goal.contains("RETIREMENT")) {
            suggestions.add("✅ Goal Strategy: NPS (National Pension System) + PPF + Equity SIP combination for long-term retirement planning.");
        } else if (goal.contains("HOME") || goal.contains("HOUSE")) {
            suggestions.add("✅ Goal Strategy: Save down payment in FD/RD. Check home loan eligibility. Keep EMI below 40% of monthly income.");
        } else if (goal.contains("EDUCATION")) {
            suggestions.add("✅ Goal Strategy: Sukanya Samriddhi (for girl child), ELSS mutual funds, or education-specific SIPs.");
        }

        if (monthlySurplus.compareTo(BigDecimal.ZERO) > 0) {
            suggestions.add("💡 Suggested Monthly Investment: You can invest approximately " + monthlySurplus.multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP) + " per month (50% of your monthly surplus).");
        }

        return suggestions;
    }

    private InvestmentResponse mapToResponse(Investment inv) {
        BigDecimal invested = inv.getAmount() != null ? inv.getAmount() : BigDecimal.ZERO;
        BigDecimal current = inv.getCurrentAmount() != null ? inv.getCurrentAmount() : invested;
        BigDecimal profitLoss = current.subtract(invested);
        double returnPercentage = 0.0;
        if (invested.compareTo(BigDecimal.ZERO) > 0) {
            returnPercentage = profitLoss.divide(invested, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        return InvestmentResponse.builder()
                .id(inv.getId())
                .name(inv.getDescription() != null ? inv.getDescription() : inv.getInvestmentType())
                .type(inv.getInvestmentType())
                .investedAmount(invested)
                .currentAmount(current)
                .profitLoss(profitLoss)
                .returnPercentage(returnPercentage)
                .investmentDate(inv.getInvestmentDate())
                .createdAt(inv.getCreatedAt())
                .build();
    }
}
