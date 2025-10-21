import http from 'k6/http';
import ws from 'k6/ws';
import { check, sleep } from 'k6';
import { Rate, Counter, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const wsConnections = new Counter('websocket_connections');
const wsMessages = new Counter('websocket_messages');
const loginDuration = new Trend('login_duration');

// Test configuration
export const options = {
  stages: [
    { duration: '2m', target: 10 },   // Ramp up to 10 users
    { duration: '5m', target: 10 },   // Stay at 10 users
    { duration: '2m', target: 20 },   // Ramp up to 20 users
    { duration: '5m', target: 20 },   // Stay at 20 users
    { duration: '2m', target: 0 },    // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% of requests must complete below 2s
    http_req_failed: ['rate<0.1'],     // Error rate must be below 10%
    errors: ['rate<0.1'],              // Custom error rate must be below 10%
  },
};

// Base URL configuration
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const WS_URL = __ENV.WS_URL || 'ws://localhost:8080/ws';

// Test data
const testUsers = [
  { username: 'testuser1', password: 'password123', email: 'test1@example.com' },
  { username: 'testuser2', password: 'password123', email: 'test2@example.com' },
  { username: 'testuser3', password: 'password123', email: 'test3@example.com' },
  { username: 'testuser4', password: 'password123', email: 'test4@example.com' },
  { username: 'testuser5', password: 'password123', email: 'test5@example.com' },
];

export function setup() {
  console.log('Setting up test environment...');
  
  // Register test users
  testUsers.forEach(user => {
    const registerPayload = JSON.stringify({
      username: user.username,
      password: user.password,
      email: user.email
    });
    
    const registerResponse = http.post(`${BASE_URL}/api/auth/register`, registerPayload, {
      headers: { 'Content-Type': 'application/json' },
    });
    
    if (registerResponse.status === 201 || registerResponse.status === 409) {
      console.log(`User ${user.username} registered or already exists`);
    } else {
      console.log(`Failed to register user ${user.username}: ${registerResponse.status}`);
    }
  });
  
  return { baseUrl: BASE_URL, wsUrl: WS_URL };
}

export default function (data) {
  const userIndex = __VU % testUsers.length;
  const user = testUsers[userIndex];
  
  // Test user authentication
  testUserLogin(user, data.baseUrl);
  
  // Test API endpoints
  testApiEndpoints(data.baseUrl);
  
  // Test WebSocket connection (for some users)
  if (__VU % 3 === 0) {
    testWebSocketConnection(data.wsUrl);
  }
  
  sleep(1);
}

function testUserLogin(user, baseUrl) {
  const loginPayload = JSON.stringify({
    username: user.username,
    password: user.password
  });
  
  const loginStart = Date.now();
  const loginResponse = http.post(`${baseUrl}/api/auth/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });
  const loginEnd = Date.now();
  
  loginDuration.add(loginEnd - loginStart);
  
  const loginSuccess = check(loginResponse, {
    'login status is 200': (r) => r.status === 200,
    'login response has token': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.token !== undefined;
      } catch (e) {
        return false;
      }
    },
  });
  
  if (!loginSuccess) {
    errorRate.add(1);
    console.log(`Login failed for ${user.username}: ${loginResponse.status} ${loginResponse.body}`);
    return null;
  }
  
  const token = JSON.parse(loginResponse.body).token;
  return token;
}

function testApiEndpoints(baseUrl) {
  // Test health endpoint
  const healthResponse = http.get(`${baseUrl}/api/health`);
  check(healthResponse, {
    'health endpoint status is 200': (r) => r.status === 200,
  });
  
  // Test market data endpoint
  const marketResponse = http.get(`${baseUrl}/api/market/data`);
  check(marketResponse, {
    'market data status is 200 or 401': (r) => r.status === 200 || r.status === 401,
  });
  
  // Test user profile endpoint (should require auth)
  const profileResponse = http.get(`${baseUrl}/api/user/profile`);
  check(profileResponse, {
    'profile endpoint returns 401 without auth': (r) => r.status === 401,
  });
}

function testWebSocketConnection(wsUrl) {
  const response = ws.connect(wsUrl, {}, function (socket) {
    wsConnections.add(1);
    
    socket.on('open', function open() {
      console.log('WebSocket connection opened');
      
      // Send a test message
      socket.send(JSON.stringify({
        type: 'ping',
        timestamp: Date.now()
      }));
      wsMessages.add(1);
    });
    
    socket.on('message', function message(data) {
      console.log('Received WebSocket message:', data);
      wsMessages.add(1);
    });
    
    socket.on('close', function close() {
      console.log('WebSocket connection closed');
    });
    
    socket.on('error', function error(e) {
      console.log('WebSocket error:', e);
      errorRate.add(1);
    });
    
    // Keep connection open for a short time
    sleep(2);
    socket.close();
  });
  
  check(response, {
    'WebSocket connection successful': (r) => r && r.status === 101,
  });
}

export function teardown(data) {
  console.log('Cleaning up test environment...');
  // Add any cleanup logic here if needed
}