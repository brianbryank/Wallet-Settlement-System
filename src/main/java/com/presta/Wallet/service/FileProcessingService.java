package com.presta.Wallet.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.presta.Wallet.dto.ExternalTransactionDTO;
import com.presta.Wallet.entity.ExternalTransaction;
import com.presta.Wallet.repository.ExternalTransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileProcessingService {

    private final ExternalTransactionRepository externalTransactionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Transactional
    public List<ExternalTransaction> processFile(MultipartFile file, String providerName, LocalDate reportDate) {
        log.info("Processing file: {} for provider: {} and date: {}", file.getOriginalFilename(), providerName, reportDate);

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        try {
            List<ExternalTransactionDTO> transactionDTOs;
            
            if (fileName.toLowerCase().endsWith(".csv")) {
                transactionDTOs = processCsvFile(file);
            } else if (fileName.toLowerCase().endsWith(".json")) {
                transactionDTOs = processJsonFile(file);
            } else {
                throw new IllegalArgumentException("Unsupported file type. Only CSV and JSON files are supported.");
            }

            List<ExternalTransaction> externalTransactions = new ArrayList<>();
            
            for (ExternalTransactionDTO dto : transactionDTOs) {
                ExternalTransaction transaction = mapToEntity(dto, providerName, fileName, reportDate);
                externalTransactions.add(transaction);
            }

            List<ExternalTransaction> savedTransactions = externalTransactionRepository.saveAll(externalTransactions);
            log.info("Successfully processed {} external transactions from file: {}", savedTransactions.size(), fileName);
            
            return savedTransactions;

        } catch (Exception e) {
            log.error("Failed to process file: {} for provider: {}", fileName, providerName, e);
            throw new RuntimeException("File processing failed: " + e.getMessage(), e);
        }
    }

    private List<ExternalTransactionDTO> processCsvFile(MultipartFile file) throws Exception {
        List<ExternalTransactionDTO> transactions = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;
            String[] headers = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] values = parseCsvLine(line);
                
                if (isFirstLine) {
                    headers = values;
                    isFirstLine = false;
                    log.debug("CSV Headers: {}", String.join(", ", headers));
                    continue;
                }
                
                if (headers == null || values.length < headers.length) {
                    log.warn("Skipping invalid CSV line: {}", line);
                    continue;
                }
                
                try {
                    ExternalTransactionDTO transaction = parseCsvRecord(headers, values);
                    if (transaction != null) {
                        transactions.add(transaction);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse CSV record: {} - Error: {}", line, e.getMessage());
                }
            }
        }
        
        log.info("Parsed {} transactions from CSV file", transactions.size());
        return transactions;
    }

    private List<ExternalTransactionDTO> processJsonFile(MultipartFile file) throws Exception {
        try {
            List<ExternalTransactionDTO> transactions = objectMapper.readValue(
                file.getInputStream(), 
                new TypeReference<List<ExternalTransactionDTO>>() {}
            );
            
            log.info("Parsed {} transactions from JSON file", transactions.size());
            return transactions;
            
        } catch (Exception e) {
            log.error("Failed to parse JSON file", e);
            throw new RuntimeException("Invalid JSON format: " + e.getMessage(), e);
        }
    }

    private String[] parseCsvLine(String line) {
        // Simple CSV parser - handles basic cases
        // In production, you might want to use a more robust CSV library
        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(currentValue.toString().trim());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        values.add(currentValue.toString().trim());
        
        return values.toArray(new String[0]);
    }

    private ExternalTransactionDTO parseCsvRecord(String[] headers, String[] values) {
        ExternalTransactionDTO.ExternalTransactionDTOBuilder builder = ExternalTransactionDTO.builder();
        
        for (int i = 0; i < headers.length && i < values.length; i++) {
            String header = headers[i].toLowerCase().trim();
            String value = values[i].trim();
            
            if (value.isEmpty()) continue;
            
            try {
                switch (header) {
                    case "transaction_id", "transactionid", "id" -> builder.transactionId(value);
                    case "transaction_date", "date", "transactiondate" -> {
                        LocalDate date = parseDate(value);
                        if (date != null) builder.transactionDate(date);
                    }
                    case "amount" -> {
                        BigDecimal amount = new BigDecimal(value);
                        builder.amount(amount);
                    }
                    case "reference_id", "reference", "referenceid" -> builder.referenceId(value);
                    case "transaction_type", "type", "transactiontype" -> builder.transactionType(value);
                    case "customer_id", "customerid" -> builder.customerId(value);
                    case "service_type", "service", "servicetype" -> builder.serviceType(value);
                    case "description" -> builder.description(value);
                }
            } catch (Exception e) {
                log.warn("Failed to parse field '{}' with value '{}': {}", header, value, e.getMessage());
            }
        }
        
        return builder.build();
    }

    private LocalDate parseDate(String dateStr) {
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                //my format
            }
        }
        
        log.warn("Could not parse date: {}", dateStr);
        return null;
    }

    private ExternalTransaction mapToEntity(ExternalTransactionDTO dto, String providerName, String fileName, LocalDate reportDate) {
        return ExternalTransaction.builder()
                .externalTransactionId(dto.getTransactionId())
                .transactionDate(dto.getTransactionDate() != null ? dto.getTransactionDate() : reportDate)
                .amount(dto.getAmount())
                .referenceId(dto.getReferenceId())
                .transactionType(dto.getTransactionType())
                .customerId(dto.getCustomerId())
                .serviceType(dto.getServiceType())
                .description(dto.getDescription())
                .providerName(providerName)
                .fileName(fileName)
                .status(ExternalTransaction.ProcessingStatus.PENDING)
                .build();
    }

    public List<ExternalTransaction> generateSampleExternalData(LocalDate date, int count) {
        log.info("Generating {} sample external transactions for date: {}", count, date);
        
        List<ExternalTransaction> sampleData = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            ExternalTransaction transaction = ExternalTransaction.builder()
                    .externalTransactionId("EXT_" + date.toString().replace("-", "") + "_" + String.format("%03d", i))
                    .transactionDate(date)
                    .amount(BigDecimal.valueOf(25.00 + (i * 10))) // Varying amounts
                    .referenceId("REF_" + System.currentTimeMillis() + "_" + i)
                    .transactionType(i % 3 == 0 ? "TOPUP" : "CONSUMPTION")
                    .customerId("CUST_" + (i % 5 + 1)) // 5 different customers
                    .serviceType(i % 2 == 0 ? "KYC" : "CRB")
                    .description("Sample external transaction " + i)
                    .providerName("SAMPLE_PROVIDER")
                    .fileName("sample_data_" + date + ".csv")
                    .status(ExternalTransaction.ProcessingStatus.PENDING)
                    .build();
            
            sampleData.add(transaction);
        }
        
        return externalTransactionRepository.saveAll(sampleData);
    }
}
