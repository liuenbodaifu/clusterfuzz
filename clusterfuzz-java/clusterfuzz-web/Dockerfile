# Multi-stage build for ClusterFuzz Web module
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy parent pom and module poms first for better caching
COPY pom.xml .
COPY clusterfuzz-core/pom.xml clusterfuzz-core/
COPY clusterfuzz-web/pom.xml clusterfuzz-web/

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY clusterfuzz-core/src clusterfuzz-core/src
COPY clusterfuzz-web/src clusterfuzz-web/src

# Build the application
RUN mvn clean package -B -DskipTests -pl clusterfuzz-web -am

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install necessary packages
RUN apk add --no-cache \
    curl \
    dumb-init \
    && addgroup -g 1000 clusterfuzz \
    && adduser -D -u 1000 -G clusterfuzz clusterfuzz

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/clusterfuzz-web/target/clusterfuzz-web-*.jar app.jar

# Create necessary directories
RUN mkdir -p /app/logs /app/data \
    && chown -R clusterfuzz:clusterfuzz /app

# Switch to non-root user
USER clusterfuzz

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+OptimizeStringConcat \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=docker"

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]

# Start the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels for metadata
LABEL maintainer="ClusterFuzz Team" \
      version="1.0.0" \
      description="ClusterFuzz Web API Server" \
      org.opencontainers.image.source="https://github.com/liuenbodaifu/clusterfuzz" \
      org.opencontainers.image.title="ClusterFuzz Web" \
      org.opencontainers.image.description="REST API server for ClusterFuzz Java implementation"