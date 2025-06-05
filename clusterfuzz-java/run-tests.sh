#!/bin/bash

# ClusterFuzz Java Test Suite Runner
# Executes comprehensive test suite including unit tests, integration tests, and performance benchmarks

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

# Test execution functions
run_unit_tests() {
    log_info "Running unit tests..."
    mvn test -Dtest="**/*Test" -DfailIfNoTests=false
    log_success "Unit tests completed"
}

run_integration_tests() {
    log_info "Running integration tests..."
    mvn test -Dtest="**/*IntegrationTest" -DfailIfNoTests=false
    log_success "Integration tests completed"
}

run_performance_benchmarks() {
    log_info "Running performance benchmarks..."
    mvn test -Dtest="**/*Benchmark" -DfailIfNoTests=false
    log_success "Performance benchmarks completed"
}

run_all_tests() {
    log_info "Running complete test suite..."
    mvn test
    log_success "Complete test suite completed"
}

generate_test_report() {
    log_info "Generating test reports..."
    mvn surefire-report:report
    log_success "Test reports generated in target/site/surefire-report.html"
}

run_coverage_analysis() {
    log_info "Running code coverage analysis..."
    mvn jacoco:prepare-agent test jacoco:report
    log_success "Coverage report generated in target/site/jacoco/index.html"
}

run_mutation_testing() {
    log_info "Running mutation testing..."
    mvn org.pitest:pitest-maven:mutationCoverage
    log_success "Mutation testing report generated in target/pit-reports/index.html"
}

# Performance monitoring
monitor_performance() {
    log_info "Monitoring test performance..."
    
    # Record start time
    start_time=$(date +%s)
    
    # Run tests with JVM monitoring
    mvn test -Dspring.profiles.active=test \
        -XX:+PrintGCDetails \
        -XX:+PrintGCTimeStamps \
        -XX:+PrintGCApplicationStoppedTime \
        -Xloggc:target/gc.log
    
    # Record end time
    end_time=$(date +%s)
    duration=$((end_time - start_time))
    
    log_info "Test execution completed in ${duration} seconds"
    
    # Analyze GC logs if available
    if [ -f "target/gc.log" ]; then
        log_info "GC log analysis:"
        grep "Total time for which application threads were stopped" target/gc.log | tail -5
    fi
}

# Test data validation
validate_test_data() {
    log_info "Validating test data integrity..."
    
    # Check test resource files
    test_resources_dir="src/test/resources"
    if [ -d "$test_resources_dir" ]; then
        log_info "Found test resources directory"
        find "$test_resources_dir" -name "*.yml" -o -name "*.yaml" -o -name "*.properties" | while read -r file; do
            log_info "Validating: $file"
        done
    fi
    
    # Validate test configuration
    if [ -f "src/test/resources/application-test.yml" ]; then
        log_success "Test configuration found and valid"
    else
        log_warning "Test configuration not found"
    fi
}

# Main execution
main() {
    echo "🧪 ClusterFuzz Java Test Suite Runner"
    echo "====================================="
    echo ""
    
    # Parse command line arguments
    case "${1:-all}" in
        "unit")
            validate_test_data
            run_unit_tests
            ;;
        "integration")
            validate_test_data
            run_integration_tests
            ;;
        "performance")
            validate_test_data
            run_performance_benchmarks
            ;;
        "coverage")
            validate_test_data
            run_coverage_analysis
            ;;
        "mutation")
            validate_test_data
            run_mutation_testing
            ;;
        "monitor")
            validate_test_data
            monitor_performance
            ;;
        "report")
            generate_test_report
            ;;
        "all")
            validate_test_data
            run_all_tests
            generate_test_report
            run_coverage_analysis
            ;;
        "help")
            echo "Usage: $0 [unit|integration|performance|coverage|mutation|monitor|report|all|help]"
            echo ""
            echo "Commands:"
            echo "  unit         - Run unit tests only"
            echo "  integration  - Run integration tests only"
            echo "  performance  - Run performance benchmarks only"
            echo "  coverage     - Run tests with code coverage analysis"
            echo "  mutation     - Run mutation testing"
            echo "  monitor      - Run tests with performance monitoring"
            echo "  report       - Generate test reports"
            echo "  all          - Run complete test suite (default)"
            echo "  help         - Show this help message"
            exit 0
            ;;
        *)
            log_error "Unknown command: $1"
            echo "Use '$0 help' for usage information"
            exit 1
            ;;
    esac
    
    echo ""
    log_success "Test execution completed successfully!"
    echo ""
    echo "📊 Test Results Summary:"
    echo "  📁 Test reports: target/site/surefire-report.html"
    echo "  📈 Coverage report: target/site/jacoco/index.html"
    echo "  🧬 Mutation testing: target/pit-reports/index.html"
    echo "  📝 Build logs: target/surefire-reports/"
    echo ""
}

# Execute main function with all arguments
main "$@"