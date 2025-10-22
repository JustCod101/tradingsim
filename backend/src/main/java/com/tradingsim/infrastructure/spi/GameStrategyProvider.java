package com.tradingsim.infrastructure.spi;

import com.tradingsim.domain.model.GameDecision;
import com.tradingsim.domain.model.GameSession;
import com.tradingsim.domain.model.OhlcvData;

import java.util.List;
import java.util.Map;

/**
 * 游戏策略提供者SPI接口
 * 允许插件化的游戏策略实现
 */
public interface GameStrategyProvider {

    /**
     * 获取策略名称
     */
    String getStrategyName();

    /**
     * 获取策略描述
     */
    String getStrategyDescription();

    /**
     * 计算决策的盈亏
     * 
     * @param decision 游戏决策
     * @param currentPrice 当前价格
     * @param previousPrice 前一价格
     * @return 盈亏金额
     */
    double calculatePnl(GameDecision decision, double currentPrice, double previousPrice);

    /**
     * 计算游戏会话的分数
     * 
     * @param session 游戏会话
     * @param decisions 决策列表
     * @return 分数
     */
    double calculateScore(GameSession session, List<GameDecision> decisions);

    /**
     * 验证决策是否有效
     * 
     * @param decision 游戏决策
     * @param session 游戏会话
     * @param marketData 市场数据
     * @return 是否有效
     */
    boolean validateDecision(GameDecision decision, GameSession session, OhlcvData marketData);

    /**
     * 获取策略配置参数
     */
    Map<String, Object> getStrategyParameters();

    /**
     * 设置策略配置参数
     */
    void setStrategyParameters(Map<String, Object> parameters);

    /**
     * 获取策略优先级（数字越小优先级越高）
     */
    int getPriority();

    /**
     * 检查策略是否启用
     */
    boolean isEnabled();
}