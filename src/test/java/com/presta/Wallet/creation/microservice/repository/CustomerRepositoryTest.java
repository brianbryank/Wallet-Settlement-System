package com.presta.Wallet.creation.microservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.presta.Wallet.entity.Customer;
import com.presta.Wallet.repository.CustomerRepository;

@DataJpaTest
@ActiveProfiles("test")
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer1;
    private Customer customer2;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        
        customer1 = Customer.builder()
                .name("John Doe")
                .email("john@example.com")
                .phoneNumber("+254700123456")
                .status(Customer.CustomerStatus.ACTIVE)
                .build();

        customer2 = Customer.builder()
                .name("Jane Smith")
                .email("jane@example.com")
                .phoneNumber("+254700987654")
                .status(Customer.CustomerStatus.INACTIVE)
                .build();

        customerRepository.save(customer1);
        customerRepository.save(customer2);
    }

    @Test
    void findByEmail_ExistingEmail_ReturnsCustomer() {
        // When
        Optional<Customer> result = customerRepository.findByEmail("john@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findByEmail_NonExistingEmail_ReturnsEmpty() {
        // When
        Optional<Customer> result = customerRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_ExistingEmail_ReturnsTrue() {
        // When
        boolean exists = customerRepository.existsByEmail("john@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_NonExistingEmail_ReturnsFalse() {
        // When
        boolean exists = customerRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByStatus_ActiveStatus_ReturnsActiveCustomers() {
        // When
        List<Customer> activeCustomers = customerRepository.findByStatus(Customer.CustomerStatus.ACTIVE);

        // Then
        assertThat(activeCustomers).hasSize(1);
        assertThat(activeCustomers.get(0).getName()).isEqualTo("John Doe");
        assertThat(activeCustomers.get(0).getStatus()).isEqualTo(Customer.CustomerStatus.ACTIVE);
    }

    @Test
    void findByStatus_InactiveStatus_ReturnsInactiveCustomers() {
        // When
        List<Customer> inactiveCustomers = customerRepository.findByStatus(Customer.CustomerStatus.INACTIVE);

        // Then
        assertThat(inactiveCustomers).hasSize(1);
        assertThat(inactiveCustomers.get(0).getName()).isEqualTo("Jane Smith");
        assertThat(inactiveCustomers.get(0).getStatus()).isEqualTo(Customer.CustomerStatus.INACTIVE);
    }

    @Test
    void save_NewCustomer_PersistsSuccessfully() {
        // Given
        Customer newCustomer = Customer.builder()
                .name("Bob Johnson")
                .email("bob@example.com")
                .phoneNumber("+254700555666")
                .status(Customer.CustomerStatus.ACTIVE)
                .build();

        // When
        Customer saved = customerRepository.save(newCustomer);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Bob Johnson");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}
