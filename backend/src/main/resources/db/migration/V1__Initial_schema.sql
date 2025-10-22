-- 游戏会话表
CREATE TABLE IF NOT EXISTS game_session (
    id VARCHAR(64) PRIMARY KEY,
    stock_code VARCHAR(20) NOT NULL,
    timeframe VARCHAR(10) NOT NULL,
    initial_balance DECIMAL(15,2) NOT NULL,
    current_balance DECIMAL(15,2) NOT NULL,
    total_pnl DECIMAL(15,2) DEFAULT 0.00,
    max_drawdown DECIMAL(15,2) DEFAULT 0.00,
    win_rate DECIMAL(5,2) DEFAULT 0.00,
    score DECIMAL(10,2) DEFAULT 0.00,
    current_frame_index INTEGER DEFAULT 0,
    total_frames INTEGER DEFAULT 0,
    total_trades INTEGER DEFAULT 0,
    winning_trades INTEGER DEFAULT 0,
    losing_trades INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 游戏决策表
CREATE TABLE IF NOT EXISTS game_decision (
    id VARCHAR(64) PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    frame_index INTEGER NOT NULL,
    decision_type VARCHAR(20) NOT NULL,
    price DECIMAL(10,2),
    quantity INTEGER,
    response_time_ms BIGINT,
    pnl DECIMAL(15,2) DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES game_session(id) ON DELETE CASCADE
);

-- 市场数据表
CREATE TABLE IF NOT EXISTS market_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_code VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    open_price DECIMAL(10,2) NOT NULL,
    high_price DECIMAL(10,2) NOT NULL,
    low_price DECIMAL(10,2) NOT NULL,
    close_price DECIMAL(10,2) NOT NULL,
    volume BIGINT NOT NULL,
    timeframe VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_stock_timestamp (stock_code, timestamp),
    INDEX idx_timeframe (timeframe)
);

-- 用户统计表
CREATE TABLE IF NOT EXISTS user_stats (
    user_id VARCHAR(64) PRIMARY KEY,
    total_sessions INTEGER DEFAULT 0,
    completed_sessions INTEGER DEFAULT 0,
    total_score DECIMAL(15,2) DEFAULT 0.00,
    best_score DECIMAL(10,2) DEFAULT 0.00,
    total_pnl DECIMAL(15,2) DEFAULT 0.00,
    win_rate DECIMAL(5,2) DEFAULT 0.00,
    avg_response_time_ms BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_game_session_status ON game_session(status);
CREATE INDEX IF NOT EXISTS idx_game_session_created_at ON game_session(created_at);
CREATE INDEX IF NOT EXISTS idx_game_decision_session_id ON game_decision(session_id);
CREATE INDEX IF NOT EXISTS idx_game_decision_frame_index ON game_decision(frame_index);