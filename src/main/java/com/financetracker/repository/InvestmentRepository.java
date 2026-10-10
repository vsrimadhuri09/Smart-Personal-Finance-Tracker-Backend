package com.financetracker.repository;

import com.financetracker.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Long> {
    List<Investment> findByUserIdOrderByInvestmentDateDesc(Long userId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Investment i WHERE i.user.id = :userId")
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(COALESCE(i.currentAmount, i.amount)), 0) FROM Investment i WHERE i.user.id = :userId")
    BigDecimal sumCurrentAmountByUserId(@Param("userId") Long userId);
}
