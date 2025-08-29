package com.presta.Wallet.creation.microservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.presta.Wallet.ServiceSimulator;
import com.presta.Wallet.dto.ServiceResponse;

@ExtendWith(MockitoExtension.class)
class ServiceSimulatorTest {

    @Test
    void callCRBService_ReturnsValidResponse() {
        // Given
        ServiceSimulator serviceSimulator = new ServiceSimulator();
        ReflectionTestUtils.setField(serviceSimulator, "crbCost", BigDecimal.valueOf(50.00));

        // When
        ServiceResponse response = serviceSimulator.callCRBService("12345678", "+254700000000", "REF123");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getServiceType()).isEqualTo("CRB");
        assertThat(response.getStatus()).isIn("SUCCESS", "FAILED");
        assertThat(response.getCost()).isEqualTo(BigDecimal.valueOf(50.00));
        assertThat(response.getExternalReference()).isNotBlank();
    }

    @Test
    void callKYCService_ReturnsValidResponse() {
        // Given
        ServiceSimulator serviceSimulator = new ServiceSimulator();
        ReflectionTestUtils.setField(serviceSimulator, "kycCost", BigDecimal.valueOf(25.00));

        // When
        ServiceResponse response = serviceSimulator.callKYCService("12345678", "+254700000000", "REF124");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getServiceType()).isEqualTo("KYC");
        assertThat(response.getStatus()).isIn("SUCCESS", "FAILED");
        assertThat(response.getCost()).isEqualTo(BigDecimal.valueOf(25.00));
    }

    @Test
    void callCreditScoringService_ReturnsValidResponse() {
        // Given
        ServiceSimulator serviceSimulator = new ServiceSimulator();
        ReflectionTestUtils.setField(serviceSimulator, "creditScoringCost", BigDecimal.valueOf(75.00));

        // When
        ServiceResponse response = serviceSimulator.callCreditScoringService("12345678", "+254700000000", "REF125");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getServiceType()).isEqualTo("CREDIT_SCORING");
        assertThat(response.getStatus()).isIn("SUCCESS", "FAILED");
        assertThat(response.getCost()).isEqualTo(BigDecimal.valueOf(75.00));
    }

    @Test
    void getServiceCost_ReturnsCorrectCosts() {
        // Given
        ServiceSimulator serviceSimulator = new ServiceSimulator();
        ReflectionTestUtils.setField(serviceSimulator, "crbCost", BigDecimal.valueOf(50.00));
        ReflectionTestUtils.setField(serviceSimulator, "kycCost", BigDecimal.valueOf(25.00));
        ReflectionTestUtils.setField(serviceSimulator, "creditScoringCost", BigDecimal.valueOf(75.00));

        // Then
        assertThat(serviceSimulator.getServiceCost("CRB")).isEqualTo(BigDecimal.valueOf(50.00));
        assertThat(serviceSimulator.getServiceCost("KYC")).isEqualTo(BigDecimal.valueOf(25.00));
        assertThat(serviceSimulator.getServiceCost("CREDIT_SCORING")).isEqualTo(BigDecimal.valueOf(75.00));
    }

    @Test
    void getServiceCost_UnknownService_ThrowsException() {
        // Given
        ServiceSimulator serviceSimulator = new ServiceSimulator();

        // When & Then
        assertThatThrownBy(() -> serviceSimulator.getServiceCost("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown service type");
    }

    @Test
    void isServiceEnabled_ReturnsTrue() {
        // Given
        ServiceSimulator serviceSimulator = new ServiceSimulator();

        // When & Then
        assertThat(serviceSimulator.isServiceEnabled("CRB")).isTrue();
        assertThat(serviceSimulator.isServiceEnabled("KYC")).isTrue();
        assertThat(serviceSimulator.isServiceEnabled("CREDIT_SCORING")).isTrue();
    }
}