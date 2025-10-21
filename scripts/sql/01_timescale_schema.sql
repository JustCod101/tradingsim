-- TimescaleDB 时序数据表结构
-- 创建时间: 2024-01-01
-- 用途: 存储OHLCV市场数据

-- 启用 TimescaleDB 扩展
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

-- 创建 OHLCV 1分钟数据表
CREATE TABLE IF NOT EXISTS ohlcv_1m (
    code VARCHAR(20) NOT NULL,              -- 股票代码 (可配置长度)
    ts TIMESTAMPTZ NOT NULL,                -- 时间戳
    open DECIMAL(12,4) NOT NULL,            -- 开盘价 (可配置精度)
    high DECIMAL(12,4) NOT NULL,            -- 最高价
    low DECIMAL(12,4) NOT NULL,             -- 最低价
    close DECIMAL(12,4) NOT NULL,           -- 收盘价
    volume BIGINT NOT NULL DEFAULT 0,       -- 成交量
    created_at TIMESTAMPTZ DEFAULT NOW()    -- 创建时间
);

-- 创建 hypertable (时序表)
SELECT create_hypertable('ohlcv_1m', 'ts', 
    chunk_time_interval => INTERVAL '1 day',  -- 可配置: 分片时间间隔
    if_not_exists => TRUE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_ohlcv_1m_code_ts ON ohlcv_1m (code, ts DESC);
CREATE INDEX IF NOT EXISTS idx_ohlcv_1m_ts ON ohlcv_1m (ts DESC);
CREATE INDEX IF NOT EXISTS idx_ohlcv_1m_code ON ohlcv_1m (code);

-- 创建数据保留策略 (可配置)
SELECT add_retention_policy('ohlcv_1m', INTERVAL '1 year', if_not_exists => TRUE);

-- 创建压缩策略 (可配置)
ALTER TABLE ohlcv_1m SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'code',
    timescaledb.compress_orderby = 'ts DESC'
);

SELECT add_compression_policy('ohlcv_1m', INTERVAL '7 days', if_not_exists => TRUE);

-- 创建连续聚合视图 - 5分钟K线 (可配置)
CREATE MATERIALIZED VIEW IF NOT EXISTS ohlcv_5m
WITH (timescaledb.continuous) AS
SELECT 
    code,
    time_bucket('5 minutes', ts) AS ts,
    first(open, ts) AS open,
    max(high) AS high,
    min(low) AS low,
    last(close, ts) AS close,
    sum(volume) AS volume
FROM ohlcv_1m
GROUP BY code, time_bucket('5 minutes', ts);

-- 创建连续聚合视图 - 1小时K线 (可配置)
CREATE MATERIALIZED VIEW IF NOT EXISTS ohlcv_1h
WITH (timescaledb.continuous) AS
SELECT 
    code,
    time_bucket('1 hour', ts) AS ts,
    first(open, ts) AS open,
    max(high) AS high,
    min(low) AS low,
    last(close, ts) AS close,
    sum(volume) AS volume
FROM ohlcv_1m
GROUP BY code, time_bucket('1 hour', ts);

-- 添加刷新策略
SELECT add_continuous_aggregate_policy('ohlcv_5m',
    start_offset => INTERVAL '1 hour',      -- 可配置
    end_offset => INTERVAL '1 minute',      -- 可配置
    schedule_interval => INTERVAL '1 minute', -- 可配置
    if_not_exists => TRUE
);

SELECT add_continuous_aggregate_policy('ohlcv_1h',
    start_offset => INTERVAL '4 hours',     -- 可配置
    end_offset => INTERVAL '5 minutes',     -- 可配置
    schedule_interval => INTERVAL '5 minutes', -- 可配置
    if_not_exists => TRUE
);

-- 创建查询优化函数
CREATE OR REPLACE FUNCTION get_ohlcv_range(
    p_code VARCHAR(20),
    p_start_time TIMESTAMPTZ,
    p_end_time TIMESTAMPTZ,
    p_timeframe VARCHAR(10) DEFAULT '1m'    -- 可配置: 1m, 5m, 1h
)
RETURNS TABLE(
    ts TIMESTAMPTZ,
    open DECIMAL(12,4),
    high DECIMAL(12,4),
    low DECIMAL(12,4),
    close DECIMAL(12,4),
    volume BIGINT
) AS $$
BEGIN
    CASE p_timeframe
        WHEN '1m' THEN
            RETURN QUERY
            SELECT o.ts, o.open, o.high, o.low, o.close, o.volume
            FROM ohlcv_1m o
            WHERE o.code = p_code 
              AND o.ts >= p_start_time 
              AND o.ts <= p_end_time
            ORDER BY o.ts;
        WHEN '5m' THEN
            RETURN QUERY
            SELECT o.ts, o.open, o.high, o.low, o.close, o.volume
            FROM ohlcv_5m o
            WHERE o.code = p_code 
              AND o.ts >= p_start_time 
              AND o.ts <= p_end_time
            ORDER BY o.ts;
        WHEN '1h' THEN
            RETURN QUERY
            SELECT o.ts, o.open, o.high, o.low, o.close, o.volume
            FROM ohlcv_1h o
            WHERE o.code = p_code 
              AND o.ts >= p_start_time 
              AND o.ts <= p_end_time
            ORDER BY o.ts;
        ELSE
            RAISE EXCEPTION 'Unsupported timeframe: %', p_timeframe;
    END CASE;
END;
$$ LANGUAGE plpgsql;

-- 创建统计信息更新函数
CREATE OR REPLACE FUNCTION update_table_stats()
RETURNS VOID AS $$
BEGIN
    ANALYZE ohlcv_1m;
    ANALYZE ohlcv_5m;
    ANALYZE ohlcv_1h;
END;
$$ LANGUAGE plpgsql;

COMMENT ON TABLE ohlcv_1m IS '1分钟OHLCV时序数据表';
COMMENT ON FUNCTION get_ohlcv_range IS '获取指定时间范围的OHLCV数据';
COMMENT ON FUNCTION update_table_stats IS '更新表统计信息以优化查询性能';