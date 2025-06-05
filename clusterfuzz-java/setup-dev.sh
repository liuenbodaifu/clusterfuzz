#!/bin/bash

# ClusterFuzz Java Development Environment Setup Script
# This script sets up the complete development environment for ClusterFuzz Java

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    local missing_tools=()
    
    if ! command_exists java; then
        missing_tools+=("Java 17+")
    else
        java_version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$java_version" -lt 17 ]; then
            missing_tools+=("Java 17+ (current: $java_version)")
        fi
    fi
    
    if ! command_exists mvn; then
        missing_tools+=("Maven 3.6+")
    fi
    
    if ! command_exists docker; then
        missing_tools+=("Docker")
    fi
    
    if ! command_exists docker-compose; then
        missing_tools+=("Docker Compose")
    fi
    
    if ! command_exists git; then
        missing_tools+=("Git")
    fi
    
    if [ ${#missing_tools[@]} -ne 0 ]; then
        log_error "Missing required tools:"
        for tool in "${missing_tools[@]}"; do
            echo "  - $tool"
        done
        echo ""
        echo "Please install the missing tools and run this script again."
        exit 1
    fi
    
    log_success "All prerequisites are satisfied"
}

# Create necessary directories
create_directories() {
    log_info "Creating necessary directories..."
    
    mkdir -p logs
    mkdir -p data
    mkdir -p work
    mkdir -p corpus
    mkdir -p crashes
    mkdir -p docker/grafana/provisioning/{dashboards,datasources}
    mkdir -p docker/grafana/dashboards
    mkdir -p docker/nginx/ssl
    
    log_success "Directories created"
}

# Generate SSL certificates for development
generate_ssl_certs() {
    log_info "Generating SSL certificates for development..."
    
    if [ ! -f "docker/nginx/ssl/cert.pem" ]; then
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout docker/nginx/ssl/key.pem \
            -out docker/nginx/ssl/cert.pem \
            -subj "/C=US/ST=CA/L=Mountain View/O=ClusterFuzz/OU=Development/CN=localhost"
        
        log_success "SSL certificates generated"
    else
        log_info "SSL certificates already exist"
    fi
}

# Setup Grafana provisioning
setup_grafana() {
    log_info "Setting up Grafana provisioning..."
    
    # Create datasource configuration
    cat > docker/grafana/provisioning/datasources/prometheus.yml << EOF
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
EOF

    # Create dashboard provisioning
    cat > docker/grafana/provisioning/dashboards/dashboard.yml << EOF
apiVersion: 1

providers:
  - name: 'ClusterFuzz'
    orgId: 1
    folder: ''
    type: file
    disableDeletion: false
    updateIntervalSeconds: 10
    allowUiUpdates: true
    options:
      path: /var/lib/grafana/dashboards
EOF

    # Create a basic dashboard
    cat > docker/grafana/dashboards/clusterfuzz-overview.json << 'EOF'
{
  "dashboard": {
    "id": null,
    "title": "ClusterFuzz Overview",
    "tags": ["clusterfuzz"],
    "style": "dark",
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "HTTP Requests",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_requests_total[5m])",
            "legendFormat": "{{method}} {{uri}}"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 0}
      },
      {
        "id": 2,
        "title": "JVM Memory Usage",
        "type": "graph",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes",
            "legendFormat": "{{area}}"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 0}
      }
    ],
    "time": {"from": "now-1h", "to": "now"},
    "refresh": "5s"
  }
}
EOF
    
    log_success "Grafana provisioning configured"
}

# Build the application
build_application() {
    log_info "Building ClusterFuzz Java application..."
    
    mvn clean compile -B
    
    log_success "Application built successfully"
}

# Run tests
run_tests() {
    log_info "Running tests..."
    
    mvn test -B
    
    log_success "Tests completed"
}

# Start development environment
start_environment() {
    log_info "Starting development environment..."
    
    # Stop any existing containers
    docker-compose down
    
    # Start the environment
    docker-compose up -d postgres redis
    
    # Wait for services to be ready
    log_info "Waiting for services to be ready..."
    sleep 30
    
    # Start application services
    docker-compose up -d clusterfuzz-web clusterfuzz-bot
    
    # Start monitoring services
    docker-compose up -d prometheus grafana nginx
    
    log_success "Development environment started"
}

# Display service URLs
show_urls() {
    log_info "Development environment is ready!"
    echo ""
    echo "Service URLs:"
    echo "  🌐 ClusterFuzz Web API: http://localhost:8080"
    echo "  📊 Swagger UI: http://localhost:8080/swagger-ui/index.html"
    echo "  📈 Grafana Dashboard: http://localhost:3000 (admin/admin)"
    echo "  🔍 Prometheus: http://localhost:9090"
    echo "  🗄️  MinIO Console: http://localhost:9001 (minioadmin/minioadmin)"
    echo "  🔍 Jaeger Tracing: http://localhost:16686"
    echo "  🌐 Nginx Proxy: http://localhost"
    echo ""
    echo "Database connections:"
    echo "  🐘 PostgreSQL: localhost:5432/clusterfuzz (clusterfuzz/clusterfuzz_dev_password)"
    echo "  🔴 Redis: localhost:6379 (password: clusterfuzz_redis_password)"
    echo ""
    echo "Logs and data:"
    echo "  📝 Application logs: ./logs/"
    echo "  💾 Application data: ./data/"
    echo "  🔧 Work directory: ./work/"
    echo "  📁 Corpus files: ./corpus/"
    echo "  💥 Crash files: ./crashes/"
    echo ""
}

# Main execution
main() {
    echo "🚀 ClusterFuzz Java Development Environment Setup"
    echo "=================================================="
    echo ""
    
    check_prerequisites
    create_directories
    generate_ssl_certs
    setup_grafana
    build_application
    run_tests
    start_environment
    show_urls
    
    log_success "Setup completed successfully!"
    echo ""
    echo "To stop the environment, run: docker-compose down"
    echo "To view logs, run: docker-compose logs -f [service-name]"
    echo "To rebuild and restart, run: ./setup-dev.sh"
}

# Handle script arguments
case "${1:-}" in
    "build")
        build_application
        ;;
    "test")
        run_tests
        ;;
    "start")
        start_environment
        show_urls
        ;;
    "stop")
        log_info "Stopping development environment..."
        docker-compose down
        log_success "Environment stopped"
        ;;
    "clean")
        log_info "Cleaning up development environment..."
        docker-compose down -v
        docker system prune -f
        log_success "Environment cleaned"
        ;;
    "logs")
        docker-compose logs -f "${2:-}"
        ;;
    *)
        main
        ;;
esac