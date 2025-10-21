// k6 Configuration File for TradingSim Load Testing

export const environments = {
  local: {
    baseUrl: 'http://localhost:8080',
    wsUrl: 'ws://localhost:8080/ws',
    maxVUs: 50,
    duration: '5m'
  },
  staging: {
    baseUrl: 'http://staging.tradingsim.com',
    wsUrl: 'ws://staging.tradingsim.com/ws',
    maxVUs: 100,
    duration: '10m'
  },
  production: {
    baseUrl: 'https://api.tradingsim.com',
    wsUrl: 'wss://api.tradingsim.com/ws',
    maxVUs: 200,
    duration: '15m'
  }
};

export const testProfiles = {
  smoke: {
    stages: [
      { duration: '30s', target: 1 },
      { duration: '1m', target: 1 },
      { duration: '30s', target: 0 }
    ],
    thresholds: {
      http_req_duration: ['p(95)<2000'],
      http_req_failed: ['rate<0.1']
    }
  },
  
  load: {
    stages: [
      { duration: '2m', target: 10 },
      { duration: '5m', target: 10 },
      { duration: '2m', target: 20 },
      { duration: '5m', target: 20 },
      { duration: '2m', target: 0 }
    ],
    thresholds: {
      http_req_duration: ['p(95)<3000'],
      http_req_failed: ['rate<0.1'],
      checks: ['rate>0.9']
    }
  },
  
  stress: {
    stages: [
      { duration: '2m', target: 20 },
      { duration: '5m', target: 20 },
      { duration: '2m', target: 50 },
      { duration: '5m', target: 50 },
      { duration: '2m', target: 100 },
      { duration: '5m', target: 100 },
      { duration: '2m', target: 0 }
    ],
    thresholds: {
      http_req_duration: ['p(95)<5000'],
      http_req_failed: ['rate<0.2']
    }
  },
  
  spike: {
    stages: [
      { duration: '1m', target: 10 },
      { duration: '30s', target: 100 },
      { duration: '1m', target: 10 },
      { duration: '30s', target: 200 },
      { duration: '1m', target: 10 },
      { duration: '30s', target: 0 }
    ],
    thresholds: {
      http_req_duration: ['p(95)<10000'],
      http_req_failed: ['rate<0.3']
    }
  },
  
  soak: {
    stages: [
      { duration: '5m', target: 20 },
      { duration: '30m', target: 20 },
      { duration: '5m', target: 0 }
    ],
    thresholds: {
      http_req_duration: ['p(95)<3000'],
      http_req_failed: ['rate<0.1']
    }
  }
};

export const commonThresholds = {
  // HTTP metrics
  http_req_duration: ['p(95)<3000', 'p(99)<5000'],
  http_req_failed: ['rate<0.1'],
  http_reqs: ['rate>10'],
  
  // Custom metrics
  errors: ['rate<0.1'],
  checks: ['rate>0.9'],
  
  // WebSocket metrics
  ws_connection_errors: ['rate<0.1'],
  ws_message_errors: ['rate<0.05'],
  ws_message_latency: ['p(95)<500'],
  
  // Business metrics
  login_duration: ['p(95)<2000'],
  game_session_duration: ['p(90)>10000'],
  decision_response_time: ['p(95)<1000']
};

export const outputOptions = {
  json: './results/test-results.json',
  influxdb: {
    url: 'http://localhost:8086',
    database: 'k6',
    tags: {
      testType: 'load-test',
      environment: 'local'
    }
  },
  prometheus: {
    url: 'http://localhost:9090/api/v1/write'
  }
};

// Helper function to get configuration based on environment and profile
export function getConfig(env = 'local', profile = 'load') {
  const environment = environments[env] || environments.local;
  const testProfile = testProfiles[profile] || testProfiles.load;
  
  return {
    ...environment,
    ...testProfile,
    thresholds: {
      ...commonThresholds,
      ...testProfile.thresholds
    }
  };
}