package com.tradingsim.infrastructure.spi.impl;

import com.tradingsim.domain.model.GameDecision;
import com.tradingsim.domain.model.GameSession;
import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.domain.model.DecisionType;
import com.tradingsim.infrastructure.spi.GameStrategyProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认游戏策略提供者实现
 * 提供基础的游戏策略计算
 */
@Component
public class DefaultGameStrategyProvider implements GameStrategyProvider {

    private Map<String, Object> parameters = new HashMap<>();

    public DefaultGameStrategyProvider() {
        // 设置默认参数
        parameters.put("scoreMultiplier", 1.0);
        parameters.put("timeBonus", 0.1);
        parameters.put("accuracyBonus", 0.2);
    }

    @Override
    public String getStrategyName() {
        return "DefaultGameStrategy";
    }

    @Override
    public String getStrategyDescription() {
        return "默认游戏策略，基于盈亏、响应时间和准确性计算分数";
    }

    @Override
    public double calculatePnl(GameDecision decision, double currentPrice, double previousPrice) {
        if (decision == null) {
            return 0.0;
        }

        double priceChange = currentPrice - previousPrice;
        int quantity = decision.getQuantity();

        if (decision.getDecisionType() == DecisionType.LONG) {
            return priceChange * quantity;
        } else if (decision.getDecisionType() == DecisionType.SHORT) {
            return -priceChange * quantity;
        }

        return 0.0;
    }

    @Override
    public double calculateScore(GameSession session, List<GameDecision> decisions) {
        if (session == null || decisions == null || decisions.isEmpty()) {
            return 0.0;
        }

        double totalPnl = session.getTotalPnl().doubleValue();
        double initialBalance = session.getInitialBalance().doubleValue();
        
        // 基础分数：基于收益率
        double returnRate = totalPnl / initialBalance;
        double baseScore = returnRate * 1000; // 基础分数

        // 时间奖励：响应时间越快分数越高
        double avgResponseTime = decisions.stream()
                .mapToLong(GameDecision::getResponseTimeMs)
                .average()
                .orElse(0.0);
        
        double timeBonus = Math.max(0, (5000 - avgResponseTime) / 100); // 5秒内响应有奖励

        // 准确性奖励：盈利决策比例
        long profitableDecisions = decisions.stream()
                .mapToLong(d -> d.getPnl().compareTo(BigDecimal.ZERO) > 0 ? 1 : 0)
                .sum();
        
        double accuracyRate = (double) profitableDecisions / decisions.size();
        double accuracyBonus = accuracyRate * 200; // 准确性奖励

        // 总分数
        double totalScore = baseScore + timeBonus + accuracyBonus;
        
        // 应用策略参数
        double scoreMultiplier = (Double) parameters.getOrDefault("scoreMultiplier", 1.0);
        
        return Math.max(0, totalScore * scoreMultiplier);
    }

    @Override
    public boolean validateDecision(GameDecision decision, GameSession session, OhlcvData marketData) {
        if (decision == null || session == null) {
            return false;
        }

        // 检查决策类型
        if (decision.getDecisionType() == null) {
            return false;
        }

        // 检查数量
        if (decision.getQuantity() <= 0) {
            return false;
        }

        // 检查价格（买卖决策需要价格）
        if (decision.getPrice() == null) {
            return false;
        }

        // 检查余额（买入时需要足够余额）
        if (decision.getDecisionType() == DecisionType.LONG) {
            double requiredAmount = decision.getPrice().doubleValue() * decision.getQuantity();
            if (session.getCurrentBalance().doubleValue() < requiredAmount) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Map<String, Object> getStrategyParameters() {
        return new HashMap<>(parameters);
    }

    @Override
    public void setStrategyParameters(Map<String, Object> parameters) {
        if (parameters != null) {
            this.parameters.putAll(parameters);
        }
    }

    @Override
    public int getPriority() {
        return 100; // 默认优先级
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}