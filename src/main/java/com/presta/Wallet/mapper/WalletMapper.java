package com.presta.Wallet.mapper;


import org.springframework.stereotype.Component;

import com.presta.Wallet.dto.CreateWalletRequest;
import com.presta.Wallet.dto.WalletDTO;
import com.presta.Wallet.entity.Customer;
import com.presta.Wallet.entity.Wallet;

@Component
public class WalletMapper {

    public WalletDTO toDTO(Wallet wallet) {
        if (wallet == null) {
            return null;
        }
        
        return WalletDTO.builder()
                .id(wallet.getId())
                .customerId(wallet.getCustomer().getId())
                .customerName(wallet.getCustomer().getName())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus().name())
                .walletType(wallet.getWalletType().name())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    public Wallet toEntity(CreateWalletRequest request, Customer customer) {
        if (request == null || customer == null) {
            return null;
        }
        
        return Wallet.builder()
                .customer(customer)
                .currency(request.getCurrency())
                .walletType(Wallet.WalletType.valueOf(request.getWalletType().toUpperCase()))
                .status(Wallet.WalletStatus.ACTIVE)
                .build();
    }
}
