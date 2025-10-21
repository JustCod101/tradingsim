import http from 'k6/http';
import ws from 'k6/ws';
import { check, sleep } from 'k6';
import { Rate, Counter, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const gameSessionsCreated = new Counter('game_sessions_created');
const gameDecisionsMade = new Counter('game_decisions_made');
const gameSessionDuration = new Trend('game_session_duration');
const decisionResponseTime = new Trend('decision_response_time');

// Test configuration for game session load testing
export const options = {
  stages: [
    { duration: '1m', target: 5 },    // Ramp up to 5 concurrent game sessions
    { duration: '3m', target: 5 },    // Maintain 5 sessions
    { duration: '1m', target: 10 },   // Ramp up to 10 sessions
    { duration: '3m', target: 10 },   // Maintain 10 sessions
    { duration: '1m', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'], // 95% of requests under 3s
    http_req_failed: ['rate<0.05'],    // Error rate under 5%
    decision_response_time: ['p(95)<1000'], // Decision response under 1s
    errors: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const WS_URL = __ENV.WS_URL || 'ws://localhost:8080/ws';

// Game test scenarios
const gameScenarios = [
  {
    name: 'Conservative Trader',
    decisions: ['buy_low_risk', 'hold', 'sell_profit'],
    riskTolerance: 'low'
  },
  {
    name: 'Aggressive Trader',
    decisions: ['buy_high_risk', 'buy_more', 'sell_loss'],
    riskTolerance: 'high'
  },
  {
    name: 'Balanced Trader',
    decisions: ['buy_medium_risk', 'hold', 'sell_profit'],
    riskTolerance: 'medium'
  }
];

export function setup() {
  console.log('Setting up game session test environment...');
  
  // Create test users for game sessions
  const gameUsers = [];
  for (let i = 1; i <= 15; i++) {
    const user = {
      username: `gameuser${i}`,
      password: 'gamepass123',
      email: `gameuser${i}@example.com`
    };
    
    const registerPayload = JSON.stringify(user);
    const registerResponse = http.post(`${BASE_URL}/api/auth/register`, registerPayload, {
      headers: { 'Content-Type': 'application/json' },
    });
    
    if (registerResponse.status === 201 || registerResponse.status === 409) {
      gameUsers.push(user);
    }
  }
  
  return { baseUrl: BASE_URL, wsUrl: WS_URL, users: gameUsers };
}

export default function (data) {
  const userIndex = (__VU - 1) % data.users.length;
  const user = data.users[userIndex];
  const scenario = gameScenarios[__VU % gameScenarios.length];
  
  // Login user
  const token = loginUser(user, data.baseUrl);
  if (!token) {
    return;
  }
  
  // Start a game session
  const sessionId = startGameSession(token, scenario, data.baseUrl);
  if (!sessionId) {
    return;
  }
  
  // Play the game session
  playGameSession(token, sessionId, scenario, data.baseUrl);
  
  sleep(1);
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
    'game user login successful': (r) => r.status === 200,
  });
  
  if (!loginSuccess) {
    errorRate.add(1);
    return null;
  }
  
  return JSON.parse(loginResponse.body).token;
}

function startGameSession(token, scenario, baseUrl) {
  const sessionPayload = JSON.stringify({
    gameType: 'trading_simulation',
    difficulty: scenario.riskTolerance,
    initialBalance: 10000
  });
  
  const sessionStart = Date.now();
  const sessionResponse = http.post(`${baseUrl}/api/game/session/start`, sessionPayload, {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
  });
  
  const sessionSuccess = check(sessionResponse, {
    'game session created': (r) => r.status === 201,
    'session response has id': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.sessionId !== undefined;
      } catch (e) {
        return false;
      }
    },
  });
  
  if (!sessionSuccess) {
    errorRate.add(1);
    console.log(`Failed to create game session: ${sessionResponse.status} ${sessionResponse.body}`);
    return null;
  }
  
  gameSessionsCreated.add(1);
  const sessionId = JSON.parse(sessionResponse.body).sessionId;
  return sessionId;
}

function playGameSession(token, sessionId, scenario, baseUrl) {
  const gameStartTime = Date.now();
  
  // Make several trading decisions
  for (let i = 0; i < scenario.decisions.length; i++) {
    const decision = scenario.decisions[i];
    
    // Get current market state
    const marketResponse = http.get(`${baseUrl}/api/game/session/${sessionId}/market`, {
      headers: { 'Authorization': `Bearer ${token}` },
    });
    
    check(marketResponse, {
      'market data retrieved': (r) => r.status === 200,
    });
    
    // Make a trading decision
    const decisionStart = Date.now();
    const decisionPayload = JSON.stringify({
      action: decision,
      symbol: 'AAPL',
      quantity: Math.floor(Math.random() * 10) + 1,
      timestamp: Date.now()
    });
    
    const decisionResponse = http.post(`${baseUrl}/api/game/session/${sessionId}/decision`, decisionPayload, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
    });
    
    const decisionEnd = Date.now();
    decisionResponseTime.add(decisionEnd - decisionStart);
    
    const decisionSuccess = check(decisionResponse, {
      'trading decision accepted': (r) => r.status === 200,
    });
    
    if (decisionSuccess) {
      gameDecisionsMade.add(1);
    } else {
      errorRate.add(1);
    }
    
    // Wait between decisions
    sleep(Math.random() * 2 + 1);
  }
  
  // End the game session
  const endResponse = http.post(`${baseUrl}/api/game/session/${sessionId}/end`, null, {
    headers: { 'Authorization': `Bearer ${token}` },
  });
  
  check(endResponse, {
    'game session ended': (r) => r.status === 200,
  });
  
  const gameEndTime = Date.now();
  gameSessionDuration.add(gameEndTime - gameStartTime);
}

export function teardown(data) {
  console.log('Game session test completed');
  console.log(`Total game sessions created: ${gameSessionsCreated.count}`);
  console.log(`Total decisions made: ${gameDecisionsMade.count}`);
}