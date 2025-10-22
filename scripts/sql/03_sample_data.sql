-- 示例数据生成脚本
-- 创建时间: 2024-01-01
-- 用途: 生成演示用的市场数据和游戏段

-- 设置随机种子以确保可重现的数据 (可配置)
SELECT setseed(0.12345);

-- 生成 AAPL 示例数据 (可配置股票代码)
DO $$
DECLARE
    base_price DECIMAL(12,4) := 150.00;    -- 可配置: 基础价格
    volatility DECIMAL := 0.02;            -- 可配置: 波动率
    curr_time TIMESTAMPTZ := '2024-01-01 09:30:00+00'::TIMESTAMPTZ;
    end_time TIMESTAMPTZ := '2024-01-01 16:00:00+00'::TIMESTAMPTZ;
    current_price DECIMAL(12,4) := base_price;
    open_price DECIMAL(12,4);
    high_price DECIMAL(12,4);
    low_price DECIMAL(12,4);
    close_price DECIMAL(12,4);
    volume_val BIGINT;
    price_change DECIMAL(12,4);
BEGIN
    WHILE curr_time < end_time LOOP
        open_price := current_price;
        
        -- 生成随机价格变动
        price_change := (random() - 0.5) * volatility * base_price;
        close_price := open_price + price_change;
        
        -- 确保价格为正数
        IF close_price <= 0 THEN
            close_price := open_price * 0.99;
        END IF;
        
        -- 生成高低价
        high_price := GREATEST(open_price, close_price) + random() * 0.5;
        low_price := LEAST(open_price, close_price) - random() * 0.5;
        
        -- 确保高低价合理
        IF low_price <= 0 THEN
            low_price := LEAST(open_price, close_price) * 0.995;
        END IF;
        
        -- 生成成交量 (可配置范围)
        volume_val := (1000 + random() * 9000)::BIGINT;
        
        -- 插入数据
        INSERT INTO ohlcv_1m (code, ts, open, high, low, close, volume)
        VALUES ('AAPL', curr_time, open_price, high_price, low_price, close_price, volume_val);
        
        current_price := close_price;
        curr_time := curr_time + INTERVAL '1 minute';
    END LOOP;
END $$;

-- 生成 TSLA 示例数据 (可配置股票代码)
DO $$
DECLARE
    base_price DECIMAL(12,4) := 200.00;    -- 可配置: 基础价格
    volatility DECIMAL := 0.03;            -- 可配置: 波动率 (更高)
    curr_time TIMESTAMPTZ := '2024-01-01 09:30:00+00'::TIMESTAMPTZ;
    end_time TIMESTAMPTZ := '2024-01-01 16:00:00+00'::TIMESTAMPTZ;
    current_price DECIMAL(12,4) := base_price;
    open_price DECIMAL(12,4);
    high_price DECIMAL(12,4);
    low_price DECIMAL(12,4);
    close_price DECIMAL(12,4);
    volume_val BIGINT;
    price_change DECIMAL(12,4);
BEGIN
    WHILE curr_time < end_time LOOP
        open_price := current_price;
        
        price_change := (random() - 0.5) * volatility * base_price;
        close_price := open_price + price_change;
        
        IF close_price <= 0 THEN
            close_price := open_price * 0.99;
        END IF;
        
        high_price := GREATEST(open_price, close_price) + random() * 0.8;
        low_price := LEAST(open_price, close_price) - random() * 0.8;
        
        IF low_price <= 0 THEN
            low_price := LEAST(open_price, close_price) * 0.995;
        END IF;
        
        volume_val := (2000 + random() * 18000)::BIGINT;  -- 可配置: 更高成交量
        
        INSERT INTO ohlcv_1m (code, ts, open, high, low, close, volume)
        VALUES ('TSLA', curr_time, open_price, high_price, low_price, close_price, volume_val);
        
        current_price := close_price;
        curr_time := curr_time + INTERVAL '1 minute';
    END LOOP;
END $$;

-- 生成 MSFT 示例数据 (可配置股票代码)
DO $$
DECLARE
    base_price DECIMAL(12,4) := 300.00;    -- 可配置: 基础价格
    volatility DECIMAL := 0.015;           -- 可配置: 波动率 (较低)
    curr_time TIMESTAMPTZ := '2024-01-01 09:30:00+00'::TIMESTAMPTZ;
    end_time TIMESTAMPTZ := '2024-01-01 16:00:00+00'::TIMESTAMPTZ;
    current_price DECIMAL(12,4) := base_price;
    open_price DECIMAL(12,4);
    high_price DECIMAL(12,4);
    low_price DECIMAL(12,4);
    close_price DECIMAL(12,4);
    volume_val BIGINT;
    price_change DECIMAL(12,4);
BEGIN
    WHILE curr_time < end_time LOOP
        open_price := current_price;
        
        price_change := (random() - 0.5) * volatility * base_price;
        close_price := open_price + price_change;
        
        IF close_price <= 0 THEN
            close_price := open_price * 0.99;
        END IF;
        
        high_price := GREATEST(open_price, close_price) + random() * 0.3;
        low_price := LEAST(open_price, close_price) - random() * 0.3;
        
        IF low_price <= 0 THEN
            low_price := LEAST(open_price, close_price) * 0.995;
        END IF;
        
        volume_val := (800 + random() * 7200)::BIGINT;   -- 可配置: 中等成交量
        
        INSERT INTO ohlcv_1m (code, ts, open, high, low, close, volume)
        VALUES ('MSFT', curr_time, open_price, high_price, low_price, close_price, volume_val);
        
        current_price := close_price;
        curr_time := curr_time + INTERVAL '1 minute';
    END LOOP;
END $$;

-- 创建游戏段数据
INSERT INTO game_segment (id, code, start_time, end_time, volatility, avg_volume, keypoint_count, keypoint_indices, difficulty_level)
VALUES 
    -- AAPL 段 (可配置参数)
    ('demo-001', 'AAPL', 
     '2024-01-01 10:00:00+00', '2024-01-01 11:00:00+00',
     0.025, 5000, 4, ARRAY[15, 30, 45, 55], 2),
    
    ('demo-002', 'AAPL', 
     '2024-01-01 13:00:00+00', '2024-01-01 14:30:00+00',
     0.032, 5200, 6, ARRAY[20, 35, 50, 65, 75, 85], 3),
    
    -- TSLA 段 (可配置参数)
    ('demo-003', 'TSLA', 
     '2024-01-01 10:30:00+00', '2024-01-01 12:00:00+00',
     0.045, 8500, 5, ARRAY[18, 32, 48, 62, 78], 4),
    
    ('demo-004', 'TSLA', 
     '2024-01-01 14:00:00+00', '2024-01-01 15:00:00+00',
     0.038, 9200, 3, ARRAY[20, 40, 55], 2),
    
    -- MSFT 段 (可配置参数)
    ('demo-005', 'MSFT', 
     '2024-01-01 11:00:00+00', '2024-01-01 12:30:00+00',
     0.018, 4000, 4, ARRAY[22, 38, 52, 68], 1),
    
    ('demo-006', 'MSFT', 
     '2024-01-01 14:30:00+00', '2024-01-01 15:30:00+00',
     0.022, 4300, 5, ARRAY[15, 28, 42, 56, 72], 3);

-- 创建示例用户会话数据
INSERT INTO game_session (id, segment_id, user_id, status, current_frame_index, total_frames, seed_value, config)
VALUES 
    ('session-001', 'demo-001', 'user-001', 'COMPLETED', 60, 60, 12345, 
     '{"frameIntervalMs": 250, "decisionTimeoutSec": 10}'::JSONB),
    
    ('session-002', 'demo-002', 'user-001', 'COMPLETED', 90, 90, 12346,
     '{"frameIntervalMs": 250, "decisionTimeoutSec": 10}'::JSONB),
    
    ('session-003', 'demo-003', 'user-002', 'RUNNING', 45, 90, 12347,
     '{"frameIntervalMs": 250, "decisionTimeoutSec": 10}'::JSONB);

-- 创建示例决策数据
INSERT INTO game_decision (session_id, frame_index, decision_type, price, quantity, timestamp_ms, response_time_ms, is_timeout)
VALUES 
    ('session-001', 15, 'BUY', 150.25, 1, 1704110400000, 2500, FALSE),
    ('session-001', 30, 'SELL', 151.80, 1, 1704110415000, 1800, FALSE),
    ('session-001', 45, 'SKIP', NULL, 0, 1704110430000, 800, FALSE),
    ('session-001', 55, 'BUY', 149.90, 1, 1704110445000, 3200, FALSE),
    
    ('session-002', 20, 'BUY', 150.10, 1, 1704121200000, 2100, FALSE),
    ('session-002', 35, 'SKIP', NULL, 0, 1704121215000, 1200, FALSE),
    ('session-002', 50, 'SELL', 152.30, 1, 1704121230000, 2800, FALSE),
    ('session-002', 65, 'BUY', 151.50, 1, 1704121245000, 10500, TRUE),  -- 超时
    ('session-002', 75, 'SELL', 153.20, 1, 1704121260000, 1900, FALSE),
    ('session-002', 85, 'SKIP', NULL, 0, 1704121275000, 900, FALSE);

-- 创建示例奖励数据
INSERT INTO game_reward (session_id, decision_id, window_size, pnl, max_drawdown, volatility, fee, total_score, score_breakdown, rule_version)
VALUES 
    ('session-001', 1, 5, 1.55, 0.02, 0.025, 0.15, 85.2, 
     '{"pnl_score": 77.5, "risk_penalty": -7.8, "fee_penalty": -0.75}'::JSONB, 'v1.0'),
    
    ('session-001', 2, 10, -0.90, 0.035, 0.028, 0.15, -12.5,
     '{"pnl_score": -45.0, "risk_penalty": -17.5, "fee_penalty": -0.75}'::JSONB, 'v1.0'),
    
    ('session-001', 4, 5, 2.30, 0.015, 0.022, 0.15, 112.8,
     '{"pnl_score": 115.0, "risk_penalty": -7.5, "fee_penalty": -0.75}'::JSONB, 'v1.0'),
    
    ('session-002', 5, 5, 2.20, 0.018, 0.024, 0.15, 108.5,
     '{"pnl_score": 110.0, "risk_penalty": -9.0, "fee_penalty": -0.75}'::JSONB, 'v1.0'),
    
    ('session-002', 7, 10, -1.20, 0.042, 0.032, 0.15, -25.8,
     '{"pnl_score": -60.0, "risk_penalty": -21.0, "fee_penalty": -0.75}'::JSONB, 'v1.0'),
    
    ('session-002', 9, 5, 1.70, 0.025, 0.026, 0.15, 78.3,
     '{"pnl_score": 85.0, "risk_penalty": -12.5, "fee_penalty": -0.75}'::JSONB, 'v1.0');

-- 更新会话完成时间
UPDATE game_session 
SET completed_at = created_at + INTERVAL '15 minutes'  -- 可配置: 模拟游戏时长
WHERE status = 'COMPLETED';

-- 生成每日排行榜数据
SELECT update_daily_leaderboard('2024-01-01'::DATE);

-- 创建数据验证查询
DO $$
DECLARE
    ohlcv_count INTEGER;
    segment_count INTEGER;
    session_count INTEGER;
    decision_count INTEGER;
    reward_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO ohlcv_count FROM ohlcv_1m;
    SELECT COUNT(*) INTO segment_count FROM game_segment;
    SELECT COUNT(*) INTO session_count FROM game_session;
    SELECT COUNT(*) INTO decision_count FROM game_decision;
    SELECT COUNT(*) INTO reward_count FROM game_reward;
    
    RAISE NOTICE '数据生成完成:';
    RAISE NOTICE '  OHLCV 记录数: %', ohlcv_count;
    RAISE NOTICE '  游戏段数: %', segment_count;
    RAISE NOTICE '  会话数: %', session_count;
    RAISE NOTICE '  决策数: %', decision_count;
    RAISE NOTICE '  奖励数: %', reward_count;
    
    -- 验证数据完整性
    IF ohlcv_count = 0 THEN
        RAISE EXCEPTION '错误: 未生成 OHLCV 数据';
    END IF;
    
    IF segment_count = 0 THEN
        RAISE EXCEPTION '错误: 未生成游戏段数据';
    END IF;
    
    RAISE NOTICE '数据验证通过!';
END $$;

-- 创建快速查询示例
-- 查看股票代码和数据量
SELECT 
    code,
    COUNT(*) as record_count,
    MIN(ts) as start_time,
    MAX(ts) as end_time,
    AVG(volume) as avg_volume
FROM ohlcv_1m 
GROUP BY code 
ORDER BY code;

-- 查看游戏段统计
SELECT 
    id,
    code,
    difficulty_level,
    keypoint_count,
    volatility,
    EXTRACT(EPOCH FROM (end_time - start_time))/60 as duration_minutes
FROM game_segment 
ORDER BY difficulty_level, code;

-- 查看会话统计
SELECT * FROM session_stats ORDER BY created_at;

-- 查看排行榜
SELECT 
    rank_position,
    user_id,
    total_score,
    session_count,
    win_rate
FROM leaderboard_daily 
WHERE date = '2024-01-01'
ORDER BY rank_position;

COMMENT ON SCRIPT IS '示例数据生成脚本 - 包含3只股票的1分钟OHLCV数据、6个游戏段、示例会话和决策数据';