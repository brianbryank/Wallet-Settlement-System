# Wallet & Settlement Microservice

A Spring Boot microservice for managing customer wallets, processing transactions, and performing daily reconciliation with external reports.
# Author, *Brian kiplangat

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-orange.svg)](https://www.rabbitmq.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

A comprehensive microservice built with Java Spring Boot that provides wallet management, transaction processing, and reconciliation capabilities for third-party service consumption (CRB, KYC, Credit Scoring).

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Quick Start](#quick-start)
- [Setup Instructions](#setup-instructions)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [Assumptions & Limitations](#assumptions--limitations)
- [Project Structure](#project-structure)
- [Contributing](#contributing)

## Overview

This microservice implements a complete wallet and settlement system that allows:

- **Customer Management**: Create and manage customer profiles
- **Wallet Operations**: Hold balances and perform top-up/consumption operations
- **Service Integration**: Consume third-party services (CRB, KYC, Credit Scoring) with automatic balance deduction
- **Transaction Processing**: All operations are recorded in a comprehensive ledger with RabbitMQ queuing
- **Reconciliation System**: Daily reconciliation between internal transactions and external provider reports
- **File Processing**: Support for CSV/JSON external report uploads and processing
- **Comprehensive Reporting**: Export reconciliation reports and transaction summaries

## Features

### Core Features
- ✅ **Customer & Wallet Management** - Full CRUD operations
- ✅ **Balance Top-up** - Multiple payment source support
- ✅ **Service Consumption** - Integrated CRB, KYC, and Credit Scoring services
- ✅ **Transaction Ledger** - Complete audit trail for all operations
- ✅ **Concurrency Control** - Optimistic locking to prevent double deductions
- ✅ **Idempotency** - Duplicate transaction protection using reference IDs

### Advanced Features
- ✅ **RabbitMQ Integration** - Asynchronous transaction processing
- ✅ **Reconciliation Engine** - 4-phase intelligent matching algorithm
- ✅ **File Processing** - CSV/JSON external report handling
- ✅ **Export Capabilities** - Professional CSV report generation
- ✅ **Comprehensive Monitoring** - Health checks and metrics endpoints
- ✅ **Docker Support** - Full containerization with production-ready setup

## 🛠 Technology Stack

| Category | Technology | Version | Purpose |
|----------|------------|---------|---------|
| **Backend** | Java | 17 | Core application language |
| **Framework** | Spring Boot | 3.2.0 | Application framework |
| **Database** | PostgreSQL | 15 | Primary data storage |
| **Message Queue** | RabbitMQ | 3.12 | Asynchronous processing |
| **Build Tool** | Maven | 3.9+ | Dependency management |
| **Documentation** | Spring Boot Actuator | - | Health checks & metrics |
| **Testing** | JUnit 5 | - | Unit & integration testing |
| **Containerization** | Docker | - | Application packaging |

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose (for containerized setup)

### Run with Docker (Recommended)
```bash
# Clone the repository
git clone < git@github.com:brianbryank/Wallet-Settlement-System.git >
cd wallet-service

# Start the complete stack
docker-compose up -d


### Run Locally
```bash
# Clone and navigate to project
git clone <git@github.com:brianbryank/Wallet-Settlement-System.git>
cd wallet-service

# Run the application
./mvnw spring-boot:run

# Access the application
curl http://localhost:9191/api/v1/health
```

## 🔧 Setup Instructions

### Option 1: Docker Setup (Production-Ready)

1. **Prerequisites**
   ```bash
   # Verify Docker installation
   docker --version
   docker-compose --version
   ```

2. **Start Services**
   ```bash
   # Start PostgreSQL, RabbitMQ, and the application
   docker-compose up -d
   
   # Check service status
   docker-compose ps
   ```

3. **Verify Deployment**
   ```bash
   # Health check
   curl http://localhost:9191/api/v1/health
   
   # RabbitMQ Management UI
   open http://localhost:15672
   # Credentials: wallet_user / wallet_secure_pass
   ```

4. **Access Points**
   - **API Base URL**: http://localhost:9191/api/v1
   - **RabbitMQ Management**: http://localhost:15672
   - **Database**: localhost:5432 (wallet_user/wallet_secure_pass/walletdb)

### Option 2: Local Development Setup

1. **Database Setup**
   ```bash
   # Start PostgreSQL with Docker
   docker run --name wallet-postgres -d \
     -e POSTGRES_DB=walletdb \
     -e POSTGRES_USER=wallet_user \
     -e POSTGRES_PASSWORD=wallet_pass \
     -p 5432:5432 postgres:15
   ```

2. **RabbitMQ Setup**
   ```bash
   # Start RabbitMQ with Docker
   docker run --name wallet-rabbitmq -d \
     -e RABBITMQ_DEFAULT_USER=wallet\
     -e RABBITMQ_DEFAULT_PASS=walletpass \
     -p "15672:15672"     # Management ui
      - "5673:5672"   #AMQP \
     rabbitmq:3-management
   ```

3. **Application Setup**
   ```bash
   # Clone repository
   git clone <git@github.com:brianbryank/Wallet-Settlement-System.git>
   cd wallet-service
   
   # Update application.yml with database credentials
   # Run the application
   ./mvnw spring-boot:run
   ```

### Option 3: H2 In-Memory Database (Testing)

```bash
# Run with H2 database
./mvnw spring-boot:run -Dspring.profiles.active=test

# Access H2 Console
open http://localhost:9191/api/v1/h2-console
# JDBC URL: jdbc:h2:mem:testdb
# Username: sa, Password: (empty)
```

## API Documentation

### Base URL
```
http://localhost:9191/api/v1
```

### Authentication
Currently, the API does not require authentication (suitable for internal microservice communication).

---

## Health & Monitoring

### Health Check
```http
GET http://localhost:9191/api/v1/health
```

**Response:**
```json
{
  "success": true,
  "message": "Service is healthy",
  "data": {
    "service": "wallet-service",
    "status": "UP",
    "timestamp": "2024-01-15T10:30:45",
    "version": "1.0.0"
  }
}
```

---

## Customer Management

### Create Customer
```http://localhost:9191/api/v1/customers
POST 
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+254700123456"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Customer created successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+254700123456",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:45",
    "updatedAt": "2024-01-15T10:30:45"
  }
}
```

### Get Customer
```http://localhost:9191/api/v1/customers/id
GET /customers/{customerId}
```

### Get All Customers
```http
GET /customers
```

### Get Customer by Email
```http
GET /customers/email/{email}
```

---

## Wallet Management

### Create Wallet
```http
POST /wallets
Content-Type: application/json

{
  "customerId": 1,
  "currency": "USD",
  "walletType": "CREDITS"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Wallet created successfully",
  "data": {
    "id": 1,
    "customerId": 1,
    "customerName": "John Doe",
    "balance": 0.00,
    "currency": "KSH",
    "status": "ACTIVE",
    "walletType": "CREDITS",
    "createdAt": "2024-01-15T10:31:00",
    "updatedAt": "2024-01-15T10:31:00"
  }
}
```

### Get Wallet Balance
```http
GET /wallets/{walletId}/balance
```

**Response:**
```json
{
  "success": true,
  "data": {
    "walletId": 1,
    "balance": 150.00,
    "currency": "KSH",
    "status": "ACTIVE"
  }
}
```

### Get Wallets by Customer
```http
GET /wallets/customer/{customerId}
```

### Get All Wallets
```http
GET /wallets
```

---

##  Transaction Operations

### Top-up Wallet
```http
POST /wallets/{walletId}/topup
Content-Type: application/json

{
  "amount": 100.00,
  "referenceId": "TOPUP_001",
  "description": "Bank transfer top-up",
  "source": "BANK_TRANSFER"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Top-up completed successfully",
  "data": {
    "transactionId": 1,
    "walletId": 1,
    "transactionType": "TOPUP",
    "amount": 100.00,
    "referenceId": "TOPUP_001",
    "status": "COMPLETED",
    "balanceBefore": 50.00,
    "balanceAfter": 150.00,
    "createdAt": "2024-01-15T10:35:00"
  }
}
```

### Consume Balance
```http
POST /wallets/{walletId}/consume
Content-Type: application/json

{
  "amount": 50.00,
  "referenceId": "CONSUME_001",
  "serviceType": "CRB",
  "description": "Credit reference check",
  "externalReference": "EXT_REF_123"
}
```

### Consume Service (Auto-deduct based on service cost)
```http
POST /wallets/{walletId}/consume-service
Content-Type: application/json

{
  "serviceType": "KYC",
  "referenceId": "KYC_001",
  "description": "KYC verification",
  "customerId": "CUST_123",
  "nationalId": "12345678",
  "phoneNumber": "+254700000000"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Service consumption completed",
  "data": {
    "serviceType": "KYC",
    "status": "SUCCESS",
    "message": "KYC verification completed",
    "externalReference": "uuid-ext-ref",
    "cost": 25.00,
    "result": "{\"verified\": true, \"confidence\": 0.89}"
  }
}
```

### Get Transaction History
```http
GET /wallets/{walletId}/transactions?page=0&size=10
```

### Get Transaction by Reference
```http
GET /wallets/transactions/reference/{referenceId}
```

### Get Service Information
```http
GET /wallets/services
```

**Response:**
```json
{
  "success": true,
  "data": {
    "services": [
      {
        "serviceType": "CRB",
        "cost": 50.00,
        "description": "Credit Reference Bureau check",
        "estimatedDuration": "1-3 seconds"
      },
      {
        "serviceType": "KYC",
        "cost": 25.00,
        "description": "Know Your Customer verification",
        "estimatedDuration": "1-2 seconds"
      },
      {
        "serviceType": "CREDIT_SCORING",
        "cost": 75.00,
        "description": "Credit score calculation",
        "estimatedDuration": "2-5 seconds"
      }
    ]
  }
}
```

---

## Reconciliation System

### Upload External Report
```http
POST /reconciliation/upload
Content-Type: multipart/form-data

file: [CSV or JSON file]
providerName: EXTERNAL_PROVIDER
reportDate: 2025-08-29
```

**Response:**
```json
{
  "success": true,
  "message": "File uploaded and processed successfully",
  "data": {
    "fileName": "external_report_2025-08-29.csv",
    "providerName": "EXTERNAL_PROVIDER",
    "reportDate": "2025-08-29",
    "transactionsProcessed": 25,
    "status": "SUCCESS"
  }
}
```

### Process Reconciliation
```http
POST /reconciliation/process?date=2025-08-29
```

### Get Reconciliation Report
```http
GET /reconciliation/report?date=2025-08-29&includeDetails=true
```

**Response:**
```json
{
  "success": true,
  "data": {
    "reportId": 1,
    "reconciliationDate": "2025-08-29",
    "summary": {
      "totalInternalTransactions": 25,
      "totalExternalTransactions": 23,
      "matchedTransactions": 20,
      "unmatchedInternal": 3,
      "unmatchedExternal": 2,
      "amountDifferences": 1,
      "totalInternalAmount": 1250.00,
      "totalExternalAmount": 1200.00,
      "differenceAmount": 50.00,
      "matchPercentage": 80.0,
      "reconciliationStatus": "ACCEPTABLE"
    },
    "discrepancies": [
      {
        "itemId": 1,
        "referenceId": "MISSING_001",
        "matchType": "NO_MATCH",
        "discrepancyType": "MISSING_EXTERNAL",
        "internalTransactionId": 15,
        "internalAmount": 50.00,
        "severity": "HIGH",
        "notes": "Internal transaction with no external match"
      }
    ],
    "status": "COMPLETED",
    "createdAt": "2024-01-15T11:00:00",
    "completedAt": "2024-01-15T11:02:30"
  }
}
```

### Export Reconciliation Report
```http
GET /reconciliation/export?date=2024-01-15
```
*Downloads CSV file: reconciliation_report_2024-01-15.csv*

### Export Transaction Summary
```http
GET /reconciliation/export/summary?startDate=2024-01-01&endDate=2024-01-31
```
*Downloads CSV file: transaction_summary_2024-01-01_to_2024-01-31.csv*

### Get Reconciliation History
```http
GET /reconciliation/history?limit=30
```

### Get Reconciliation Status
```http
GET /reconciliation/status
```

---

## 🛠 Utility Endpoints

### Generate Sample External Data
```http
POST /reconciliation/generate-sample-data?date=2024-01-15&count=10
```

### Download Sample External Report
```http
GET /reconciliation/sample-report?date=2024-01-15&count=10
```

---

## 🧪 Testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=CustomerServiceTest

# Run integration tests
./mvnw verify

# Run tests with coverage
./mvnw test jacoco:report
```

### Test Categories

1. **Unit Tests**
   - Service layer testing with Mockito
   - Repository layer testing
   - Utility class testing

2. **Integration Tests**
   - Controller endpoint testing
   - Database integration testing
   - RabbitMQ integration testing

3. **Manual Testing**
   - Postman collection included
   - Comprehensive API workflow testing

### Postman Collection

A complete Postman collection is included in the repository:
- **File**: `wallet-service-postman-collection.json`
- **Features**: 25+ pre-configured API calls
- **Environment**: Variables for easy testing
- **Error Scenarios**: Comprehensive error testing

**Import Instructions:**
1. Open Postman
2. Click Import → Upload Files
3. Select `wallet-service-postman-collection.json`
4. Set environment variables (baseUrl, customerId, walletId)

## 🐳 Docker Deployment

### Development Environment
```bash
# Start with H2 database
docker-compose -f docker-compose.dev.yml up -d
```

### Production Environment
```bash
# Start full stack with PostgreSQL and RabbitMQ
docker-compose up -d

# Monitor logs
docker-compose logs -f wallet-service
```

### Services Included
- **wallet-service**: Main application (Port 8080)
- **postgres**: Database (Port 5432)
- **rabbitmq**: Message broker (Ports 5672, 15672)
- **nginx**: Reverse proxy (Port 80)
- **prometheus**: Metrics collection (Port 9090)
- **grafana**: Monitoring dashboard (Port 3000)

## 🏗 Architecture

### System Architecture
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │────│    Nginx    │────│   Wallet    │
│ Application │    │   (Proxy)   │    │  Service    │
└─────────────┘    └─────────────┘    └──────┬──────┘
                                              │
                   ┌─────────────┐    ┌───────▼──────┐
                   │  RabbitMQ   │────│ PostgreSQL   │
                   │ (Messages)  │    │ (Database)   │
                   └─────────────┘    └──────────────┘
```

### Application Architecture
- **Controller Layer**: REST API endpoints
- **Service Layer**: Business logic and transaction management
- **Repository Layer**: Data access and persistence
- **Message Layer**: Asynchronous processing with RabbitMQ
- **Security Layer**: Input validation and error handling

## 🗄 Database Schema

### Core Tables

#### customers
```sql
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### wallets
```sql
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES customers(id),
    balance DECIMAL(19,2) DEFAULT 0.00 CHECK (balance >= 0),
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    wallet_type VARCHAR(20) DEFAULT 'CREDITS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    UNIQUE(customer_id, wallet_type)
);
```

#### wallet_transactions
```sql
CREATE TABLE wallet_transactions (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT REFERENCES wallets(id),
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(19,2) NOT NULL CHECK (amount > 0),
    reference_id VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING',
    balance_before DECIMAL(19,2),
    balance_after DECIMAL(19,2),
    service_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);
```

### Reconciliation Tables

#### external_transactions
```sql
CREATE TABLE external_transactions (
    id BIGSERIAL PRIMARY KEY,
    external_transaction_id VARCHAR(255) NOT NULL,
    transaction_date DATE NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    reference_id VARCHAR(255),
    transaction_type VARCHAR(50),
    customer_id VARCHAR(255),
    service_type VARCHAR(50),
    provider_name VARCHAR(255),
    file_name VARCHAR(255),
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### reconciliation_reports
```sql
CREATE TABLE reconciliation_reports (
    id BIGSERIAL PRIMARY KEY,
    reconciliation_date DATE UNIQUE NOT NULL,
    total_internal_transactions INT NOT NULL,
    total_external_transactions INT NOT NULL,
    matched_transactions INT NOT NULL,
    unmatched_internal INT NOT NULL,
    unmatched_external INT NOT NULL,
    amount_differences INT NOT NULL,
    total_internal_amount DECIMAL(19,2),
    total_external_amount DECIMAL(19,2),
    difference_amount DECIMAL(19,2),
    status VARCHAR(20) DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);
```

## ⚡ Performance Features

### Optimizations
- **Connection Pooling**: HikariCP with optimized settings
- **Optimistic Locking**: Prevents race conditions on wallet operations
- **Database Indexing**: Strategic indexes on frequently queried columns
- **Caching**: Service-level caching for reference data
- **Batch Processing**: Efficient bulk operations for reconciliation

### Monitoring
- **Health Checks**: Comprehensive application and dependency health monitoring
- **Metrics**: Business and technical metrics via Actuator
- **Logging**: Structured logging with correlation IDs
- **Tracing**: Request tracing for debugging and monitoring

## Business Logic

### Transaction Processing
1. **Idempotency**: Duplicate reference ID prevention
2. **Balance Validation**: Sufficient funds check before deduction
3. **Atomic Operations**: Database transactions ensure consistency
4. **Audit Trail**: Complete transaction history maintenance
5. **Async Notification**: RabbitMQ queuing for event processing

### Service Integration
- **CRB Service**: Credit Reference Bureau check ($50.00)
- **KYC Service**: Know Your Customer verification ($25.00)
- **Credit Scoring**: Credit score calculation ($75.00)
- **Service Simulation**: Realistic response times and success rates

### Reconciliation Engine
1. **Perfect Match**: Reference ID + exact amount match
2. **Reference Match**: Reference ID match with amount difference
3. **Missing Internal**: External transaction with no internal match
4. **Missing External**: Internal transaction with no external match

##  Assumptions & Limitations

### Assumptions Made

1. **Single Currency**: 
   - All transactions are processed in USD
   - Multi-currency support can be added in future versions

2. **Service Simulation**: 
   - Third-party services (CRB, KYC, Credit Scoring) are simulated
   - Real integrations would require API credentials and endpoints

3. **Authentication**: 
   - No authentication/authorization implemented
   - Suitable for internal microservice communication
   - Production deployment would require security implementation

4. **Reconciliation Frequency**: 
   - Daily reconciliation assumed
   - Automated scheduling not implemented (manual trigger required)

5. **File Processing**: 
   - CSV and JSON formats supported for external reports
   - File size limited to 50MB for performance reasons

6. **Database**: 
   - PostgreSQL for production, H2 for development/testing
   - Single database instance (no sharding or replication)

### Current Limitations

1. **Scalability**: 
   - Single instance application
   - Horizontal scaling would require session management and database clustering

2. **Security**: 
   - No encryption at rest
   - No API authentication/authorization
   - SSL/TLS configuration available but not enforced in development

3. **Backup & Recovery**: 
   - Database backup strategy not implemented
   - Disaster recovery procedures not defined

4. **Advanced Features**: 
   - No transaction limits or spending controls
   - No fraud detection mechanisms
   - No regulatory compliance features (PCI DSS, etc.)

5. **Monitoring**: 
   - Basic health checks and metrics
   - Advanced APM (Application Performance Monitoring) not integrated

### Future Enhancements

1. **Security**: 
   - JWT-based authentication
   - Role-based access control (RBAC)
   - API rate limiting and throttling

2. **Advanced Features**: 
   - Transaction limits and controls
   - Fraud detection algorithms
   - Regulatory compliance features

3. **Scalability**: 
   - Database clustering and replication
   - Caching layer (Redis)

4. **Integration**: 
   - Real third-party service integrations
   - Webhook support for external notifications
   - Event sourcing pattern implementation

##  Project Structure

```
wallet-service/
├── src/
│   ├── main/
│   │   ├── java/com/wallet/
│   │   │   ├── controller/          # REST Controllers
│   │   │   │   ├── CustomerController.java
│   │   │   │   ├── WalletController.java
│   │   │   │   ├── TransactionController.java
│   │   │   │   ├── ReconciliationController.java
│   │   │   │   └── HealthController.java
│   │   │   ├── service/            # Business Logic
│   │   │   │   ├── CustomerService.java
│   │   │   │   ├── WalletService.java
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── ReconciliationService.java
│   │   │   │   ├── FileProcessingService.java
│   │   │   │   ├── CsvExportService.java
│   │   │   │   ├── NotificationService.java
│   │   │   │   └── ServiceSimulator.java
│   │   │   ├── repository/         # Data Access
│   │   │   │   ├── CustomerRepository.java
│   │   │   │   ├── WalletRepository.java
│   │   │   │   ├── WalletTransactionRepository.java
│   │   │   │   ├── ExternalTransactionRepository.java
│   │   │   │   ├── ReconciliationReportRepository.java
│   │   │   │   └── ReconciliationItemRepository.java
│   │   │   ├── entity/            # JPA Entities
│   │   │   │   ├── Customer.java
│   │   │   │   ├── Wallet.java
│   │   │   │   ├── WalletTransaction.java
│   │   │   │   ├── ExternalTransaction.java
│   │   │   │   ├── ReconciliationReport.java
│   │   │   │   └── ReconciliationItem.java
│   │   │   ├── dto/               # Data Transfer Objects
│   │   │   │   ├── CustomerDTO.java
│   │   │   │   ├── WalletDTO.java
│   │   │   │   ├── TransactionResponse.java
│   │   │   │   ├── ReconciliationReportResponse.java
│   │   │   │   └── [Other DTOs...]
│   │   │   ├── mapper/            # Entity-DTO Mappers
│   │   │   ├── exception/         # Custom Exceptions
│   │   │   ├── config/           # Configuration Classes
│   │   │   └── message/          # RabbitMQ Message Classes
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       └── db/changelog/     # Database Migration Scripts
│   └── test/
│       └── java/com/wallet/      # Test Classes
 ── docker-compose.yml
│ docker-compose.dev.yml
│   ├── Dockerfile
│   └── [Configuration Files...]
├── scripts/                      # Utility Scripts
├── docs/                        # Additional Documentation
├── wallet-service-postman-collection.json
├── pom.xml
└── README.md
```

##  Contributing

### Development Setup

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes**
4. **Run tests**
   ```bash
   ./mvnw test
   ```
5. **Submit a pull request**

### Code Standards
- Follow Java coding conventions
- Write comprehensive unit tests
- Update documentation for new features
- Use meaningful commit messages

### Testing Requirements
- Unit tests for all service methods
- Integration tests for API endpoints
- Maintain test coverage above 80%

---

## 📞 Support & Contact

For questions, issues, or contributions, please:

1. **Create an Issue**: Use GitHub issues for bug reports and feature requests
2. **Submit a Pull Request**: For code contributions
3. **Check Documentation**: Review this README and inline code documentation
4. **Email Me**; brianbryan0125@gmail.com

---

## License

This project is developed as an evaluation project and is intended for demonstration purposes.

---

## Acknowledgments

- Built with Spring Boot framework
- Uses PostgreSQL for reliable data storage
- RabbitMQ for robust message queuing
- Docker for containerization
- Comprehensive testing with JUnit 5

---

** This wallet and settlement microservice demonstrates enterprise-grade Java development with modern Spring Boot practices, comprehensive testing, and production-ready deployment capabilities.**