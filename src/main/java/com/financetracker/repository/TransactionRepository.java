package com.financetracker.repository;

import com.financetracker.entity.Transaction;
import com.financetracker.entity.enums.TransactionCategory;
import com.financetracker.entity.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    List<Transaction> findByUserIdAndTypeOrderByTransactionDateDesc(Long userId, TransactionType type);

    List<Transaction> findByUserIdAndCategoryOrderByTransactionDateDesc(Long userId, TransactionCategory category);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type")
    BigDecimal sumAmountByUserIdAndType(@Param("userId") Long userId, @Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND MONTH(t.transactionDate) = :month AND YEAR(t.transactionDate) = :year")
    BigDecimal sumAmountByUserIdAndTypeAndMonthAndYear(@Param("userId") Long userId, @Param("type") TransactionType type, @Param("month") int month, @Param("year") int year);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.category = :category AND t.type = 'EXPENSE' AND MONTH(t.transactionDate) = :month AND YEAR(t.transactionDate) = :year")
    BigDecimal sumExpenseByCategory(@Param("userId") Long userId, @Param("category") TransactionCategory category, @Param("month") int month, @Param("year") int year);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND (LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR CAST(t.amount AS string) LIKE CONCAT('%', :keyword, '%'))")
    List<Transaction> searchTransactions(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Query("SELECT MONTH(t.transactionDate) as month, YEAR(t.transactionDate) as year, SUM(t.amount) as total FROM Transaction t WHERE t.user.id = :userId AND t.type = :type GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate)")
    List<Object[]> getMonthlyTotalByType(@Param("userId") Long userId, @Param("type") TransactionType type);

    @Query("SELECT t.category as category, SUM(t.amount) as total FROM Transaction t WHERE t.user.id = :userId AND t.type = 'EXPENSE' GROUP BY t.category")
    List<Object[]> getExpenseByCategory(@Param("userId") Long userId);

    List<Transaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(Long userId, LocalDate from, LocalDate to);
}
