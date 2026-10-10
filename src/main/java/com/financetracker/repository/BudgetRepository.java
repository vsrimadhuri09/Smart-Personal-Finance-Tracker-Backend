package com.financetracker.repository;

import com.financetracker.entity.Budget;
import com.financetracker.entity.enums.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserIdAndMonthAndYear(Long userId, int month, int year);
    List<Budget> findByUserId(Long userId);
    Optional<Budget> findByUserIdAndCategoryAndMonthAndYear(Long userId, TransactionCategory category, int month, int year);
}
