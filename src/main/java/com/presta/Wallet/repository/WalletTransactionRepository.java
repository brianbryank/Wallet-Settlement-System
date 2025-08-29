package com.presta.Wallet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.presta.Wallet.entity.WalletTransaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    
    Optional<WalletTransaction> findByReferenceId(String referenceId);
    
    boolean existsByReferenceId(String referenceId);
    
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
    
    Page<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);
    
    @Query("SELECT t FROM WalletTransaction t WHERE t.wallet.id = :walletId " +
           "AND t.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.createdAt DESC")
    List<WalletTransaction> findByWalletIdAndDateRange(@Param("walletId") Long walletId,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM WalletTransaction t WHERE t.status = :status")
    List<WalletTransaction> findByStatus(@Param("status") WalletTransaction.TransactionStatus status);
    
    @Query("SELECT COUNT(t) FROM WalletTransaction t WHERE t.wallet.id = :walletId AND t.status = :status")
    long countByWalletIdAndStatus(@Param("walletId") Long walletId, 
                                 @Param("status") WalletTransaction.TransactionStatus status);
}