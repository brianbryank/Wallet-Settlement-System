package com.presta.Wallet.creation.microservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.presta.Wallet.dto.CreateCustomerRequest;
import com.presta.Wallet.dto.CustomerDTO;
import com.presta.Wallet.entity.Customer;
import com.presta.Wallet.exception.CustomerNotFoundException;
import com.presta.Wallet.exception.DuplicateResourceException;
import com.presta.Wallet.mapper.CustomerMapper;
import com.presta.Wallet.repository.CustomerRepository;
import com.presta.Wallet.service.CustomerService;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerDTO customerDTO;
    private CreateCustomerRequest createRequest;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .phoneNumber("+254700123456")
                .status(Customer.CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        customerDTO = CustomerDTO.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .phoneNumber("+254700123456")
                .status("ACTIVE")
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();

        createRequest = CreateCustomerRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .phoneNumber("+254700123456")
                .build();
    }

    @Test
    void createCustomer_Success() {
        // Given
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerMapper.toEntity(any(CreateCustomerRequest.class))).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toDTO(any(Customer.class))).thenReturn(customerDTO);

        // When
        CustomerDTO result = customerService.createCustomer(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(customerRepository).existsByEmail("john@example.com");
        verify(customerRepository).save(any(Customer.class));
        verify(customerMapper).toDTO(customer);
    }

    @Test
    void createCustomer_DuplicateEmail_ThrowsException() {
        // Given
        when(customerRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> customerService.createCustomer(createRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Customer already exists with email");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void getCustomerById_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerMapper.toDTO(any(Customer.class))).thenReturn(customerDTO);

        // When
        CustomerDTO result = customerService.getCustomerById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(customerRepository).findById(1L);
    }

    @Test
    void getCustomerById_NotFound_ThrowsException() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customerService.getCustomerById(1L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer not found with ID: 1");
    }

    @Test
    void getAllCustomers_Success() {
        // Given
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findAll()).thenReturn(customers);
        when(customerMapper.toDTO(any(Customer.class))).thenReturn(customerDTO);

        // When
        List<CustomerDTO> result = customerService.getAllCustomers();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        verify(customerRepository).findAll();
    }

    @Test
    void getCustomerByEmail_Success() {
        // Given
        when(customerRepository.findByEmail("john@example.com")).thenReturn(Optional.of(customer));
        when(customerMapper.toDTO(customer)).thenReturn(customerDTO);

        // When
        CustomerDTO result = customerService.getCustomerByEmail("john@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getCustomerEntityById_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // When
        Customer result = customerService.getCustomerEntityById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
    }
}
