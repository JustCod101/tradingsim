package com.tradingsim.infra.spi;

import com.tradingsim.domain.model.GameDecision;
import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.spi.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 单窗口评分规则实现
 * 
 * 基于单一时间窗口的简单评分规则:
 * - 计算决策后固定窗口期内的收益
 * - 支持做多、做空和跳过决策
 * - 考虑交易费用和风险调整
 * - 使用可配置的评分参数
 * 
 * 评分公式:
 * Score = PnL * Direction - TradingFee - RiskPenalty
 * 
 * 其中:
 * - PnL: 价格变动产生的损益
 * - Direction: 决策方向 (做多=1, 做空=-1, 跳过=0)
 * - TradingFee: 交易费用 (买入/卖出时收取)
 * - RiskPenalty: 风险惩罚 (基于波动率)
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@Component
public class SingleWindowRule implements Scoring {
    
    private static final String NAME = "SingleWindowRule";
    private static final String VERSION = "1.0.0";
    private static final String DESCRIPTION = "基于单一时间窗口的简单评分规则";
    
    // 可配置参数
    private static final int DEFAULT_WINDOW_SIZE = 10;          // 默认评分窗口大小
    private static final double DEFAULT_TRADING_FEE = 0.0005;   // 默认交易费率 (0.05%)
    private static final double DEFAULT_RISK_WEIGHT = 0.1;      // 默认风险权重
    private static final double DEFAULT_SKIP_BONUS = 0.001;     // 默认跳过奖励
    private static final double DEFAULT_MAX_LOSS_PENALTY = 0.1; // 默认最大亏损惩罚
    
    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public String getVersion() {
        return VERSION;
    }
    
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
    
    @Override
    public ScoringResult calculateScore(GameDecision decision, 
                                       List<OhlcvData> historicalData, 
                                       List<OhlcvData> futureData) {
        if (decision == null || futureData == null || futureData.isEmpty()) {
            return createZeroScore("无效的输入数据");
        }
        
        // 获取配置参数
        ScoringConfig config = getDefaultConfig();
        int windowSize = config.getIntParameter("windowSize", DEFAULT_WINDOW_SIZE);
        double tradingFee = config.getDoubleParameter("tradingFee", DEFAULT_TRADING_FEE);
        double riskWeight = config.getDoubleParameter("riskWeight", DEFAULT_RISK_WEIGHT);
        double skipBonus = config.getDoubleParameter("skipBonus", DEFAULT_SKIP_BONUS);
        double maxLossPenalty = config.getDoubleParameter("maxLossPenalty", DEFAULT_MAX_LOSS_PENALTY);
        
        // 确保有足够的未来数据
        int availableData = Math.min(windowSize, futureData.size());
        if (availableData == 0) {
            return createZeroScore("没有足够的未来数据进行评分");
        }
        
        // 获取决策价格和未来价格
        BigDecimal decisionPrice = decision.getPrice();
        BigDecimal futurePrice = futureData.get(availableData - 1).getClose();
        
        // 计算基础PnL
        ScoringResult result = calculateBasicPnL(decision, decisionPrice, futurePrice, tradingFee);
        
        // 计算风险调整
        RiskPenalty riskPenalty = calculateRiskPenalty(futureData, availableData, riskWeight, maxLossPenalty);
        
        // 应用风险调整
        double adjustedScore = result.getTotalScore() - riskPenalty.getTotalPenalty();
        
        // 跳过决策的特殊处理
        if (decision.isSkip()) {
            adjustedScore = Math.max(0, skipBonus - riskPenalty.getVolatilityPenalty());
        }
        
        // 创建评分明细
        Map<String, Double> breakdown = createScoreBreakdown(result, riskPenalty, skipBonus, decision.isSkip());
        
        // 创建元数据
        Map<String, Object> metadata = createMetadata(decision, decisionPrice, futurePrice, 
                                                     availableData, windowSize, config);
        
        return new ScoringResult(
            adjustedScore,
            result.getPnl(),
            breakdown,
            riskPenalty,
            metadata
        );
    }
    
    /**
     * 计算基础损益
     */
    private ScoringResult calculateBasicPnL(GameDecision decision, BigDecimal decisionPrice, 
                                           BigDecimal futurePrice, double tradingFee) {
        BigDecimal priceChange = futurePrice.subtract(decisionPrice);
        double priceChangePercent = priceChange.divide(decisionPrice, 6, RoundingMode.HALF_UP).doubleValue();
        
        double pnl = 0.0;
        double score = 0.0;
        double feeAmount = 0.0;
        
        if (decision.isBuy()) {
            // 做多: 价格上涨获利
            pnl = priceChangePercent;
            feeAmount = tradingFee; // 买入费用
            score = pnl - feeAmount;
        } else if (decision.isSell()) {
            // 做空: 价格下跌获利
            pnl = -priceChangePercent;
            feeAmount = tradingFee; // 卖出费用
            score = pnl - feeAmount;
        } else {
            // 跳过: 无损益，无费用
            pnl = 0.0;
            feeAmount = 0.0;
            score = 0.0;
        }
        
        Map<String, Double> breakdown = new HashMap<>();
        breakdown.put("rawPnL", pnl);
        breakdown.put("tradingFee", -feeAmount);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("priceChange", priceChangePercent);
        metadata.put("decisionType", decision.getType().name());
        
        return new ScoringResult(score, pnl, breakdown, null, metadata);
    }
    
    /**
     * 计算风险惩罚
     */
    private RiskPenalty calculateRiskPenalty(List<OhlcvData> futureData, int windowSize, 
                                           double riskWeight, double maxLossPenalty) {
        if (futureData.size() < 2) {
            return new RiskPenalty(0.0, 0.0, 0.0);
        }
        
        // 计算价格波动率
        double volatility = calculateVolatility(futureData, windowSize);
        double volatilityPenalty = volatility * riskWeight;
        
        // 计算最大回撤
        double maxDrawdown = calculateMaxDrawdown(futureData, windowSize);
        double mddPenalty = Math.min(maxDrawdown * riskWeight * 2, maxLossPenalty);
        
        // 费用惩罚 (在基础计算中已处理)
        double feePenalty = 0.0;
        
        return new RiskPenalty(mddPenalty, volatilityPenalty, feePenalty);
    }
    
    /**
     * 计算价格波动率
     */
    private double calculateVolatility(List<OhlcvData> data, int windowSize) {
        if (data.size() < 2) return 0.0;
        
        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < Math.min(windowSize, data.size()); i++) {
            BigDecimal prevPrice = data.get(i - 1).getClose();
            BigDecimal currPrice = data.get(i).getClose();
            double returnRate = currPrice.subtract(prevPrice)
                .divide(prevPrice, 6, RoundingMode.HALF_UP).doubleValue();
            returns.add(returnRate);
        }
        
        if (returns.isEmpty()) return 0.0;
        
        // 计算标准差
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = returns.stream()
            .mapToDouble(r -> Math.pow(r - mean, 2))
            .average().orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    /**
     * 计算最大回撤
     */
    private double calculateMaxDrawdown(List<OhlcvData> data, int windowSize) {
        if (data.size() < 2) return 0.0;
        
        BigDecimal peak = data.get(0).getClose();
        double maxDrawdown = 0.0;
        
        for (int i = 1; i < Math.min(windowSize, data.size()); i++) {
            BigDecimal currentPrice = data.get(i).getClose();
            
            // 更新峰值
            if (currentPrice.compareTo(peak) > 0) {
                peak = currentPrice;
            }
            
            // 计算当前回撤
            double drawdown = peak.subtract(currentPrice)
                .divide(peak, 6, RoundingMode.HALF_UP).doubleValue();
            
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
        }
        
        return maxDrawdown;
    }
    
    /**
     * 创建评分明细
     */
    private Map<String, Double> createScoreBreakdown(ScoringResult basicResult, RiskPenalty riskPenalty, 
                                                    double skipBonus, boolean isSkip) {
        Map<String, Double> breakdown = new HashMap<>(basicResult.getBreakdown());
        
        if (riskPenalty != null) {
            breakdown.put("mddPenalty", -riskPenalty.getMddPenalty());
            breakdown.put("volatilityPenalty", -riskPenalty.getVolatilityPenalty());
        }
        
        if (isSkip) {
            breakdown.put("skipBonus", skipBonus);
        }
        
        return breakdown;
    }
    
    /**
     * 创建元数据
     */
    private Map<String, Object> createMetadata(GameDecision decision, BigDecimal decisionPrice, 
                                             BigDecimal futurePrice, int actualWindow, int configWindow,
                                             ScoringConfig config) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("scoringRule", NAME);
        metadata.put("version", VERSION);
        metadata.put("decisionId", decision.getId());
        metadata.put("decisionType", decision.getType().name());
        metadata.put("decisionPrice", decisionPrice.doubleValue());
        metadata.put("futurePrice", futurePrice.doubleValue());
        metadata.put("actualWindow", actualWindow);
        metadata.put("configWindow", configWindow);
        metadata.put("timestamp", System.currentTimeMillis());
        
        // 添加配置参数
        metadata.put("config", config.getAllParameters());
        
        return metadata;
    }
    
    /**
     * 创建零分结果
     */
    private ScoringResult createZeroScore(String reason) {
        Map<String, Double> breakdown = new HashMap<>();
        breakdown.put("reason", 0.0);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("scoringRule", NAME);
        metadata.put("errorReason", reason);
        metadata.put("timestamp", System.currentTimeMillis());
        
        return new ScoringResult(0.0, 0.0, breakdown, null, metadata);
    }
    
    @Override
    public ScoringConfig getDefaultConfig() {
        ScoringConfig config = new ScoringConfig();
        config.setParameter("windowSize", DEFAULT_WINDOW_SIZE);
        config.setParameter("tradingFee", DEFAULT_TRADING_FEE);
        config.setParameter("riskWeight", DEFAULT_RISK_WEIGHT);
        config.setParameter("skipBonus", DEFAULT_SKIP_BONUS);
        config.setParameter("maxLossPenalty", DEFAULT_MAX_LOSS_PENALTY);
        return config;
    }
    
    @Override
    public boolean validateConfig(ScoringConfig config) {
        if (config == null) return false;
        
        int windowSize = config.getIntParameter("windowSize", DEFAULT_WINDOW_SIZE);
        double tradingFee = config.getDoubleParameter("tradingFee", DEFAULT_TRADING_FEE);
        double riskWeight = config.getDoubleParameter("riskWeight", DEFAULT_RISK_WEIGHT);
        
        return windowSize > 0 && windowSize <= 100 &&
               tradingFee >= 0.0 && tradingFee <= 0.01 &&
               riskWeight >= 0.0 && riskWeight <= 1.0;
    }
    
    @Override
    public int getMinFutureDataPoints() {
        return DEFAULT_WINDOW_SIZE;
    }
    
    @Override
    public int getPriority() {
        return 10; // 高优先级
    }
    
    @Override
    public Set<GameDecision.DecisionType> getSupportedDecisionTypes() {
        return Set.of(
            GameDecision.DecisionType.BUY,
            GameDecision.DecisionType.SELL,
            GameDecision.DecisionType.SKIP
        );
    }
}