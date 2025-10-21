#!/bin/bash

# TradingSim Load Testing Script
# This script runs various load tests against the TradingSim application

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default configuration
BASE_URL="http://localhost:8080"
WS_URL="ws://localhost:8080/ws"
RESULTS_DIR="./results"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if k6 is installed
check_k6() {
    if ! command -v k6 &> /dev/null; then
        print_error "k6 is not installed. Please install k6 first:"
        echo "  macOS: brew install k6"
        echo "  Linux: sudo apt-get install k6"
        echo "  Windows: choco install k6"
        echo "  Or download from: https://k6.io/docs/getting-started/installation/"
        exit 1
    fi
    print_success "k6 is installed: $(k6 version)"
}

# Function to check if backend is running
check_backend() {
    print_status "Checking if backend is running at $BASE_URL..."
    
    if curl -s -f "$BASE_URL/api/health" > /dev/null 2>&1; then
        print_success "Backend is running and healthy"
    else
        print_error "Backend is not running or not healthy at $BASE_URL"
        print_warning "Please start the backend service before running load tests"
        exit 1
    fi
}

# Function to create results directory
setup_results_dir() {
    mkdir -p "$RESULTS_DIR"
    print_status "Results will be saved to: $RESULTS_DIR"
}

# Function to run a specific test
run_test() {
    local test_name=$1
    local test_file=$2
    local output_file="$RESULTS_DIR/${test_name}_${TIMESTAMP}.json"
    
    print_status "Running $test_name..."
    print_status "Test file: $test_file"
    print_status "Output file: $output_file"
    
    if k6 run \
        --env BASE_URL="$BASE_URL" \
        --env WS_URL="$WS_URL" \
        --out json="$output_file" \
        "$test_file"; then
        print_success "$test_name completed successfully"
        print_status "Results saved to: $output_file"
    else
        print_error "$test_name failed"
        return 1
    fi
}

# Function to generate summary report
generate_summary() {
    local summary_file="$RESULTS_DIR/test_summary_${TIMESTAMP}.txt"
    
    print_status "Generating test summary..."
    
    cat > "$summary_file" << EOF
TradingSim Load Testing Summary
==============================
Test Run: $TIMESTAMP
Base URL: $BASE_URL
WebSocket URL: $WS_URL

Test Results:
EOF

    for result_file in "$RESULTS_DIR"/*_"$TIMESTAMP".json; do
        if [[ -f "$result_file" ]]; then
            local test_name=$(basename "$result_file" | sed "s/_${TIMESTAMP}.json//")
            echo "- $test_name: $(basename "$result_file")" >> "$summary_file"
        fi
    done
    
    cat >> "$summary_file" << EOF

To analyze results:
1. Use k6's built-in analysis tools
2. Import JSON files into Grafana
3. Use k6 Cloud for detailed analysis

For more information, visit: https://k6.io/docs/results-visualization/
EOF

    print_success "Summary generated: $summary_file"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS] [TEST_TYPE]"
    echo ""
    echo "Options:"
    echo "  -u, --url URL          Backend base URL (default: $BASE_URL)"
    echo "  -w, --ws-url URL       WebSocket URL (default: $WS_URL)"
    echo "  -o, --output DIR       Results output directory (default: $RESULTS_DIR)"
    echo "  -h, --help             Show this help message"
    echo ""
    echo "Test Types:"
    echo "  basic                  Run basic load test"
    echo "  game                   Run game session test"
    echo "  websocket              Run WebSocket stress test"
    echo "  all                    Run all tests (default)"
    echo ""
    echo "Examples:"
    echo "  $0                     # Run all tests with default settings"
    echo "  $0 basic               # Run only basic load test"
    echo "  $0 -u http://prod.example.com all  # Run all tests against production"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -u|--url)
            BASE_URL="$2"
            shift 2
            ;;
        -w|--ws-url)
            WS_URL="$2"
            shift 2
            ;;
        -o|--output)
            RESULTS_DIR="$2"
            shift 2
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        basic|game|websocket|all)
            TEST_TYPE="$1"
            shift
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Set default test type if not specified
TEST_TYPE=${TEST_TYPE:-all}

# Main execution
main() {
    print_status "Starting TradingSim Load Testing"
    print_status "Target URL: $BASE_URL"
    print_status "WebSocket URL: $WS_URL"
    print_status "Test Type: $TEST_TYPE"
    
    # Pre-flight checks
    check_k6
    check_backend
    setup_results_dir
    
    # Run tests based on type
    case $TEST_TYPE in
        basic)
            run_test "basic-load-test" "basic-load-test.js"
            ;;
        game)
            run_test "game-session-test" "game-session-test.js"
            ;;
        websocket)
            run_test "websocket-stress-test" "websocket-stress-test.js"
            ;;
        all)
            print_status "Running all load tests..."
            run_test "basic-load-test" "basic-load-test.js"
            sleep 5  # Brief pause between tests
            run_test "game-session-test" "game-session-test.js"
            sleep 5
            run_test "websocket-stress-test" "websocket-stress-test.js"
            ;;
    esac
    
    # Generate summary
    generate_summary
    
    print_success "Load testing completed!"
    print_status "Check the results in: $RESULTS_DIR"
}

# Run main function
main