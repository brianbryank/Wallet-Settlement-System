package com.presta.Wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.presta.Wallet.entity.ExternalTransaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExternalTransactionRepository extends JpaRepository<ExternalTransaction, Long> {
    
    List<ExternalTransaction> findByTransactionDate(LocalDate transactionDate);
    
    List<ExternalTransaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);
    
    Optional<ExternalTransaction> findByReferenceId(String referenceId);
    
    List<ExternalTransaction> findByReferenceIdAndTransactionDate(String referenceId, LocalDate transactionDate);
    
    @Query("SELECT e FROM ExternalTransaction e WHERE e.transactionDate = :date AND e.status = :status")
    List<ExternalTransaction> findByTransactionDateAndStatus(@Param("date") LocalDate date, 
                                                           @Param("status") ExternalTransaction.ProcessingStatus status);
    
    @Query("SELECT COUNT(e) FROM ExternalTransaction e WHERE e.transactionDate = :date")
    long countByTransactionDate(@Param("date") LocalDate date);
    
    @Query("SELECT SUM(e.amount) FROM ExternalTransaction e WHERE e.transactionDate = :date")
    java.math.BigDecimal sumAmountByTransactionDate(@Param("date") LocalDate date);
    
    @Query("SELECT e FROM ExternalTransaction e WHERE e.fileName = :fileName")
    List<ExternalTransaction> findByFileName(@Param("fileName") String fileName);
    
    boolean existsByReferenceIdAndTransactionDate(String referenceId, LocalDate transactionDate);
    
    @Query("SELECT DISTINCT e.transactionDate FROM ExternalTransaction e ORDER BY e.transactionDate DESC")
    List<LocalDate> findDistinctTransactionDates();
}
