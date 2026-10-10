package com.financetracker.service;

import com.financetracker.dto.request.SavingsGoalRequest;
import com.financetracker.dto.response.SavingsGoalResponse;
import com.financetracker.entity.SavingsGoal;
import com.financetracker.entity.User;
import com.financetracker.exception.BadRequestException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.repository.SavingsGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;

    public List<SavingsGoalResponse> getAllSavingsGoals(User user) {
        return savingsGoalRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SavingsGoalResponse getSavingsGoalById(User user, Long id) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied");
        }
        return mapToResponse(goal);
    }

    public SavingsGoalResponse createSavingsGoal(User user, SavingsGoalRequest request) {
        SavingsGoal goal = SavingsGoal.builder()
                .user(user)
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .currentAmount(request.getCurrentAmount() != null ? request.getCurrentAmount() : BigDecimal.ZERO)
                .targetDate(request.getTargetDate())
                .build();
        return mapToResponse(savingsGoalRepository.save(goal));
    }

    public SavingsGoalResponse updateSavingsGoal(User user, Long id, SavingsGoalRequest request) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied");
        }
        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        if (request.getCurrentAmount() != null) {
            goal.setCurrentAmount(request.getCurrentAmount());
        }
        goal.setTargetDate(request.getTargetDate());
        return mapToResponse(savingsGoalRepository.save(goal));
    }

    public SavingsGoalResponse updateCurrentAmount(User user, Long id, BigDecimal currentAmount) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied");
        }
        goal.setCurrentAmount(currentAmount != null ? currentAmount : BigDecimal.ZERO);
        return mapToResponse(savingsGoalRepository.save(goal));
    }

    public void deleteSavingsGoal(User user, Long id) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied");
        }
        savingsGoalRepository.delete(goal);
    }

    private SavingsGoalResponse mapToResponse(SavingsGoal goal) {
        BigDecimal target = goal.getTargetAmount() != null ? goal.getTargetAmount() : BigDecimal.ZERO;
        BigDecimal current = goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO;
        double progress = 0.0;
        if (target.compareTo(BigDecimal.ZERO) > 0) {
            progress = current.divide(target, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }
        return SavingsGoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(target)
                .currentAmount(current)
                .progressPercentage(progress)
                .targetDate(goal.getTargetDate())
                .createdAt(goal.getCreatedAt())
                .build();
    }
}
