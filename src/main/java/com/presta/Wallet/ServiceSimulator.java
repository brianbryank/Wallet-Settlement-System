package com.presta.Wallet;



import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.presta.Wallet.dto.ServiceResponse;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
public class ServiceSimulator {

    @Value("${wallet.services.crb.cost}")
    private BigDecimal crbCost;

    @Value("${wallet.services.kyc.cost}")
    private BigDecimal kycCost;

    @Value("${wallet.services.credit-scoring.cost}")
    private BigDecimal creditScoringCost;

    private final Random random = new Random();

    public ServiceResponse callCRBService(String nationalId, String phoneNumber, String referenceId) {
        log.info("Simulating CRB service call for nationalId: {}, reference: {}", nationalId, referenceId);
        
        simulateDelay(500, 1500);
        
        boolean success = random.nextDouble() < 0.90;
        
        return ServiceResponse.builder()
                .serviceType("CRB")
                .status(success ? "SUCCESS" : "FAILED")
                .message(success ? "CRB check completed successfully" : "CRB service temporarily unavailable")
                .externalReference(UUID.randomUUID().toString())
                .cost(crbCost)
                .result(success ? generateCRBResult() : null)
                .build();
    }

    public ServiceResponse callKYCService(String nationalId, String phoneNumber, String referenceId) {
        log.info("Simulating KYC service call for nationalId: {}, reference: {}", nationalId, referenceId);
        
        simulateDelay(300, 1000);
        
        boolean success = random.nextDouble() < 0.95;
        
        return ServiceResponse.builder()
                .serviceType("KYC")
                .status(success ? "SUCCESS" : "FAILED")
                .message(success ? "KYC verification completed" : "KYC verification failed")
                .externalReference(UUID.randomUUID().toString())
                .cost(kycCost)
                .result(success ? generateKYCResult() : null)
                .build();
    }

    public ServiceResponse callCreditScoringService(String nationalId, String phoneNumber, String referenceId) {
        log.info("Simulating Credit Scoring service call for nationalId: {}, reference: {}", nationalId, referenceId);
        
        simulateDelay(800, 2000);
        
        // 85% success rate
        boolean success = random.nextDouble() < 0.85;
        
        return ServiceResponse.builder()
                .serviceType("CREDIT_SCORING")
                .status(success ? "SUCCESS" : "FAILED")
                .message(success ? "Credit score calculated" : "Credit scoring service error")
                .externalReference(UUID.randomUUID().toString())
                .cost(creditScoringCost)
                .result(success ? generateCreditScoringResult() : null)
                .build();
    }

    public BigDecimal getServiceCost(String serviceType) {
        return switch (serviceType.toUpperCase()) {
            case "CRB" -> crbCost;
            case "KYC" -> kycCost;
            case "CREDIT_SCORING" -> creditScoringCost;
            default -> throw new IllegalArgumentException("Unknown service type: " + serviceType);
        };
    }

    public boolean isServiceEnabled(String serviceType) {
       
        return true;
    }

    private void simulateDelay(int minMs, int maxMs) {
        try {
            int delay = random.nextInt(maxMs - minMs) + minMs;
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Service simulation interrupted", e);
        }
    }

    private String generateCRBResult() {
        String[] statuses = {"CLEAN", "LISTED", "WATCH_LIST"};
        String status = statuses[random.nextInt(statuses.length)];
        
        return String.format("""
            {
                "status": "%s",
                "score": %d,
                "lastUpdated": "%s",
                "bureauName": "TransUnion Kenya"
            }
            """, status, random.nextInt(800) + 200, java.time.LocalDateTime.now());
    }

    private String generateKYCResult() {
        boolean verified = random.nextBoolean();
        
        return String.format("""
            {
                "verified": %s,
                "confidence": %.2f,
                "matchedFields": ["name", "id_number", "phone"],
                "verificationDate": "%s"
            }
            """, verified, random.nextDouble() * 0.3 + 0.7, java.time.LocalDateTime.now());
    }

    private String generateCreditScoringResult() {
        int score = random.nextInt(650) + 350; //let used Score between 350-1000
        String grade = getGradeFromScore(score);
        
        return String.format("""
            {
                "score": %d,
                "grade": "%s",
                "probability_of_default": %.3f,
                "recommendations": ["Approved for basic products"],
                "calculatedAt": "%s"
            }
            """, score, grade, random.nextDouble() * 0.1 + 0.01, java.time.LocalDateTime.now());
    }

    private String getGradeFromScore(int score) {
        if (score >= 800) return "A";
        if (score >= 700) return "B";
        if (score >= 600) return "C";
        if (score >= 500) return "D";
        return "E";
    }
}