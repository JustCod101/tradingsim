-- 清理枚举类型依赖关系

-- 删除默认值
ALTER TABLE game_session ALTER COLUMN status DROP DEFAULT;

-- 设置新的默认值
ALTER TABLE game_session ALTER COLUMN status SET DEFAULT 'CREATED';

-- 强制删除枚举类型
DROP TYPE IF EXISTS session_status CASCADE;