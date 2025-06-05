# Multi-stage build for ClusterFuzz Bot module
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy parent pom and module poms first for better caching
COPY pom.xml .
COPY clusterfuzz-core/pom.xml clusterfuzz-core/
COPY clusterfuzz-bot/pom.xml clusterfuzz-bot/

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY clusterfuzz-core/src clusterfuzz-core/src
COPY clusterfuzz-bot/src clusterfuzz-bot/src

# Build the application
RUN mvn clean package -B -DskipTests -pl clusterfuzz-bot -am

# Runtime stage with additional tools for fuzzing
FROM eclipse-temurin:17-jre-alpine

# Install fuzzing tools and dependencies
RUN apk add --no-cache \
    curl \
    dumb-init \
    git \
    build-base \
    cmake \
    llvm \
    clang \
    python3 \
    python3-dev \
    py3-pip \
    bash \
    && addgroup -g 1000 clusterfuzz \
    && adduser -D -u 1000 -G clusterfuzz clusterfuzz

# Install AFL++ (American Fuzzy Lop)
RUN git clone https://github.com/AFLplusplus/AFLplusplus.git /tmp/aflplusplus \
    && cd /tmp/aflplusplus \
    && make all \
    && make install \
    && rm -rf /tmp/aflplusplus

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/clusterfuzz-bot/target/clusterfuzz-bot-*.jar app.jar

# Create necessary directories
RUN mkdir -p /app/logs /app/data /app/work /app/corpus /app/crashes \
    && chown -R clusterfuzz:clusterfuzz /app

# Switch to non-root user
USER clusterfuzz

# Expose port for health checks
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+OptimizeStringConcat \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=docker"

# Environment variables for fuzzing
ENV AFL_PATH="/usr/local/bin" \
    AFL_SKIP_CPUFREQ=1 \
    AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]

# Start the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels for metadata
LABEL maintainer="ClusterFuzz Team" \
      version="1.0.0" \
      description="ClusterFuzz Bot Worker" \
      org.opencontainers.image.source="https://github.com/liuenbodaifu/clusterfuzz" \
      org.opencontainers.image.title="ClusterFuzz Bot" \
      org.opencontainers.image.description="Worker bot for ClusterFuzz Java implementation with fuzzing tools"