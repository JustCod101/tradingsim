package com.tradingsim.spi;

import com.tradingsim.domain.model.GameDecision;
import com.tradingsim.domain.model.OhlcvData;
import java.math.BigDecimal;
import java.util.List;

/**
 * 评分规则 SPI 接口
 * 
 * 用于计算交易决策的评分，支持多种评分算法:
 * - 单窗口简单规则
 * - 多窗口风险感知规则
 * - 夏普比率评分
 * - 自定义评分规则
 * 
 * 实现类需要在 META-INF/services/com.tradingsim.spi.Scoring 中注册
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
public interface Scoring {
    
    /**
     * 获取评分规则名称
     * 
     * @return 评分规则唯一名称
     */
    String getName();
    
    /**
     * 获取评分规则版本
     * 
     * @return 版本号
     */
    String getVersion();
    
    /**
     * 获取评分规则描述
     * 
     * @return 评分规则功能描述
     */
    String getDescription();
    
    /**
     * 计算决策评分
     * 
     * @param decision 交易决策
     * @param futureData 决策后的未来数据窗口 (用于计算盈亏)
     * @param windowSizes 评分窗口大小列表 (可配置)
     * @param weights 各窗口权重 (可配置)
     * @param riskPenalty 风险惩罚参数 (可配置)
     * @param seedValue 随机种子值 (确保可重现性)
     * @return 评分结果
     */
    ScoringResult calculateScore(GameDecision decision,
                               List<OhlcvData> futureData,
                               List<Integer> windowSizes,
                               List<Double> weights,
                               RiskPenalty riskPenalty,
                               long seedValue);
    
    /**
     * 验证评分规则配置
     * 
     * @param config 评分规则配置参数
     * @return 配置是否有效
     */
    default boolean validateConfig(ScoringConfig config) {
        return config != null;
    }
    
    /**
     * 获取默认配置
     * 
     * @return 默认评分规则配置
     */
    default ScoringConfig getDefaultConfig() {
        return new ScoringConfig();
    }
    
    /**
     * 检查是否支持实时评分
     * 
     * @return 是否支持实时评分
     */
    default boolean supportsRealTimeScoring() {
        return true;
    }
    
    /**
     * 获取所需的最小未来数据点数量
     * 
     * @return 最小未来数据点数量
     */
    default int getMinFutureDataPoints() {
        return 5;  // 可配置: 默认最小未来数据点数量
    }
    
    /**
     * 获取评分规则优先级 (数值越小优先级越高)
     * 
     * @return 优先级值
     */
    default int getPriority() {
        return 100;  // 可配置: 默认优先级
    }
    
    /**
     * 获取支持的决策类型
     * 
     * @return 支持的决策类型列表
     */
    default List<String> getSupportedDecisionTypes() {
        return List.of("BUY", "SELL", "SKIP");
    }
}

/**
 * 评分结果
 */
class ScoringResult {
    private final BigDecimal totalScore;        // 总评分
    private final BigDecimal totalPnl;          // 总盈亏
    private final String ruleVersion;           // 规则版本
    private final java.util.Map<String, BigDecimal> scoreBreakdown;  // 评分明细
    private final java.util.Map<String, Object> metadata;           // 额外元数据
    
    public ScoringResult(BigDecimal totalScore, BigDecimal totalPnl, String ruleVersion,
                        java.util.Map<String, BigDecimal> scoreBreakdown,
                        java.util.Map<String, Object> metadata) {
        this.totalScore = totalScore != null ? totalScore : BigDecimal.ZERO;
        this.totalPnl = totalPnl != null ? totalPnl : BigDecimal.ZERO;
        this.ruleVersion = ruleVersion != null ? ruleVersion : "unknown";
        this.scoreBreakdown = scoreBreakdown != null ? 
            new java.util.HashMap<>(scoreBreakdown) : new java.util.HashMap<>();
        this.metadata = metadata != null ? 
            new java.util.HashMap<>(metadata) : new java.util.HashMap<>();
    }
    
    // Getters
    public BigDecimal getTotalScore() { return totalScore; }
    public BigDecimal getTotalPnl() { return totalPnl; }
    public String getRuleVersion() { return ruleVersion; }
    public java.util.Map<String, BigDecimal> getScoreBreakdown() { 
        return new java.util.HashMap<>(scoreBreakdown); 
    }
    public java.util.Map<String, Object> getMetadata() { 
        return new java.util.HashMap<>(metadata); 
    }
    
    /**
     * 获取特定评分项
     */
    public BigDecimal getScoreComponent(String component) {
        return scoreBreakdown.getOrDefault(component, BigDecimal.ZERO);
    }
    
    /**
     * 检查是否盈利
     */
    public boolean isProfitable() {
        return totalPnl.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 检查是否正评分
     */
    public boolean isPositiveScore() {
        return totalScore.compareTo(BigDecimal.ZERO) > 0;
    }
    
    @Override
    public String toString() {
        return String.format("ScoringResult{totalScore=%.2f, totalPnl=%.4f, ruleVersion='%s'}", 
                           totalScore.doubleValue(), totalPnl.doubleValue(), ruleVersion);
    }
}

/**
 * 风险惩罚参数
 */
class RiskPenalty {
    private final double mddPenalty;      // 最大回撤惩罚系数 (可配置)
    private final double sigmaPenalty;    // 波动率惩罚系数 (可配置)
    private final double feePenalty;      // 手续费率 (可配置)
    
    public RiskPenalty(double mddPenalty, double sigmaPenalty, double feePenalty) {
        this.mddPenalty = Math.max(0.0, mddPenalty);
        this.sigmaPenalty = Math.max(0.0, sigmaPenalty);
        this.feePenalty = Math.max(0.0, feePenalty);
    }
    
    // Getters
    public double getMddPenalty() { return mddPenalty; }
    public double getSigmaPenalty() { return sigmaPenalty; }
    public double getFeePenalty() { return feePenalty; }
    
    /**
     * 创建默认风险惩罚参数
     */
    public static RiskPenalty defaultPenalty() {
        return new RiskPenalty(0.5, 0.3, 0.0005);  // 可配置: 默认风险惩罚参数
    }
    
    @Override
    public String toString() {
        return String.format("RiskPenalty{mdd=%.3f, sigma=%.3f, fee=%.5f}", 
                           mddPenalty, sigmaPenalty, feePenalty);
    }
}

/**
 * 评分规则配置类
 */
class ScoringConfig {
    private java.util.Map<String, Object> parameters = new java.util.HashMap<>();
    
    public ScoringConfig() {}
    
    public ScoringConfig(java.util.Map<String, Object> parameters) {
        this.parameters = new java.util.HashMap<>(parameters);
    }
    
    /**
     * 设置参数
     */
    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }
    
    /**
     * 获取参数
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, T defaultValue) {
        Object value = parameters.get(key);
        if (value == null) return defaultValue;
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取整数参数
     */
    public int getIntParameter(String key, int defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    /**
     * 获取双精度参数
     */
    public double getDoubleParameter(String key, double defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
    
    /**
     * 获取布尔参数
     */
    public boolean getBooleanParameter(String key, boolean defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
    
    /**
     * 获取字符串参数
     */
    public String getStringParameter(String key, String defaultValue) {
        Object value = parameters.get(key);
        if (value != null) {
            return value.toString();
        }
        return defaultValue;
    }
    
    /**
     * 获取所有参数
     */
    public java.util.Map<String, Object> getAllParameters() {
        return new java.util.HashMap<>(parameters);
    }
    
    /**
     * 检查是否包含参数
     */
    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }
    
    /**
     * 移除参数
     */
    public void removeParameter(String key) {
        parameters.remove(key);
    }
    
    /**
     * 清空所有参数
     */
    public void clear() {
        parameters.clear();
    }
    
    @Override
    public String toString() {
        return "ScoringConfig{parameters=" + parameters + '}';
    }
}