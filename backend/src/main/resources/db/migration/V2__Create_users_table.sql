-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    avatar_url VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    roles VARCHAR(255) DEFAULT 'USER',
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 用户会话表（用于记录登录会话）
CREATE TABLE IF NOT EXISTS user_sessions (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    refresh_token_hash VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_token_hash ON user_sessions(token_hash);
CREATE INDEX IF NOT EXISTS idx_user_sessions_expires_at ON user_sessions(expires_at);

-- 修改 game_session 表，添加 user_id 外键
ALTER TABLE game_session ADD COLUMN IF NOT EXISTS user_id VARCHAR(64);
ALTER TABLE game_session ADD CONSTRAINT IF NOT EXISTS fk_game_session_user_id 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-- 创建 game_session 的 user_id 索引
CREATE INDEX IF NOT EXISTS idx_game_session_user_id ON game_session(user_id);

-- 插入一个测试用户账户
-- 密码是 'password123' 的 BCrypt 哈希值
INSERT INTO users (
    id, 
    username, 
    email, 
    password_hash, 
    full_name, 
    phone, 
    status, 
    roles, 
    email_verified,
    created_at,
    updated_at
) VALUES (
    'user_test_001',
    'testuser',
    'test@tradingsim.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZJLjqOm9coFZFp6.1oJSMbmu6MCa', -- password123
    '测试用户',
    '13800138000',
    'ACTIVE',
    'USER',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

-- 为测试用户创建用户统计记录
INSERT INTO user_stats (
    user_id,
    total_sessions,
    completed_sessions,
    total_score,
    best_score,
    total_pnl,
    win_rate,
    avg_response_time_ms,
    created_at,
    updated_at
) VALUES (
    'user_test_001',
    0,
    0,
    0.00,
    0.00,
    0.00,
    0.00,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (user_id) DO NOTHING;