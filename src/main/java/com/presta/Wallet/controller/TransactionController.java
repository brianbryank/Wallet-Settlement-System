package com.presta.Wallet.controller;




import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.presta.Wallet.dto.ApiResponse;
import com.presta.Wallet.dto.ConsumeRequest;
import com.presta.Wallet.dto.ServiceConsumeRequest;
import com.presta.Wallet.dto.ServiceInfo;
import com.presta.Wallet.dto.ServiceInfoResponse;
import com.presta.Wallet.dto.ServiceResponse;
import com.presta.Wallet.dto.TopupRequest;
import com.presta.Wallet.dto.TransactionHistoryRequest;
import com.presta.Wallet.dto.TransactionResponse;
import com.presta.Wallet.service.TransactionService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/{walletId}/topup")
    public ResponseEntity<ApiResponse<TransactionResponse>> topup(
            @PathVariable Long walletId,
            @Valid @RequestBody TopupRequest request) {
        
        log.info("Received top-up request for wallet: {}, amount: {}", walletId, request.getAmount());
        
        TransactionResponse response = transactionService.topup(walletId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Top-up completed successfully", response));
    }

    @PostMapping("/{walletId}/consume")
    public ResponseEntity<ApiResponse<TransactionResponse>> consume(
            @PathVariable Long walletId,
            @Valid @RequestBody ConsumeRequest request) {
        
        log.info("Received consumption request for wallet: {}, amount: {}, service: {}", 
                walletId, request.getAmount(), request.getServiceType());
        
        TransactionResponse response = transactionService.consume(walletId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Consumption completed successfully", response));
    }

    @PostMapping("/{walletId}/consume-service")
    public ResponseEntity<ApiResponse<ServiceResponse>> consumeService(
            @PathVariable Long walletId,
            @Valid @RequestBody ServiceConsumeRequest request) {
        
        log.info("Received service consumption request for wallet: {}, service: {}", 
                walletId, request.getServiceType());
        
        ServiceResponse response = transactionService.consumeService(walletId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service consumption completed", response));
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionHistory(
            @PathVariable Long walletId,
            @ModelAttribute TransactionHistoryRequest request) {
        
        log.info("Received transaction history request for wallet: {}", walletId);
        
        List<TransactionResponse> transactions = transactionService.getTransactionHistory(walletId, request);
        
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/transactions/reference/{referenceId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByReference(
            @PathVariable String referenceId) {
        
        log.info("Received transaction lookup request for reference: {}", referenceId);
        
        TransactionResponse transaction = transactionService.getTransactionByReference(referenceId);
        
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    // Utility endpoints for service information
    @GetMapping("/services")
    public ResponseEntity<ApiResponse<ServiceInfoResponse>> getServiceInfo() {
        log.info("Received service info request");
        
        ServiceInfoResponse serviceInfo = ServiceInfoResponse.builder()
                .services(List.of(
                    ServiceInfo.builder()
                        .serviceType("CRB")
                        .cost(50.00)
                        .description("Credit Reference Bureau check")
                        .estimatedDuration("1-3 seconds")
                        .build(),
                    ServiceInfo.builder()
                        .serviceType("KYC")
                        .cost(25.00)
                        .description("Know Your Customer verification")
                        .estimatedDuration("1-2 seconds")
                        .build(),
                    ServiceInfo.builder()
                        .serviceType("CREDIT_SCORING")
                        .cost(75.00)
                        .description("Credit score calculation")
                        .estimatedDuration("2-5 seconds")
                        .build()
                ))
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(serviceInfo));
    }
}








