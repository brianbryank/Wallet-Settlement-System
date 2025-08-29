package com.presta.Wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.presta.Wallet.entity.ReconciliationItem;

import java.util.List;

@Repository
public interface ReconciliationItemRepository extends JpaRepository<ReconciliationItem, Long> {
    
    List<ReconciliationItem> findByReconciliationReportId(Long reportId);
    
    @Query("SELECT ri FROM ReconciliationItem ri WHERE ri.reconciliationReport.id = :reportId AND ri.matchType = :matchType")
    List<ReconciliationItem> findByReconciliationReportIdAndMatchType(@Param("reportId") Long reportId, 
                                                                     @Param("matchType") ReconciliationItem.MatchType matchType);
    
    @Query("SELECT ri FROM ReconciliationItem ri WHERE ri.reconciliationReport.id = :reportId AND ri.discrepancyType = :discrepancyType")
    List<ReconciliationItem> findByReconciliationReportIdAndDiscrepancyType(@Param("reportId") Long reportId, 
                                                                           @Param("discrepancyType") ReconciliationItem.DiscrepancyType discrepancyType);
    
    @Query("SELECT ri FROM ReconciliationItem ri WHERE ri.reconciliationReport.id = :reportId AND ri.discrepancyType != 'NONE'")
    List<ReconciliationItem> findDiscrepanciesByReportId(@Param("reportId") Long reportId);
    
    @Query("SELECT COUNT(ri) FROM ReconciliationItem ri WHERE ri.reconciliationReport.id = :reportId AND ri.matchType = 'PERFECT_MATCH'")
    long countPerfectMatchesByReportId(@Param("reportId") Long reportId);
    
    @Query("SELECT COUNT(ri) FROM ReconciliationItem ri WHERE ri.reconciliationReport.id = :reportId AND ri.discrepancyType != 'NONE'")
    long countDiscrepanciesByReportId(@Param("reportId") Long reportId);
}