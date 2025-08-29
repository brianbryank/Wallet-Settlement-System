package com.presta.Wallet.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.presta.Wallet.dto.ApiResponse;
import com.presta.Wallet.dto.ReconciliationReportResponse;
import com.presta.Wallet.entity.ExternalTransaction;
import com.presta.Wallet.service.CsvExportService;
import com.presta.Wallet.service.FileProcessingService;
import com.presta.Wallet.service.ReconciliationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;
    private final FileProcessingService fileProcessingService;
    private final CsvExportService csvExportService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadExternalReport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("providerName") String providerName,
            @RequestParam("reportDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        
        log.info("Received file upload request: file={}, provider={}, date={}", 
                file.getOriginalFilename(), providerName, reportDate);

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File cannot be empty"));
            }

            List<ExternalTransaction> transactions = fileProcessingService.processFile(file, providerName, reportDate);
            
            Map<String, Object> result = Map.of(
                "fileName", file.getOriginalFilename(),
                "providerName", providerName,
                "reportDate", reportDate,
                "transactionsProcessed", transactions.size(),
                "status", "SUCCESS"
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("File uploaded and processed successfully", result));

        } catch (Exception e) {
            log.error("Failed to process uploaded file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("File processing failed: " + e.getMessage()));
        }
    }

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<ReconciliationReportResponse>> processReconciliation(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Received reconciliation process request for date: {}", date);

        try {
            ReconciliationReportResponse report = reconciliationService.performReconciliation(date);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Reconciliation completed successfully", report));

        } catch (Exception e) {
            log.error("Reconciliation failed for date: {} - Error: {}", date, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Reconciliation failed: " + e.getMessage()));
        }
    }

    @GetMapping("/report")
    public ResponseEntity<ApiResponse<ReconciliationReportResponse>> getReconciliationReport(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "includeDetails", defaultValue = "true") boolean includeDetails) {
        
        log.info("Received request for reconciliation report: date={}, includeDetails={}", date, includeDetails);

        try {
            ReconciliationReportResponse report = reconciliationService.getReconciliationReport(date);
            
            if (!includeDetails) {
                // here trying to remove detailed discrepancy items to reduce response size
                report.setDiscrepancies(null);
            }
            
            return ResponseEntity.ok(ApiResponse.success(report));

        } catch (RuntimeException e) {
            log.error("Failed to fetch reconciliation report for date: {} - Error: {}", date, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Reconciliation report not found for date: " + date));
        } catch (Exception e) {
            log.error("Error fetching reconciliation report for date: {} - Error: {}", date, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch reconciliation report: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ReconciliationReportResponse>>> getReconciliationHistory(
            @RequestParam(value = "limit", defaultValue = "30") int limit) {
        
        log.info("Received request for reconciliation history with limit: {}", limit);

        try {
            List<ReconciliationReportResponse> reports = reconciliationService.getReconciliationHistory(limit);
            
            return ResponseEntity.ok(ApiResponse.success(reports));

        } catch (Exception e) {
            log.error("Failed to fetch reconciliation history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch reconciliation history: " + e.getMessage()));
        }
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportReconciliationReport(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Received request to export reconciliation report for date: {}", date);

        try {
            String csvContent = csvExportService.exportReconciliationReport(date);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "reconciliation_report_" + date + ".csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvContent);

        } catch (RuntimeException e) {
            log.error("Export failed for date: {} - Report not found", date);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Reconciliation report not found for date: " + date);
        } catch (Exception e) {
            log.error("Export failed for date: {} - Error: {}", date, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Export failed: " + e.getMessage());
        }
    }

    @GetMapping("/export/summary")
    public ResponseEntity<String> exportTransactionSummary(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Received request to export transaction summary from {} to {}", startDate, endDate);

        try {
            String csvContent = csvExportService.exportTransactionSummary(startDate, endDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", 
                "transaction_summary_" + startDate + "_to_" + endDate + ".csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvContent);

        } catch (Exception e) {
            log.error("Summary export failed for period {} to {} - Error: {}", startDate, endDate, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Summary export failed: " + e.getMessage());
        }
    }

    // Utility endpoints for testing and development

    @PostMapping("/generate-sample-data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateSampleData(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "count", defaultValue = "10") int count) {
        
        log.info("Generating sample external data for date: {} with {} transactions", date, count);

        try {
            List<ExternalTransaction> transactions = fileProcessingService.generateSampleExternalData(date, count);
            
            Map<String, Object> result = Map.of(
                "date", date,
                "transactionsGenerated", transactions.size(),
                "status", "SUCCESS"
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Sample data generated successfully", result));

        } catch (Exception e) {
            log.error("Failed to generate sample data: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate sample data: " + e.getMessage()));
        }
    }

    @GetMapping("/sample-report")
    public ResponseEntity<String> generateSampleExternalReport(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "count", defaultValue = "10") int count) {
        
        log.info("Generating sample external CSV report for date: {} with {} transactions", date, count);

        try {
            String csvContent = csvExportService.generateSampleExternalReport(date, count);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "sample_external_report_" + date + ".csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvContent);

        } catch (Exception e) {
            log.error("Failed to generate sample CSV report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate sample report: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReconciliationStatus() {
        log.info("Received request for reconciliation service status");

        try {
            List<ReconciliationReportResponse> recentReports = reconciliationService.getReconciliationHistory(7);
            
            long totalReports = recentReports.size();
            long completedReports = recentReports.stream()
                    .mapToLong(r -> "COMPLETED".equals(r.getStatus()) ? 1 : 0)
                    .sum();
            
            Map<String, Object> status = Map.of(
                "serviceName", "Reconciliation Service",
                "status", "ACTIVE",
                "totalReportsLast7Days", totalReports,
                "completedReportsLast7Days", completedReports,
                "lastReportDate", recentReports.isEmpty() ? null : recentReports.get(0).getReconciliationDate(),
                "timestamp", java.time.LocalDateTime.now()
            );

            return ResponseEntity.ok(ApiResponse.success("Reconciliation service status", status));

        } catch (Exception e) {
            log.error("Failed to fetch reconciliation status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch service status: " + e.getMessage()));
        }
    }
}
