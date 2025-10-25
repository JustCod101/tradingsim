-- 修复枚举类型兼容性问题
-- 将session_status枚举类型改为VARCHAR类型

-- 首先修改game_session表的status列类型
ALTER TABLE game_session ALTER COLUMN status TYPE VARCHAR(20) USING status::text;

-- 为status列添加检查约束，确保只能使用有效值
ALTER TABLE game_session ADD CONSTRAINT check_session_status 
CHECK (status IN ('CREATED', 'RUNNING', 'PAUSED', 'COMPLETED', 'CANCELLED'));

-- 同样处理decision_type枚举
ALTER TABLE game_decision ALTER COLUMN decision_type TYPE VARCHAR(20) USING decision_type::text;
DROP TYPE IF EXISTS decision_type;

-- 为decision_type列添加检查约束
ALTER TABLE game_decision ADD CONSTRAINT check_decision_type 
CHECK (decision_type IN ('LONG', 'SHORT'));