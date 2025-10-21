-- 业务数据表结构
-- 创建时间: 2024-01-01
-- 用途: 存储游戏会话、决策、奖励等业务数据

-- 创建枚举类型
CREATE TYPE session_status AS ENUM ('CREATED', 'RUNNING', 'PAUSED', 'COMPLETED', 'CANCELLED');
CREATE TYPE decision_type AS ENUM ('BUY', 'SELL', 'SKIP');
CREATE TYPE frame_type AS ENUM ('KLINE', 'PAUSE', 'REWARD', 'END');

-- 游戏段表 (预定义的交易段)
CREATE TABLE IF NOT EXISTS game_segment (
    id VARCHAR(50) PRIMARY KEY,             -- 段ID
    code VARCHAR(20) NOT NULL,              -- 股票代码
    start_time TIMESTAMPTZ NOT NULL,        -- 开始时间
    end_time TIMESTAMPTZ NOT NULL,          -- 结束时间
    volatility DECIMAL(8,6) NOT NULL,       -- 波动率 (可配置精度)
    avg_volume BIGINT NOT NULL,             -- 平均成交量
    keypoint_count INTEGER NOT NULL,        -- 关键点数量
    keypoint_indices INTEGER[] NOT NULL,    -- 关键点索引数组
    difficulty_level INTEGER DEFAULT 1,     -- 难度等级 (可配置: 1-5)
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 游戏会话表
CREATE TABLE IF NOT EXISTS game_session (
    id VARCHAR(50) PRIMARY KEY,             -- 会话ID
    segment_id VARCHAR(50) NOT NULL,        -- 关联段ID
    user_id VARCHAR(50),                    -- 用户ID (可为空，支持匿名)
    status session_status DEFAULT 'CREATED',
    current_frame_index INTEGER DEFAULT 0,  -- 当前帧索引
    total_frames INTEGER NOT NULL,          -- 总帧数
    seed_value BIGINT NOT NULL,             -- 随机种子
    config JSONB,                           -- 会话配置 (可配置)
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    FOREIGN KEY (segment_id) REFERENCES game_segment(id)
);

-- 游戏决策表
CREATE TABLE IF NOT EXISTS game_decision (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(50) NOT NULL,        -- 会话ID
    frame_index INTEGER NOT NULL,           -- 帧索引
    decision_type decision_type NOT NULL,   -- 决策类型
    price DECIMAL(12,4),                    -- 决策价格
    quantity INTEGER DEFAULT 1,             -- 数量 (可配置)
    timestamp_ms BIGINT NOT NULL,           -- 决策时间戳(毫秒)
    response_time_ms INTEGER,               -- 响应时间(毫秒)
    is_timeout BOOLEAN DEFAULT FALSE,       -- 是否超时
    created_at TIMESTAMPTZ DEFAULT NOW(),
    
    FOREIGN KEY (session_id) REFERENCES game_session(id),
    UNIQUE(session_id, frame_index)         -- 每帧只能有一个决策
);

-- 游戏奖励表
CREATE TABLE IF NOT EXISTS game_reward (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(50) NOT NULL,        -- 会话ID
    decision_id BIGINT NOT NULL,            -- 关联决策ID
    window_size INTEGER NOT NULL,           -- 评分窗口大小 (可配置)
    pnl DECIMAL(12,4) NOT NULL,             -- 盈亏
    max_drawdown DECIMAL(8,6),              -- 最大回撤
    volatility DECIMAL(8,6),                -- 波动率
    fee DECIMAL(12,4) DEFAULT 0,            -- 手续费
    total_score DECIMAL(12,4) NOT NULL,     -- 总分
    score_breakdown JSONB,                  -- 评分明细 (可配置)
    rule_version VARCHAR(20) NOT NULL,      -- 评分规则版本
    created_at TIMESTAMPTZ DEFAULT NOW(),
    
    FOREIGN KEY (session_id) REFERENCES game_session(id),
    FOREIGN KEY (decision_id) REFERENCES game_decision(id)
);

-- 每日排行榜表
CREATE TABLE IF NOT EXISTS leaderboard_daily (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,                     -- 日期
    user_id VARCHAR(50),                    -- 用户ID
    session_count INTEGER DEFAULT 0,        -- 会话数量
    total_score DECIMAL(12,4) DEFAULT 0,    -- 总分
    avg_score DECIMAL(12,4) DEFAULT 0,      -- 平均分
    best_score DECIMAL(12,4) DEFAULT 0,     -- 最佳分数
    total_pnl DECIMAL(12,4) DEFAULT 0,      -- 总盈亏
    win_rate DECIMAL(5,4) DEFAULT 0,        -- 胜率 (可配置精度)
    rank_position INTEGER,                  -- 排名
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    UNIQUE(date, user_id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_game_segment_code ON game_segment(code);
CREATE INDEX IF NOT EXISTS idx_game_segment_volatility ON game_segment(volatility);
CREATE INDEX IF NOT EXISTS idx_game_segment_created_at ON game_segment(created_at);

CREATE INDEX IF NOT EXISTS idx_game_session_segment_id ON game_session(segment_id);
CREATE INDEX IF NOT EXISTS idx_game_session_user_id ON game_session(user_id);
CREATE INDEX IF NOT EXISTS idx_game_session_status ON game_session(status);
CREATE INDEX IF NOT EXISTS idx_game_session_created_at ON game_session(created_at);

CREATE INDEX IF NOT EXISTS idx_game_decision_session_id ON game_decision(session_id);
CREATE INDEX IF NOT EXISTS idx_game_decision_frame_index ON game_decision(frame_index);
CREATE INDEX IF NOT EXISTS idx_game_decision_created_at ON game_decision(created_at);

CREATE INDEX IF NOT EXISTS idx_game_reward_session_id ON game_reward(session_id);
CREATE INDEX IF NOT EXISTS idx_game_reward_decision_id ON game_reward(decision_id);
CREATE INDEX IF NOT EXISTS idx_game_reward_rule_version ON game_reward(rule_version);

CREATE INDEX IF NOT EXISTS idx_leaderboard_daily_date ON leaderboard_daily(date);
CREATE INDEX IF NOT EXISTS idx_leaderboard_daily_user_id ON leaderboard_daily(user_id);
CREATE INDEX IF NOT EXISTS idx_leaderboard_daily_rank ON leaderboard_daily(rank_position);

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为相关表添加更新时间触发器
CREATE TRIGGER update_game_segment_updated_at 
    BEFORE UPDATE ON game_segment 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_game_session_updated_at 
    BEFORE UPDATE ON game_session 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_leaderboard_daily_updated_at 
    BEFORE UPDATE ON leaderboard_daily 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 创建会话统计视图
CREATE OR REPLACE VIEW session_stats AS
SELECT 
    gs.id,
    gs.segment_id,
    gs.user_id,
    gs.status,
    gs.current_frame_index,
    gs.total_frames,
    COALESCE(decision_count, 0) AS decision_count,
    COALESCE(avg_response_time, 0) AS avg_response_time_ms,
    COALESCE(timeout_count, 0) AS timeout_count,
    COALESCE(total_score, 0) AS total_score,
    COALESCE(total_pnl, 0) AS total_pnl,
    gs.created_at,
    gs.updated_at
FROM game_session gs
LEFT JOIN (
    SELECT 
        session_id,
        COUNT(*) AS decision_count,
        AVG(response_time_ms) AS avg_response_time,
        SUM(CASE WHEN is_timeout THEN 1 ELSE 0 END) AS timeout_count
    FROM game_decision
    GROUP BY session_id
) d ON gs.id = d.session_id
LEFT JOIN (
    SELECT 
        session_id,
        SUM(total_score) AS total_score,
        SUM(pnl) AS total_pnl
    FROM game_reward
    GROUP BY session_id
) r ON gs.id = r.session_id;

-- 创建排行榜更新函数
CREATE OR REPLACE FUNCTION update_daily_leaderboard(target_date DATE DEFAULT CURRENT_DATE)
RETURNS VOID AS $$
BEGIN
    -- 删除当日数据
    DELETE FROM leaderboard_daily WHERE date = target_date;
    
    -- 重新计算当日排行榜
    WITH daily_stats AS (
        SELECT 
            target_date as date,
            gs.user_id,
            COUNT(gs.id) as session_count,
            COALESCE(SUM(r.total_score), 0) as total_score,
            COALESCE(AVG(r.total_score), 0) as avg_score,
            COALESCE(MAX(r.total_score), 0) as best_score,
            COALESCE(SUM(r.pnl), 0) as total_pnl,
            CASE 
                WHEN COUNT(CASE WHEN r.pnl > 0 THEN 1 END) > 0 
                THEN COUNT(CASE WHEN r.pnl > 0 THEN 1 END)::DECIMAL / COUNT(r.pnl)::DECIMAL
                ELSE 0 
            END as win_rate
        FROM game_session gs
        LEFT JOIN game_reward r ON gs.id = r.session_id
        WHERE DATE(gs.created_at) = target_date
          AND gs.user_id IS NOT NULL
        GROUP BY gs.user_id
    ),
    ranked_stats AS (
        SELECT *,
               ROW_NUMBER() OVER (ORDER BY total_score DESC, avg_score DESC) as rank_position
        FROM daily_stats
    )
    INSERT INTO leaderboard_daily (
        date, user_id, session_count, total_score, avg_score, 
        best_score, total_pnl, win_rate, rank_position
    )
    SELECT 
        date, user_id, session_count, total_score, avg_score,
        best_score, total_pnl, win_rate, rank_position
    FROM ranked_stats;
END;
$$ LANGUAGE plpgsql;

-- 添加表注释
COMMENT ON TABLE game_segment IS '游戏段配置表，存储预定义的交易段';
COMMENT ON TABLE game_session IS '游戏会话表，记录每次游戏的状态';
COMMENT ON TABLE game_decision IS '游戏决策表，记录用户的交易决策';
COMMENT ON TABLE game_reward IS '游戏奖励表，记录评分结果';
COMMENT ON TABLE leaderboard_daily IS '每日排行榜表';
COMMENT ON VIEW session_stats IS '会话统计视图，聚合会话相关数据';
COMMENT ON FUNCTION update_daily_leaderboard IS '更新每日排行榜数据';