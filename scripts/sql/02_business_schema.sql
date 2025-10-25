-- 业务数据表结构
-- 创建时间: 2024-01-01
-- 用途: 存储游戏会话、决策、奖励等业务数据

-- 创建枚举类型
CREATE TYPE session_status AS ENUM ('CREATED', 'RUNNING', 'PAUSED', 'COMPLETED', 'CANCELLED');
CREATE TYPE decision_type AS ENUM ('LONG', 'SHORT');

-- 游戏会话表 (根据GameSession实体类)
CREATE TABLE IF NOT EXISTS game_session (
    id VARCHAR(50) PRIMARY KEY,                    -- 会话ID
    stock_code VARCHAR(20) NOT NULL,               -- 股票代码
    timeframe VARCHAR(20) NOT NULL,                -- 时间框架
    start_time TIMESTAMPTZ,                        -- 开始时间
    end_time TIMESTAMPTZ,                          -- 结束时间
    status session_status DEFAULT 'CREATED',       -- 会话状态
    current_frame_index INTEGER DEFAULT 0,         -- 当前帧索引
    total_frames INTEGER,                          -- 总帧数
    initial_balance DECIMAL(19,4),                 -- 初始余额
    current_balance DECIMAL(19,4),                 -- 当前余额
    total_pnl DECIMAL(19,4),                       -- 总盈亏
    max_drawdown DECIMAL(19,4),                    -- 最大回撤
    win_rate DECIMAL(5,4),                         -- 胜率
    total_trades INTEGER DEFAULT 0,                -- 总交易数
    winning_trades INTEGER DEFAULT 0,              -- 盈利交易数
    losing_trades INTEGER DEFAULT 0,               -- 亏损交易数
    score DECIMAL(19,4),                           -- 得分
    created_at TIMESTAMPTZ DEFAULT NOW(),          -- 创建时间
    updated_at TIMESTAMPTZ DEFAULT NOW()           -- 更新时间
);

-- 游戏决策表 (根据GameDecision实体类)
CREATE TABLE IF NOT EXISTS game_decision (
    id BIGSERIAL PRIMARY KEY,                      -- 决策ID
    session_id VARCHAR(50) NOT NULL,               -- 会话ID
    frame_index INTEGER NOT NULL,                  -- 帧索引
    decision_type decision_type NOT NULL,          -- 决策类型
    price DECIMAL(19,4),                           -- 决策价格
    quantity INTEGER,                              -- 数量
    pnl DECIMAL(19,4),                             -- 盈亏
    cumulative_pnl DECIMAL(19,4),                  -- 累计盈亏
    decision_time TIMESTAMPTZ NOT NULL,            -- 决策时间
    response_time_ms BIGINT,                       -- 响应时间(毫秒)
    created_at TIMESTAMPTZ DEFAULT NOW(),          -- 创建时间
    
    FOREIGN KEY (session_id) REFERENCES game_session(id),
    UNIQUE(session_id, frame_index)                -- 每帧只能有一个决策
);

-- 游戏段表 (预定义的交易段)
CREATE TABLE IF NOT EXISTS game_segment (
    id VARCHAR(50) PRIMARY KEY,                    -- 段ID
    code VARCHAR(20) NOT NULL,                     -- 股票代码
    start_time TIMESTAMPTZ NOT NULL,               -- 开始时间
    end_time TIMESTAMPTZ NOT NULL,                 -- 结束时间
    volatility DECIMAL(8,6) NOT NULL,              -- 波动率
    avg_volume BIGINT NOT NULL,                    -- 平均成交量
    keypoint_count INTEGER NOT NULL,               -- 关键点数量
    keypoint_indices INTEGER[] NOT NULL,           -- 关键点索引数组
    difficulty_level INTEGER DEFAULT 1,            -- 难度等级
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 游戏奖励表
CREATE TABLE IF NOT EXISTS game_reward (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(50) NOT NULL,               -- 会话ID
    decision_id BIGINT NOT NULL,                   -- 关联决策ID
    window_size INTEGER NOT NULL,                  -- 评分窗口大小
    pnl DECIMAL(12,4) NOT NULL,                    -- 盈亏
    max_drawdown DECIMAL(8,6),                     -- 最大回撤
    volatility DECIMAL(8,6),                       -- 波动率
    fee DECIMAL(12,4) DEFAULT 0,                   -- 手续费
    total_score DECIMAL(12,4) NOT NULL,            -- 总分
    score_breakdown JSONB,                         -- 评分明细
    rule_version VARCHAR(20) NOT NULL,             -- 评分规则版本
    created_at TIMESTAMPTZ DEFAULT NOW(),
    
    FOREIGN KEY (session_id) REFERENCES game_session(id),
    FOREIGN KEY (decision_id) REFERENCES game_decision(id)
);

-- 每日排行榜表
CREATE TABLE IF NOT EXISTS leaderboard_daily (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,                            -- 日期
    user_id VARCHAR(50),                           -- 用户ID
    session_count INTEGER DEFAULT 0,               -- 会话数量
    total_score DECIMAL(12,4) DEFAULT 0,           -- 总分
    avg_score DECIMAL(12,4) DEFAULT 0,             -- 平均分
    best_score DECIMAL(12,4) DEFAULT 0,            -- 最佳分数
    total_pnl DECIMAL(12,4) DEFAULT 0,             -- 总盈亏
    win_rate DECIMAL(5,4) DEFAULT 0,               -- 胜率
    rank_position INTEGER,                         -- 排名
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    UNIQUE(date, user_id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_game_session_stock_code ON game_session(stock_code);
CREATE INDEX IF NOT EXISTS idx_game_session_status ON game_session(status);
CREATE INDEX IF NOT EXISTS idx_game_session_created_at ON game_session(created_at);

CREATE INDEX IF NOT EXISTS idx_game_decision_session_id ON game_decision(session_id);
CREATE INDEX IF NOT EXISTS idx_game_decision_frame_index ON game_decision(frame_index);
CREATE INDEX IF NOT EXISTS idx_game_decision_decision_time ON game_decision(decision_time);

CREATE INDEX IF NOT EXISTS idx_game_segment_code ON game_segment(code);
CREATE INDEX IF NOT EXISTS idx_game_segment_volatility ON game_segment(volatility);
CREATE INDEX IF NOT EXISTS idx_game_segment_created_at ON game_segment(created_at);

CREATE INDEX IF NOT EXISTS idx_game_reward_session_id ON game_reward(session_id);
CREATE INDEX IF NOT EXISTS idx_game_reward_decision_id ON game_reward(decision_id);

CREATE INDEX IF NOT EXISTS idx_leaderboard_daily_date ON leaderboard_daily(date);
CREATE INDEX IF NOT EXISTS idx_leaderboard_daily_user_id ON leaderboard_daily(user_id);
CREATE INDEX IF NOT EXISTS idx_leaderboard_daily_rank ON leaderboard_daily(rank_position);