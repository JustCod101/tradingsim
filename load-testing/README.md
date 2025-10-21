# TradingSim Load Testing

This directory contains comprehensive load testing scripts for the TradingSim application using [k6](https://k6.io/).

## Prerequisites

1. **Install k6**:
   ```bash
   # macOS
   brew install k6
   
   # Ubuntu/Debian
   sudo apt-get install k6
   
   # Windows
   choco install k6
   ```

2. **Ensure TradingSim backend is running**:
   - Backend should be accessible at `http://localhost:8080`
   - WebSocket endpoint should be available at `ws://localhost:8080/ws`

## Test Scripts

### 1. Basic Load Test (`basic-load-test.js`)
Tests fundamental application functionality:
- User registration and authentication
- API endpoint responses
- Basic WebSocket connections
- Error handling

**Usage**:
```bash
k6 run basic-load-test.js
```

### 2. Game Session Test (`game-session-test.js`)
Focuses on game-specific functionality:
- Game session creation and management
- Trading decision processing
- Session completion workflows
- Business logic performance

**Usage**:
```bash
k6 run game-session-test.js
```

### 3. WebSocket Stress Test (`websocket-stress-test.js`)
Intensive WebSocket testing:
- Concurrent connection handling
- Message throughput and latency
- Connection stability under load
- Real-time data streaming

**Usage**:
```bash
k6 run websocket-stress-test.js
```

## Quick Start

### Run All Tests
```bash
./run-tests.sh
```

### Run Specific Test
```bash
./run-tests.sh basic      # Basic load test only
./run-tests.sh game       # Game session test only
./run-tests.sh websocket  # WebSocket stress test only
```

### Custom Configuration
```bash
./run-tests.sh -u http://staging.example.com -w ws://staging.example.com/ws all
```

## Test Profiles

The tests support different load profiles defined in `k6.config.js`:

- **Smoke Test**: Minimal load to verify basic functionality
- **Load Test**: Normal expected load
- **Stress Test**: Above-normal load to find breaking points
- **Spike Test**: Sudden load increases
- **Soak Test**: Extended duration testing

### Using Test Profiles
```bash
# Run with specific profile
k6 run --env PROFILE=stress basic-load-test.js

# Run with specific environment
k6 run --env ENV=staging --env PROFILE=load game-session-test.js
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `BASE_URL` | Backend API base URL | `http://localhost:8080` |
| `WS_URL` | WebSocket endpoint URL | `ws://localhost:8080/ws` |
| `ENV` | Environment profile (local/staging/production) | `local` |
| `PROFILE` | Test profile (smoke/load/stress/spike/soak) | `load` |

## Results and Monitoring

### Result Files
Test results are saved to the `./results/` directory:
- JSON format for detailed analysis
- Summary reports with key metrics
- Timestamped files for historical comparison

### Metrics Tracked

#### HTTP Metrics
- Request duration (p95, p99)
- Request failure rate
- Requests per second

#### WebSocket Metrics
- Connection establishment time
- Message latency
- Connection errors
- Message throughput

#### Business Metrics
- User login duration
- Game session creation time
- Trading decision response time
- Portfolio update frequency

### Integration with Monitoring

#### Prometheus Integration
```bash
k6 run --out prometheus=http://localhost:9090/api/v1/write basic-load-test.js
```

#### InfluxDB Integration
```bash
k6 run --out influxdb=http://localhost:8086/k6 basic-load-test.js
```

#### Grafana Dashboards
Import the provided Grafana dashboards to visualize:
- Real-time test metrics
- Historical performance trends
- System resource utilization during tests

## Test Scenarios

### Scenario 1: User Onboarding Flow
```bash
# Test user registration and first game session
k6 run --env SCENARIO=onboarding basic-load-test.js
```

### Scenario 2: Peak Trading Hours
```bash
# Simulate high concurrent trading activity
k6 run --env PROFILE=stress game-session-test.js
```

### Scenario 3: Real-time Data Streaming
```bash
# Test WebSocket performance under load
k6 run --env PROFILE=soak websocket-stress-test.js
```

## Troubleshooting

### Common Issues

1. **Connection Refused**:
   - Ensure backend is running
   - Check firewall settings
   - Verify URL configuration

2. **Authentication Failures**:
   - Check user registration process
   - Verify token handling
   - Review API endpoint authentication

3. **WebSocket Connection Issues**:
   - Confirm WebSocket endpoint is accessible
   - Check for proxy/firewall blocking WebSocket upgrades
   - Verify authentication token format

### Debug Mode
```bash
# Run with verbose logging
k6 run --verbose basic-load-test.js

# Run with HTTP debug
k6 run --http-debug basic-load-test.js
```

## Performance Baselines

### Expected Performance Targets

| Metric | Target | Critical |
|--------|--------|----------|
| API Response Time (p95) | < 2s | < 5s |
| WebSocket Message Latency (p95) | < 500ms | < 1s |
| Error Rate | < 5% | < 10% |
| Concurrent Users | 50+ | 20+ |
| Game Sessions/minute | 100+ | 50+ |

### Load Test Recommendations

1. **Development**: Run smoke tests on every build
2. **Staging**: Run full load tests before deployment
3. **Production**: Schedule regular soak tests during low-traffic periods

## Contributing

When adding new test scenarios:

1. Follow the existing code structure
2. Add appropriate metrics and thresholds
3. Update this README with new scenarios
4. Test locally before committing

## Resources

- [k6 Documentation](https://k6.io/docs/)
- [k6 Examples](https://github.com/grafana/k6/tree/master/examples)
- [Performance Testing Best Practices](https://k6.io/docs/testing-guides/)