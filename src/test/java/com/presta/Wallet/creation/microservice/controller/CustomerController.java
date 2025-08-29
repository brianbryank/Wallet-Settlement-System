package com.presta.Wallet.creation.microservice.controller;


import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.presta.Wallet.dto.ApiResponse;
import com.presta.Wallet.dto.CreateCustomerRequest;
import com.presta.Wallet.dto.CustomerDTO;
import com.presta.Wallet.service.CustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDTO>> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        log.info("Received request to create customer: {}", request.getEmail());
        
        CustomerDTO customer = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully", customer));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomer(@PathVariable Long customerId) {
        log.info("Received request to fetch customer with ID: {}", customerId);
        
        CustomerDTO customer = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getAllCustomers() {
        log.info("Received request to fetch all customers");
        
        List<CustomerDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomerByEmail(@PathVariable String email) {
        log.info("Received request to fetch customer with email: {}", email);
        
        CustomerDTO customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }
}
