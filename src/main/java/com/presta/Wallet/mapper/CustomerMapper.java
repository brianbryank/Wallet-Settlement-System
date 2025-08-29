package com.presta.Wallet.mapper;

import org.springframework.stereotype.Component;

import com.presta.Wallet.dto.CreateCustomerRequest;
import com.presta.Wallet.dto.CustomerDTO;
import com.presta.Wallet.entity.Customer;

@Component
public class CustomerMapper {

    public CustomerDTO toDTO(Customer customer) {
        if (customer == null) {
            return null;
        }
        
        return CustomerDTO.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .status(customer.getStatus().name())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    public Customer toEntity(CreateCustomerRequest request) {
        if (request == null) {
            return null;
        }
        
        return Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .status(Customer.CustomerStatus.ACTIVE)
                .build();
    }
}