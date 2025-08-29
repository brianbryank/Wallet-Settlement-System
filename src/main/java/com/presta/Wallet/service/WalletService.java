package com.presta.Wallet.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.presta.Wallet.dto.BalanceResponse;
import com.presta.Wallet.dto.CreateWalletRequest;
import com.presta.Wallet.dto.WalletDTO;
import com.presta.Wallet.entity.Customer;
import com.presta.Wallet.entity.Wallet;
import com.presta.Wallet.exception.DuplicateResourceException;
import com.presta.Wallet.exception.WalletNotFoundException;
import com.presta.Wallet.mapper.WalletMapper;
import com.presta.Wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final CustomerService customerService;
    private final WalletMapper walletMapper;

    @Transactional
    public WalletDTO createWallet(CreateWalletRequest request) {
        log.info("Creating wallet for customer ID: {}", request.getCustomerId());
        
        Customer customer = customerService.getCustomerEntityById(request.getCustomerId());
        
        Wallet.WalletType walletType = Wallet.WalletType.valueOf(request.getWalletType().toUpperCase());
        
        if (walletRepository.existsByCustomerIdAndWalletType(customer.getId(), walletType)) {
            throw new DuplicateResourceException(
                String.format("Wallet of type %s already exists for customer %d", 
                            walletType, customer.getId()));
        }

        Wallet wallet = walletMapper.toEntity(request, customer);
        Wallet savedWallet = walletRepository.save(wallet);
        
        log.info("Wallet created successfully with ID: {}", savedWallet.getId());
        return walletMapper.toDTO(savedWallet);
    }

    @Transactional(readOnly = true)
    public WalletDTO getWalletById(Long walletId) {
        log.info("Fetching wallet with ID: {}", walletId);
        
        Wallet wallet = getWalletEntityById(walletId);
        return walletMapper.toDTO(wallet);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getWalletBalance(Long walletId) {
        log.info("Fetching balance for wallet ID: {}", walletId);
        
        Wallet wallet = getWalletEntityById(walletId);
        
        return BalanceResponse.builder()
                .walletId(wallet.getId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus().name())
                .build();
    }

    @Transactional(readOnly = true)
    public List<WalletDTO> getWalletsByCustomerId(Long customerId) {
        log.info("Fetching wallets for customer ID: {}", customerId);
        
        // check Verifyif the  customer exists
        customerService.getCustomerEntityById(customerId);
        
        List<Wallet> wallets = walletRepository.findByCustomerId(customerId);
        return wallets.stream()
                .map(walletMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Wallet getWalletEntityById(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    @Transactional
    public Wallet saveWallet(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public List<WalletDTO> getAllWallets() {
        log.info("Fetching all wallets");
        
        return walletRepository.findAll().stream()
                .map(walletMapper::toDTO)
                .collect(Collectors.toList());
    }
}