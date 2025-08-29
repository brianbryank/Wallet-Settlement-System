package com.presta.Wallet.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.presta.Wallet.entity.Wallet;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    List<Wallet> findByCustomerId(Long customerId);
    
    @Query("SELECT w FROM Wallet w WHERE w.customer.id = :customerId AND w.status = :status")
    List<Wallet> findByCustomerIdAndStatus(@Param("customerId") Long customerId, 
                                          @Param("status") Wallet.WalletStatus status);
    
    @Query("SELECT w FROM Wallet w WHERE w.customer.id = :customerId AND w.walletType = :type")
    Optional<Wallet> findByCustomerIdAndWalletType(@Param("customerId") Long customerId, 
                                                   @Param("type") Wallet.WalletType type);
    
    boolean existsByCustomerIdAndWalletType(Long customerId, Wallet.WalletType walletType);
}