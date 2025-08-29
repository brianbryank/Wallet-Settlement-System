# Dockerfile
FROM openjdk:17-jdk-slim as builder

LABEL maintainer="wallet-service"
LABEL version="1.0.0"

# Install required packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests

# Production image
FROM openjdk:17-jdk-slim

# Create app user
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Install required packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Create data directory for application files
RUN mkdir -p /app/data && \
    mkdir -p /app/logs && \
    chown -R spring:spring /app

# Copy JAR from builder stage
COPY --from=builder /app/target/wallet-service-1.0.0.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1

# JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# docker-compose.yml - Complete Production Setup
version: '3.8'

services:
  # PostgreSQL Database
  postgres:
    image: postgres:15-alpine
    container_name: wallet-postgres
    environment:
      POSTGRES_DB: walletdb
      POSTGRES_USER: wallet_user
      POSTGRES_PASSWORD: wallet_secure_pass
      POSTGRES_INITDB_ARGS: "--encoding=UTF8 --lc-collate=C --lc-ctype=C"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d:ro
    ports:
      - "5432:5432"
    networks:
      - wallet-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U wallet_user -d walletdb"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 30s
    restart: unless-stopped

  # RabbitMQ Message Broker
  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    container_name: wallet-rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: wallet_user
      RABBITMQ_DEFAULT_PASS: wallet_secure_pass
      RABBITMQ_DEFAULT_VHOST: wallet_vhost
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
      - ./rabbitmq/rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf:ro
      - ./rabbitmq/definitions.json:/etc/rabbitmq/definitions.json:ro
    ports:
      - "5672:5672"   # AMQP port
      - "15672:15672" # Management UI
    networks:
      - wallet-network
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "ping"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s
    restart: unless-stopped

  # Wallet Service Application
  wallet-service:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: wallet-service
    environment:
      # Spring Profile
      SPRING_PROFILES_ACTIVE: docker
      
      # Database Configuration
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/walletdb
      SPRING_DATASOURCE_USERNAME: wallet_user
      SPRING_DATASOURCE_PASSWORD: wallet_secure_pass
      
      # RabbitMQ Configuration
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_RABBITMQ_PORT: 5672
      SPRING_RABBITMQ_USERNAME: wallet_user
      SPRING_RABBITMQ_PASSWORD: wallet_secure_pass
      SPRING_RABBITMQ_VIRTUAL_HOST: wallet_vhost
      
      # Service Configuration
      WALLET_SERVICES_CRB_COST: 50.00
      WALLET_SERVICES_KYC_COST: 25.00
      WALLET_SERVICES_CREDIT_SCORING_COST: 75.00
      
      # JVM Options
      JAVA_OPTS: -Xms512m -Xmx1024m -XX:+UseG1GC
      
      # Logging
      LOGGING_LEVEL_COM_WALLET: INFO
      LOGGING_LEVEL_ROOT: WARN
      
    ports:
      - "8080:8080"
    volumes:
      - wallet_data:/app/data
      - wallet_logs:/app/logs
    networks:
      - wallet-network
    depends_on:
      postgres:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/v1/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 90s
    restart: unless-stopped

  # Nginx Reverse Proxy (Optional)
  nginx:
    image: nginx:alpine
    container_name: wallet-nginx
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
    ports:
      - "80:80"
      - "443:443"
    networks:
      - wallet-network
    depends_on:
      - wallet-service
    restart: unless-stopped

  # Prometheus Monitoring (Optional)
  prometheus:
    image: prom/prometheus:latest
    container_name: wallet-prometheus
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus_data:/prometheus
    ports:
      - "9090:9090"
    networks:
      - wallet-network
    restart: unless-stopped

  # Grafana Dashboard (Optional)
  grafana:
    image: grafana/grafana:latest
    container_name: wallet-grafana
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
    volumes:
      - grafana_data:/var/lib/grafana
    ports:
      - "3000:3000"
    networks:
      - wallet-network
    restart: unless-stopped

# Named Volumes
volumes:
  postgres_data:
    driver: local
  rabbitmq_data:
    driver: local
  wallet_data:
    driver: local
  wallet_logs:
    driver: local
  prometheus_data:
    driver: local
  grafana_data:
    driver: local

# Networks
networks:
  wallet-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16

# docker-compose.dev.yml - Development Setup
version: '3.8'

services:
  # Development Database (PostgreSQL)
  postgres-dev:
    image: postgres:15-alpine
    container_name: wallet-postgres-dev
    environment:
      POSTGRES_DB: walletdb_dev
      POSTGRES_USER: dev_user
      POSTGRES_PASSWORD: dev_pass
    volumes:
      - postgres_dev_data:/var/lib/postgresql/data
    ports:
      - "5433:5432"
    networks:
      - wallet-dev-network
    restart: unless-stopped

  # Development RabbitMQ
  rabbitmq-dev:
    image: rabbitmq:3.12-management-alpine
    container_name: wallet-rabbitmq-dev
    environment:
      RABBITMQ_DEFAULT_USER: dev_user
      RABBITMQ_DEFAULT_PASS: dev_pass
    ports:
      - "5673:5672"
      - "15673:15672"
    networks:
      - wallet-dev-network
    restart: unless-stopped

  # Database Admin Tool
  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: wallet-pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@wallet.com
      PGADMIN_DEFAULT_PASSWORD: admin
      PGADMIN_CONFIG_SERVER_MODE: 'False'
    volumes:
      - pgadmin_data:/var/lib/pgadmin
    ports:
      - "8081:80"
    networks:
      - wallet-dev-network
    depends_on:
      - postgres-dev
    restart: unless-stopped

volumes:
  postgres_dev_data:
  pgadmin_data:

networks:
  wallet-dev-network:
    driver: bridge

# docker-compose.test.yml - Testing Setup
version: '3.8'

services:
  wallet-service-test:
    build:
      context: .
      dockerfile: Dockerfile
    environment:
      SPRING_PROFILES_ACTIVE: test
      SPRING_DATASOURCE_URL: jdbc:h2:mem:testdb
    command: ["./mvnw", "test"]
    volumes:
      - .:/app
      - maven_cache:/root/.m2
    networks:
      - test-network

volumes:
  maven_cache:

networks:
  test-network: