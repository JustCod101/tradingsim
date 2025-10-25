-- 用户管理表结构
-- 创建时间: 2024-01-01
-- 用途: 存储用户信息、偏好设置、统计数据等

-- 创建枚举类型
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED');
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN', 'MODERATOR');

-- 用户基本信息表
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,                    -- 用户ID
    username VARCHAR(50) UNIQUE NOT NULL,          -- 用户名
    email VARCHAR(100) UNIQUE NOT NULL,            -- 邮箱
    password_hash VARCHAR(255) NOT NULL,           -- 密码哈希
    full_name VARCHAR(100),                        -- 全名
    avatar_url VARCHAR(500),                       -- 头像URL
    status user_status DEFAULT 'ACTIVE',           -- 用户状态
    roles VARCHAR(100) DEFAULT 'USER',             -- 用户角色
    email_verified BOOLEAN DEFAULT FALSE,          -- 邮箱验证状态
    phone VARCHAR(20),                             -- 手机号
    birth_date DATE,                               -- 生日
    timezone VARCHAR(50) DEFAULT 'UTC',            -- 时区
    language VARCHAR(10) DEFAULT 'zh-CN',          -- 语言偏好
    last_login_at TIMESTAMPTZ,                     -- 最后登录时间
    last_login_ip VARCHAR(45),                     -- 最后登录IP
    created_at TIMESTAMPTZ DEFAULT NOW(),          -- 创建时间
    updated_at TIMESTAMPTZ DEFAULT NOW()           -- 更新时间
);

-- 用户偏好设置表
CREATE TABLE IF NOT EXISTS user_preferences (
    user_id VARCHAR(50) PRIMARY KEY,               -- 用户ID
    preferred_difficulty VARCHAR(20) DEFAULT 'normal', -- 偏好难度
    preferred_timeframe VARCHAR(10) DEFAULT '1m',  -- 偏好时间框架
    preferred_stocks TEXT[],                       -- 偏好股票列表
    notifications_enabled BOOLEAN DEFAULT TRUE,    -- 通知开关
    sound_enabled BOOLEAN DEFAULT TRUE,            -- 声音开关
    theme VARCHAR(20) DEFAULT 'light',             -- 主题设置
    auto_save_enabled BOOLEAN DEFAULT TRUE,        -- 自动保存开关
    show_tutorials BOOLEAN DEFAULT TRUE,           -- 显示教程
    risk_tolerance VARCHAR(20) DEFAULT 'medium',   -- 风险承受度
    trading_style VARCHAR(20) DEFAULT 'balanced',  -- 交易风格
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 用户统计信息表
CREATE TABLE IF NOT EXISTS user_stats (
    user_id VARCHAR(50) PRIMARY KEY,               -- 用户ID
    total_sessions INTEGER DEFAULT 0,              -- 总会话数
    completed_sessions INTEGER DEFAULT 0,          -- 完成会话数
    total_score DECIMAL(12,4) DEFAULT 0,           -- 总分数
    best_score DECIMAL(12,4) DEFAULT 0,            -- 最佳分数
    average_score DECIMAL(12,4) DEFAULT 0,         -- 平均分数
    total_pnl DECIMAL(12,4) DEFAULT 0,             -- 总盈亏
    best_pnl DECIMAL(12,4) DEFAULT 0,              -- 最佳盈亏
    worst_pnl DECIMAL(12,4) DEFAULT 0,             -- 最差盈亏
    win_rate DECIMAL(5,4) DEFAULT 0,               -- 胜率
    total_trades INTEGER DEFAULT 0,                -- 总交易数
    winning_trades INTEGER DEFAULT 0,              -- 盈利交易数
    losing_trades INTEGER DEFAULT 0,               -- 亏损交易数
    avg_response_time_ms BIGINT DEFAULT 0,         -- 平均响应时间
    max_consecutive_wins INTEGER DEFAULT 0,        -- 最大连胜
    max_consecutive_losses INTEGER DEFAULT 0,      -- 最大连败
    total_play_time_minutes BIGINT DEFAULT 0,      -- 总游戏时间(分钟)
    rank_position INTEGER,                         -- 当前排名
    rank_percentile DECIMAL(5,4),                  -- 排名百分位
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 用户会话表 (登录会话管理)
CREATE TABLE IF NOT EXISTS user_sessions (
    id VARCHAR(50) PRIMARY KEY,                    -- 会话ID
    user_id VARCHAR(50) NOT NULL,                  -- 用户ID
    access_token_hash VARCHAR(255) NOT NULL,       -- 访问令牌哈希
    refresh_token_hash VARCHAR(255) NOT NULL,      -- 刷新令牌哈希
    device_info VARCHAR(500),                      -- 设备信息
    ip_address VARCHAR(45),                        -- IP地址
    user_agent VARCHAR(1000),                      -- 用户代理
    is_remember_me BOOLEAN DEFAULT FALSE,          -- 记住我
    expires_at TIMESTAMPTZ NOT NULL,               -- 过期时间
    last_activity_at TIMESTAMPTZ DEFAULT NOW(),    -- 最后活动时间
    created_at TIMESTAMPTZ DEFAULT NOW(),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 用户成就表
CREATE TABLE IF NOT EXISTS user_achievements (
    id BIGSERIAL PRIMARY KEY,                      -- 成就ID
    user_id VARCHAR(50) NOT NULL,                  -- 用户ID
    achievement_type VARCHAR(50) NOT NULL,         -- 成就类型
    achievement_name VARCHAR(100) NOT NULL,        -- 成就名称
    description TEXT,                              -- 成就描述
    icon_url VARCHAR(500),                         -- 图标URL
    points INTEGER DEFAULT 0,                      -- 成就积分
    unlocked_at TIMESTAMPTZ DEFAULT NOW(),         -- 解锁时间
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(user_id, achievement_type)
);

-- 用户关注股票表
CREATE TABLE IF NOT EXISTS user_watchlist (
    id BIGSERIAL PRIMARY KEY,                      -- 记录ID
    user_id VARCHAR(50) NOT NULL,                  -- 用户ID
    stock_code VARCHAR(20) NOT NULL,               -- 股票代码
    added_at TIMESTAMPTZ DEFAULT NOW(),            -- 添加时间
    notes TEXT,                                    -- 备注
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(user_id, stock_code)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_users_last_login_at ON users(last_login_at);

CREATE INDEX IF NOT EXISTS idx_user_preferences_user_id ON user_preferences(user_id);

CREATE INDEX IF NOT EXISTS idx_user_stats_user_id ON user_stats(user_id);
CREATE INDEX IF NOT EXISTS idx_user_stats_total_score ON user_stats(total_score DESC);
CREATE INDEX IF NOT EXISTS idx_user_stats_rank_position ON user_stats(rank_position);

CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_expires_at ON user_sessions(expires_at);
CREATE INDEX IF NOT EXISTS idx_user_sessions_last_activity ON user_sessions(last_activity_at);

CREATE INDEX IF NOT EXISTS idx_user_achievements_user_id ON user_achievements(user_id);
CREATE INDEX IF NOT EXISTS idx_user_achievements_type ON user_achievements(achievement_type);

CREATE INDEX IF NOT EXISTS idx_user_watchlist_user_id ON user_watchlist(user_id);
CREATE INDEX IF NOT EXISTS idx_user_watchlist_stock_code ON user_watchlist(stock_code);

-- 创建触发器函数：自动更新 updated_at 字段
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为相关表创建触发器
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_preferences_updated_at 
    BEFORE UPDATE ON user_preferences 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_stats_updated_at 
    BEFORE UPDATE ON user_stats 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 创建用户统计更新函数
CREATE OR REPLACE FUNCTION update_user_stats_from_session(p_user_id VARCHAR(50))
RETURNS VOID AS $$
DECLARE
    session_count INTEGER;
    completed_count INTEGER;
    total_score_val DECIMAL(12,4);
    best_score_val DECIMAL(12,4);
    avg_score_val DECIMAL(12,4);
    total_pnl_val DECIMAL(12,4);
    best_pnl_val DECIMAL(12,4);
    worst_pnl_val DECIMAL(12,4);
    win_count INTEGER;
    total_count INTEGER;
    win_rate_val DECIMAL(5,4);
BEGIN
    -- 计算会话统计
    SELECT COUNT(*), COUNT(*) FILTER (WHERE status = 'COMPLETED')
    INTO session_count, completed_count
    FROM game_session 
    WHERE user_id = p_user_id;
    
    -- 计算分数统计 (从奖励表)
    SELECT 
        COALESCE(SUM(total_score), 0),
        COALESCE(MAX(total_score), 0),
        COALESCE(AVG(total_score), 0)
    INTO total_score_val, best_score_val, avg_score_val
    FROM game_reward gr
    JOIN game_session gs ON gr.session_id = gs.id
    WHERE gs.user_id = p_user_id;
    
    -- 计算盈亏统计
    SELECT 
        COALESCE(SUM(total_pnl), 0),
        COALESCE(MAX(total_pnl), 0),
        COALESCE(MIN(total_pnl), 0)
    INTO total_pnl_val, best_pnl_val, worst_pnl_val
    FROM game_session 
    WHERE user_id = p_user_id AND status = 'COMPLETED';
    
    -- 计算胜率
    SELECT 
        COUNT(*) FILTER (WHERE total_pnl > 0),
        COUNT(*)
    INTO win_count, total_count
    FROM game_session 
    WHERE user_id = p_user_id AND status = 'COMPLETED';
    
    IF total_count > 0 THEN
        win_rate_val := win_count::DECIMAL / total_count::DECIMAL;
    ELSE
        win_rate_val := 0;
    END IF;
    
    -- 更新用户统计
    INSERT INTO user_stats (
        user_id, total_sessions, completed_sessions, 
        total_score, best_score, average_score,
        total_pnl, best_pnl, worst_pnl, win_rate,
        updated_at
    ) VALUES (
        p_user_id, session_count, completed_count,
        total_score_val, best_score_val, avg_score_val,
        total_pnl_val, best_pnl_val, worst_pnl_val, win_rate_val,
        NOW()
    )
    ON CONFLICT (user_id) DO UPDATE SET
        total_sessions = EXCLUDED.total_sessions,
        completed_sessions = EXCLUDED.completed_sessions,
        total_score = EXCLUDED.total_score,
        best_score = EXCLUDED.best_score,
        average_score = EXCLUDED.average_score,
        total_pnl = EXCLUDED.total_pnl,
        best_pnl = EXCLUDED.best_pnl,
        worst_pnl = EXCLUDED.worst_pnl,
        win_rate = EXCLUDED.win_rate,
        updated_at = NOW();
END;
$$ LANGUAGE plpgsql;

-- 创建用户排名更新函数
CREATE OR REPLACE FUNCTION update_user_rankings()
RETURNS VOID AS $$
BEGIN
    WITH ranked_users AS (
        SELECT 
            user_id,
            ROW_NUMBER() OVER (ORDER BY total_score DESC, best_score DESC, completed_sessions DESC) as rank_pos,
            PERCENT_RANK() OVER (ORDER BY total_score DESC, best_score DESC, completed_sessions DESC) as percentile
        FROM user_stats
        WHERE total_sessions > 0
    )
    UPDATE user_stats 
    SET 
        rank_position = ranked_users.rank_pos,
        rank_percentile = ranked_users.percentile,
        updated_at = NOW()
    FROM ranked_users
    WHERE user_stats.user_id = ranked_users.user_id;
END;
$$ LANGUAGE plpgsql;

COMMENT ON TABLE users IS '用户基本信息表';
COMMENT ON TABLE user_preferences IS '用户偏好设置表';
COMMENT ON TABLE user_stats IS '用户统计信息表';
COMMENT ON TABLE user_sessions IS '用户登录会话表';
COMMENT ON TABLE user_achievements IS '用户成就表';
COMMENT ON TABLE user_watchlist IS '用户关注股票表';
COMMENT ON FUNCTION update_user_stats_from_session IS '根据游戏会话更新用户统计信息';
COMMENT ON FUNCTION update_user_rankings IS '更新所有用户排名信息';