package com.presta.Wallet.controller;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.presta.Wallet.dto.ApiResponse;
import com.presta.Wallet.dto.BalanceResponse;
import com.presta.Wallet.dto.CreateWalletRequest;
import com.presta.Wallet.dto.WalletDTO;
import com.presta.Wallet.service.WalletService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<ApiResponse<WalletDTO>> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        log.info("Received request to create wallet for customer: {}", request.getCustomerId());
        
        WalletDTO wallet = walletService.createWallet(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wallet created successfully", wallet));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<ApiResponse<WalletDTO>> getWallet(@PathVariable Long walletId) {
        log.info("Received request to fetch wallet with ID: {}", walletId);
        
        WalletDTO wallet = walletService.getWalletById(walletId);
        return ResponseEntity.ok(ApiResponse.success(wallet));
    }

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getWalletBalance(@PathVariable Long walletId) {
        log.info("Received request to fetch balance for wallet ID: {}", walletId);
        
        BalanceResponse balance = walletService.getWalletBalance(walletId);
        return ResponseEntity.ok(ApiResponse.success(balance));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WalletDTO>>> getAllWallets() {
        log.info("Received request to fetch all wallets");
        
        List<WalletDTO> wallets = walletService.getAllWallets();
        return ResponseEntity.ok(ApiResponse.success(wallets));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<WalletDTO>>> getWalletsByCustomer(@PathVariable Long customerId) {
        log.info("Received request to fetch wallets for customer ID: {}", customerId);
        
        List<WalletDTO> wallets = walletService.getWalletsByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(wallets));
    }
}
