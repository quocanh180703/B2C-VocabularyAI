# ============================================================
# Dockerfile - Build VocabularyAI Backend
# ============================================================
# 
# Multi-stage build:
# Stage 1: Build application (Maven)
# Stage 2: Run application (Java)
#
# Usage:
#   docker build -t vocabulary-ai-backend:1.0.0 .
#   docker run -p 8080:8080 --env-file .env vocabulary-ai-backend:1.0.0
#
# ============================================================

# ============================================================
# Stage 1: Build Stage
# ============================================================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build application
RUN mvn clean package -DskipTests=true -B

# ============================================================
# Stage 2: Runtime Stage
# ============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install additional tools
RUN apk add --no-cache curl tzdata

# Set timezone
ENV TZ=Asia/Ho_Chi_Minh

# Copy JAR from builder stage
COPY --from=builder /build/target/vocabulary-ai-backend-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD []
