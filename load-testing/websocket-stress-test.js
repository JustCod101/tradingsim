import ws from 'k6/ws';
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter, Trend } from 'k6/metrics';

// Custom metrics for WebSocket testing
const wsConnectionErrors = new Rate('ws_connection_errors');
const wsMessageErrors = new Rate('ws_message_errors');
const wsConnections = new Counter('ws_connections_total');
const wsMessagesReceived = new Counter('ws_messages_received');
const wsMessagesSent = new Counter('ws_messages_sent');
const wsConnectionDuration = new Trend('ws_connection_duration');
const wsMessageLatency = new Trend('ws_message_latency');

// Test configuration for WebSocket stress testing
export const options = {
  stages: [
    { duration: '30s', target: 10 },   // Ramp up to 10 concurrent connections
    { duration: '2m', target: 10 },    // Maintain 10 connections
    { duration: '30s', target: 25 },   // Ramp up to 25 connections
    { duration: '2m', target: 25 },    // Maintain 25 connections
    { duration: '30s', target: 50 },   // Ramp up to 50 connections
    { duration: '1m', target: 50 },    // Maintain 50 connections
    { duration: '30s', target: 0 },    // Ramp down
  ],
  thresholds: {
    ws_connection_errors: ['rate<0.1'],     // Less than 10% connection errors
    ws_message_errors: ['rate<0.05'],       // Less than 5% message errors
    ws_message_latency: ['p(95)<500'],      // 95% of messages under 500ms latency
    ws_connection_duration: ['p(90)>10000'], // 90% of connections last more than 10s
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const WS_URL = __ENV.WS_URL || 'ws://localhost:8080/ws';

// WebSocket message types for testing
const messageTypes = [
  { type: 'ping', data: {} },
  { type: 'subscribe_market', data: { symbols: ['AAPL', 'GOOGL', 'MSFT'] } },
  { type: 'get_portfolio', data: {} },
  { type: 'market_data_request', data: { symbol: 'AAPL' } },
  { type: 'heartbeat', data: { timestamp: Date.now() } }
];

export function setup() {
  console.log('Setting up WebSocket stress test...');
  
  // Create test users for WebSocket connections
  const wsUsers = [];
  for (let i = 1; i <= 60; i++) {
    const user = {
      username: `wsuser${i}`,
      password: 'wspass123',
      email: `wsuser${i}@example.com`
    };
    
    const registerPayload = JSON.stringify(user);
    const registerResponse = http.post(`${BASE_URL}/api/auth/register`, registerPayload, {
      headers: { 'Content-Type': 'application/json' },
    });
    
    if (registerResponse.status === 201 || registerResponse.status === 409) {
      wsUsers.push(user);
    }
  }
  
  return { baseUrl: BASE_URL, wsUrl: WS_URL, users: wsUsers };
}

export default function (data) {
  const userIndex = (__VU - 1) % data.users.length;
  const user = data.users[userIndex];
  
  // Login to get authentication token
  const token = loginUser(user, data.baseUrl);
  if (!token) {
    return;
  }
  
  // Establish WebSocket connection with authentication
  testWebSocketConnection(data.wsUrl, token);
}

function loginUser(user, baseUrl) {
  const loginPayload = JSON.stringify({
    username: user.username,
    password: user.password
  });
  
  const loginResponse = http.post(`${baseUrl}/api/auth/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });
  
  const loginSuccess = check(loginResponse, {
    'WebSocket user login successful': (r) => r.status === 200,
  });
  
  if (!loginSuccess) {
    console.log(`Login failed for ${user.username}: ${loginResponse.status}`);
    return null;
  }
  
  return JSON.parse(loginResponse.body).token;
}

function testWebSocketConnection(wsUrl, token) {
  const connectionStart = Date.now();
  let messagesSent = 0;
  let messagesReceived = 0;
  let connectionEstablished = false;
  
  // Add token to WebSocket URL as query parameter
  const authenticatedWsUrl = `${wsUrl}?token=${token}`;
  
  const response = ws.connect(authenticatedWsUrl, {}, function (socket) {
    wsConnections.add(1);
    connectionEstablished = true;
    
    socket.on('open', function open() {
      console.log(`WebSocket connection opened for VU ${__VU}`);
      
      // Send initial subscription message
      const subscribeMessage = {
        type: 'subscribe',
        data: {
          channels: ['market_data', 'portfolio_updates'],
          symbols: ['AAPL', 'GOOGL', 'MSFT', 'TSLA']
        },
        timestamp: Date.now()
      };
      
      socket.send(JSON.stringify(subscribeMessage));
      wsMessagesSent.add(1);
      messagesSent++;
      
      // Set up periodic message sending
      const messageInterval = setInterval(() => {
        if (socket.readyState === WebSocket.OPEN) {
          const messageType = messageTypes[Math.floor(Math.random() * messageTypes.length)];
          const message = {
            ...messageType,
            id: `msg_${Date.now()}_${Math.random()}`,
            timestamp: Date.now()
          };
          
          const sendStart = Date.now();
          socket.send(JSON.stringify(message));
          wsMessagesSent.add(1);
          messagesSent++;
          
          // Store send time for latency calculation
          message.sendTime = sendStart;
        }
      }, 1000 + Math.random() * 2000); // Send message every 1-3 seconds
      
      // Clean up interval after connection duration
      setTimeout(() => {
        clearInterval(messageInterval);
      }, 15000); // Stop sending after 15 seconds
    });
    
    socket.on('message', function message(data) {
      try {
        const parsedMessage = JSON.parse(data);
        messagesReceived++;
        wsMessagesReceived.add(1);
        
        // Calculate message latency if it's a response to our message
        if (parsedMessage.id && parsedMessage.timestamp) {
          const latency = Date.now() - parsedMessage.timestamp;
          wsMessageLatency.add(latency);
        }
        
        // Handle different message types
        switch (parsedMessage.type) {
          case 'market_update':
            console.log(`Received market update: ${parsedMessage.data.symbol}`);
            break;
          case 'portfolio_update':
            console.log(`Received portfolio update`);
            break;
          case 'pong':
            console.log(`Received pong response`);
            break;
          case 'error':
            console.log(`Received error: ${parsedMessage.message}`);
            wsMessageErrors.add(1);
            break;
        }
      } catch (e) {
        console.log(`Failed to parse WebSocket message: ${e.message}`);
        wsMessageErrors.add(1);
      }
    });
    
    socket.on('close', function close() {
      const connectionEnd = Date.now();
      const connectionDuration = connectionEnd - connectionStart;
      wsConnectionDuration.add(connectionDuration);
      
      console.log(`WebSocket connection closed for VU ${__VU}`);
      console.log(`Connection duration: ${connectionDuration}ms`);
      console.log(`Messages sent: ${messagesSent}, received: ${messagesReceived}`);
    });
    
    socket.on('error', function error(e) {
      console.log(`WebSocket error for VU ${__VU}: ${e.error()}`);
      wsConnectionErrors.add(1);
    });
    
    // Keep connection alive for a random duration between 10-20 seconds
    const connectionDuration = 10000 + Math.random() * 10000;
    sleep(connectionDuration / 1000);
    
    socket.close();
  });
  
  // Check if connection was established successfully
  const connectionSuccess = check(response, {
    'WebSocket connection established': (r) => r && r.status === 101,
  });
  
  if (!connectionSuccess) {
    wsConnectionErrors.add(1);
    console.log(`Failed to establish WebSocket connection for VU ${__VU}`);
  }
}

export function teardown(data) {
  console.log('WebSocket stress test completed');
  console.log(`Total WebSocket connections: ${wsConnections.count}`);
  console.log(`Total messages sent: ${wsMessagesSent.count}`);
  console.log(`Total messages received: ${wsMessagesReceived.count}`);
}