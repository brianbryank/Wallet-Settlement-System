package com.presta.Wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.presta.Wallet.entity.ReconciliationReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReconciliationReportRepository extends JpaRepository<ReconciliationReport, Long> {
    
    Optional<ReconciliationReport> findByReconciliationDate(LocalDate reconciliationDate);
    
    List<ReconciliationReport> findByReconciliationDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT r FROM ReconciliationReport r WHERE r.status = :status ORDER BY r.reconciliationDate DESC")
    List<ReconciliationReport> findByStatus(@Param("status") ReconciliationReport.ReconciliationStatus status);
    
    @Query("SELECT r FROM ReconciliationReport r ORDER BY r.reconciliationDate DESC")
    List<ReconciliationReport> findAllOrderByDateDesc();
    
    @Query("SELECT r FROM ReconciliationReport r WHERE r.reconciliationDate >= :fromDate ORDER BY r.reconciliationDate DESC")
    List<ReconciliationReport> findRecentReports(@Param("fromDate") LocalDate fromDate);
    
    boolean existsByReconciliationDate(LocalDate reconciliationDate);
    
    @Query("SELECT COUNT(r) FROM ReconciliationReport r WHERE r.status = 'COMPLETED' AND r.matchedTransactions = r.totalInternalTransactions")
    long countPerfectReconciliations();
}
