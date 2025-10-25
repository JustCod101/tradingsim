#!/bin/bash

# 数据导入测试脚本
# 测试新实现的数据导入API功能

BASE_URL="http://localhost:8080/api"

echo "=== 股票数据导入测试 ==="
echo

# 1. 检查后端服务状态
echo "1. 检查后端服务状态..."
curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/health" > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ 后端服务运行正常"
else
    echo "❌ 后端服务未启动，请先启动后端服务"
    exit 1
fi
echo

# 2. 获取支持的股票代码
echo "2. 获取支持的股票代码..."
SUPPORTED_STOCKS=$(curl -s "$BASE_URL/data-import/supported-stocks")
echo "支持的股票代码: $SUPPORTED_STOCKS"
echo

# 3. 导入示例数据
echo "3. 导入示例数据..."
echo "正在导入AAPL、TSLA、MSFT等示例股票的近期数据..."
SAMPLE_RESULT=$(curl -s -X POST "$BASE_URL/data-import/sample-data")
echo "示例数据导入结果:"
echo "$SAMPLE_RESULT" | python3 -m json.tool 2>/dev/null || echo "$SAMPLE_RESULT"
echo

# 4. 导入单个股票的最新数据
echo "4. 测试导入单个股票最新数据..."
echo "正在导入AAPL的最新数据..."
LATEST_RESULT=$(curl -s -X POST "$BASE_URL/data-import/latest/AAPL")
echo "最新数据导入结果:"
echo "$LATEST_RESULT" | python3 -m json.tool 2>/dev/null || echo "$LATEST_RESULT"
echo

# 5. 检查数据完整性
echo "5. 检查数据完整性..."
START_TIME=$(date -u -v-7d +"%Y-%m-%dT%H:%M:%S")
END_TIME=$(date -u +"%Y-%m-%dT%H:%M:%S")
echo "检查AAPL过去7天的数据完整性..."
INTEGRITY_RESULT=$(curl -s "$BASE_URL/data-import/integrity/AAPL?startTime=${START_TIME}&endTime=${END_TIME}")
echo "数据完整性报告:"
echo "$INTEGRITY_RESULT" | python3 -m json.tool 2>/dev/null || echo "$INTEGRITY_RESULT"
echo

# 6. 验证数据是否可用于游戏
echo "6. 验证导入的数据是否可用于游戏..."
MARKET_DATA=$(curl -s "$BASE_URL/market-data/AAPL?limit=10")
echo "AAPL最新10条市场数据:"
echo "$MARKET_DATA" | python3 -m json.tool 2>/dev/null || echo "$MARKET_DATA"
echo

# 7. 测试游戏会话创建
echo "7. 测试游戏会话创建..."
SESSION_DATA='{"stockCode":"AAPL","difficulty":"MEDIUM","duration":300}'
SESSION_RESULT=$(curl -s -X POST -H "Content-Type: application/json" -d "$SESSION_DATA" "$BASE_URL/game-sessions")
echo "游戏会话创建结果:"
echo "$SESSION_RESULT" | python3 -m json.tool 2>/dev/null || echo "$SESSION_RESULT"
echo

echo "=== 测试完成 ==="
echo "如果所有测试都成功，说明数据导入功能正常工作，可以开始游戏了！"