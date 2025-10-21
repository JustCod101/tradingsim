package com.tradingsim.infra.spi;

import com.tradingsim.domain.model.GameDecision;
import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.spi.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 多窗口风险感知评分规则实现
 * 
 * 基于多个时间窗口的复合评分规则:
 * - 支持多个评分窗口 (短期、中期、长期)
 * - 综合考虑收益、风险和交易成本
 * - 使用加权平均计算最终得分
 * - 包含最大回撤、波动率和费用惩罚
 * 
 * 评分公式:
 * Score = Σ(αᵢ * PnLᵢ) - β * MDD - γ * σ - δ * Fee
 * 
 * 其中:
 * - αᵢ: 第i个窗口的权重
 * - PnLᵢ: 第i个窗口的收益率
 * - β: 最大回撤惩罚系数
 * - MDD: 最大回撤
 * - γ: 波动率惩罚系数
 * - σ: 价格波动率
 * - δ: 交易费用系数
 * - Fee: 交易费用
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@Component
public class MultiWindowRiskAwareRule implements Scoring {
    
    private static final String NAME = "MultiWindowRiskAwareRule";
    private static final String VERSION = "1.0.0";
    private static final String DESCRIPTION = "基于多窗口的风险感知评分规则";
    
    // 可配置参数
    private static final int[] DEFAULT_WINDOWS = {5, 10, 20};           // 默认评分窗口
    private static final double[] DEFAULT_WEIGHTS = {0.3, 0.4, 0.3};    // 默认窗口权重
    private static final double DEFAULT_MDD_PENALTY = 0.5;              // 默认最大回撤惩罚系数
    private static final double DEFAULT_VOLATILITY_PENALTY = 0.3;       // 默认波动率惩罚系数
    private static final double DEFAULT_TRADING_FEE = 0.0005;           // 默认交易费率
    private static final double DEFAULT_SKIP_BONUS = 0.002;             // 默认跳过奖励
    private static final double DEFAULT_MOMENTUM_WEIGHT = 0.2;          // 默认动量权重
    private static final double DEFAULT_MEAN_REVERSION_WEIGHT = 0.1;    // 默认均值回归权重
    
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
        int[] windows = config.getIntArrayParameter("windows", DEFAULT_WINDOWS);
        double[] weights = config.getDoubleArrayParameter("weights", DEFAULT_WEIGHTS);
        double mddPenalty = config.getDoubleParameter("mddPenalty", DEFAULT_MDD_PENALTY);
        double volatilityPenalty = config.getDoubleParameter("volatilityPenalty", DEFAULT_VOLATILITY_PENALTY);
        double tradingFee = config.getDoubleParameter("tradingFee", DEFAULT_TRADING_FEE);
        double skipBonus = config.getDoubleParameter("skipBonus", DEFAULT_SKIP_BONUS);
        double momentumWeight = config.getDoubleParameter("momentumWeight", DEFAULT_MOMENTUM_WEIGHT);
        double meanReversionWeight = config.getDoubleParameter("meanReversionWeight", DEFAULT_MEAN_REVERSION_WEIGHT);
        
        // 验证配置
        if (windows.length != weights.length) {
            return createZeroScore("窗口数量与权重数量不匹配");
        }
        
        // 获取决策价格
        BigDecimal decisionPrice = decision.getPrice();
        
        // 计算多窗口收益
        MultiWindowResult multiWindowResult = calculateMultiWindowPnL(decision, decisionPrice, 
                                                                     futureData, windows, weights, tradingFee);
        
        // 计算风险惩罚
        RiskPenalty riskPenalty = calculateAdvancedRiskPenalty(futureData, windows, 
                                                              mddPenalty, volatilityPenalty);
        
        // 计算技术指标调整
        double technicalAdjustment = calculateTechnicalAdjustment(historicalData, futureData, 
                                                                 decision, momentumWeight, meanReversionWeight);
        
        // 计算最终得分
        double finalScore = multiWindowResult.weightedScore - riskPenalty.getTotalPenalty() + technicalAdjustment;
        
        // 跳过决策的特殊处理
        if (decision.isSkip()) {
            finalScore = calculateSkipScore(futureData, skipBonus, riskPenalty);
        }
        
        // 创建评分明细
        Map<String, Double> breakdown = createAdvancedBreakdown(multiWindowResult, riskPenalty, 
                                                               technicalAdjustment, skipBonus, decision.isSkip());
        
        // 创建元数据
        Map<String, Object> metadata = createAdvancedMetadata(decision, decisionPrice, futureData, 
                                                             windows, weights, config, multiWindowResult);
        
        return new ScoringResult(
            finalScore,
            multiWindowResult.averagePnL,
            breakdown,
            riskPenalty,
            metadata
        );
    }
    
    /**
     * 计算多窗口收益
     */
    private MultiWindowResult calculateMultiWindowPnL(GameDecision decision, BigDecimal decisionPrice,
                                                     List<OhlcvData> futureData, int[] windows, 
                                                     double[] weights, double tradingFee) {
        List<Double> windowPnLs = new ArrayList<>();
        List<Double> windowScores = new ArrayList<>();
        double weightedScore = 0.0;
        double totalWeight = 0.0;
        
        for (int i = 0; i < windows.length; i++) {
            int window = windows[i];
            double weight = weights[i];
            
            if (window <= futureData.size()) {
                BigDecimal futurePrice = futureData.get(window - 1).getClose();
                WindowResult windowResult = calculateWindowScore(decision, decisionPrice, 
                                                               futurePrice, tradingFee);
                
                windowPnLs.add(windowResult.pnl);
                windowScores.add(windowResult.score);
                weightedScore += windowResult.score * weight;
                totalWeight += weight;
            }
        }
        
        // 归一化权重
        if (totalWeight > 0) {
            weightedScore /= totalWeight;
        }
        
        double averagePnL = windowPnLs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        return new MultiWindowResult(windowPnLs, windowScores, weightedScore, averagePnL);
    }
    
    /**
     * 计算单个窗口的得分
     */
    private WindowResult calculateWindowScore(GameDecision decision, BigDecimal decisionPrice, 
                                            BigDecimal futurePrice, double tradingFee) {
        BigDecimal priceChange = futurePrice.subtract(decisionPrice);
        double priceChangePercent = priceChange.divide(decisionPrice, 6, RoundingMode.HALF_UP).doubleValue();
        
        double pnl = 0.0;
        double score = 0.0;
        
        if (decision.isBuy()) {
            pnl = priceChangePercent;
            score = pnl - tradingFee;
        } else if (decision.isSell()) {
            pnl = -priceChangePercent;
            score = pnl - tradingFee;
        } else {
            pnl = 0.0;
            score = 0.0;
        }
        
        return new WindowResult(pnl, score);
    }
    
    /**
     * 计算高级风险惩罚
     */
    private RiskPenalty calculateAdvancedRiskPenalty(List<OhlcvData> futureData, int[] windows,
                                                   double mddPenalty, double volatilityPenalty) {
        if (futureData.size() < 2) {
            return new RiskPenalty(0.0, 0.0, 0.0);
        }
        
        // 使用最长窗口计算风险指标
        int maxWindow = Arrays.stream(windows).max().orElse(10);
        int actualWindow = Math.min(maxWindow, futureData.size());
        
        // 计算最大回撤
        double maxDrawdown = calculateMaxDrawdown(futureData, actualWindow);
        double mddPenaltyAmount = maxDrawdown * mddPenalty;
        
        // 计算波动率
        double volatility = calculateVolatility(futureData, actualWindow);
        double volatilityPenaltyAmount = volatility * volatilityPenalty;
        
        // 计算下行风险 (额外的风险度量)
        double downSideRisk = calculateDownSideRisk(futureData, actualWindow);
        double downSidePenalty = downSideRisk * volatilityPenalty * 0.5;
        
        double totalVolatilityPenalty = volatilityPenaltyAmount + downSidePenalty;
        
        return new RiskPenalty(mddPenaltyAmount, totalVolatilityPenalty, 0.0);
    }
    
    /**
     * 计算最大回撤
     */
    private double calculateMaxDrawdown(List<OhlcvData> data, int window) {
        if (data.size() < 2) return 0.0;
        
        BigDecimal peak = data.get(0).getClose();
        double maxDrawdown = 0.0;
        
        for (int i = 1; i < Math.min(window, data.size()); i++) {
            BigDecimal currentPrice = data.get(i).getClose();
            
            if (currentPrice.compareTo(peak) > 0) {
                peak = currentPrice;
            }
            
            double drawdown = peak.subtract(currentPrice)
                .divide(peak, 6, RoundingMode.HALF_UP).doubleValue();
            
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
        }
        
        return maxDrawdown;
    }
    
    /**
     * 计算波动率
     */
    private double calculateVolatility(List<OhlcvData> data, int window) {
        if (data.size() < 2) return 0.0;
        
        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < Math.min(window, data.size()); i++) {
            BigDecimal prevPrice = data.get(i - 1).getClose();
            BigDecimal currPrice = data.get(i).getClose();
            double returnRate = currPrice.subtract(prevPrice)
                .divide(prevPrice, 6, RoundingMode.HALF_UP).doubleValue();
            returns.add(returnRate);
        }
        
        if (returns.isEmpty()) return 0.0;
        
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = returns.stream()
            .mapToDouble(r -> Math.pow(r - mean, 2))
            .average().orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    /**
     * 计算下行风险
     */
    private double calculateDownSideRisk(List<OhlcvData> data, int window) {
        if (data.size() < 2) return 0.0;
        
        List<Double> negativeReturns = new ArrayList<>();
        for (int i = 1; i < Math.min(window, data.size()); i++) {
            BigDecimal prevPrice = data.get(i - 1).getClose();
            BigDecimal currPrice = data.get(i).getClose();
            double returnRate = currPrice.subtract(prevPrice)
                .divide(prevPrice, 6, RoundingMode.HALF_UP).doubleValue();
            
            if (returnRate < 0) {
                negativeReturns.add(returnRate);
            }
        }
        
        if (negativeReturns.isEmpty()) return 0.0;
        
        double variance = negativeReturns.stream()
            .mapToDouble(r -> Math.pow(r, 2))
            .average().orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    /**
     * 计算技术指标调整
     */
    private double calculateTechnicalAdjustment(List<OhlcvData> historicalData, List<OhlcvData> futureData,
                                              GameDecision decision, double momentumWeight, 
                                              double meanReversionWeight) {
        if (historicalData == null || historicalData.size() < 10) {
            return 0.0;
        }
        
        double adjustment = 0.0;
        
        // 动量调整
        double momentum = calculateMomentum(historicalData);
        if (decision.isBuy() && momentum > 0) {
            adjustment += momentum * momentumWeight;
        } else if (decision.isSell() && momentum < 0) {
            adjustment += Math.abs(momentum) * momentumWeight;
        }
        
        // 均值回归调整
        double meanReversion = calculateMeanReversion(historicalData);
        if (decision.isBuy() && meanReversion < 0) {
            adjustment += Math.abs(meanReversion) * meanReversionWeight;
        } else if (decision.isSell() && meanReversion > 0) {
            adjustment += meanReversion * meanReversionWeight;
        }
        
        return adjustment;
    }
    
    /**
     * 计算动量
     */
    private double calculateMomentum(List<OhlcvData> data) {
        if (data.size() < 10) return 0.0;
        
        int size = data.size();
        BigDecimal currentPrice = data.get(size - 1).getClose();
        BigDecimal pastPrice = data.get(size - 10).getClose();
        
        return currentPrice.subtract(pastPrice)
            .divide(pastPrice, 6, RoundingMode.HALF_UP).doubleValue();
    }
    
    /**
     * 计算均值回归
     */
    private double calculateMeanReversion(List<OhlcvData> data) {
        if (data.size() < 20) return 0.0;
        
        // 计算20期移动平均
        BigDecimal sum = BigDecimal.ZERO;
        int period = Math.min(20, data.size());
        for (int i = data.size() - period; i < data.size(); i++) {
            sum = sum.add(data.get(i).getClose());
        }
        BigDecimal movingAverage = sum.divide(BigDecimal.valueOf(period), 6, RoundingMode.HALF_UP);
        
        // 当前价格相对于移动平均的偏离
        BigDecimal currentPrice = data.get(data.size() - 1).getClose();
        return currentPrice.subtract(movingAverage)
            .divide(movingAverage, 6, RoundingMode.HALF_UP).doubleValue();
    }
    
    /**
     * 计算跳过决策的得分
     */
    private double calculateSkipScore(List<OhlcvData> futureData, double skipBonus, RiskPenalty riskPenalty) {
        // 跳过决策在高风险时期获得更多奖励
        double riskLevel = riskPenalty.getTotalPenalty();
        double adaptiveBonus = skipBonus * (1 + riskLevel * 2);
        
        return Math.max(0, adaptiveBonus - riskPenalty.getVolatilityPenalty() * 0.5);
    }
    
    /**
     * 创建高级评分明细
     */
    private Map<String, Double> createAdvancedBreakdown(MultiWindowResult multiWindowResult, 
                                                       RiskPenalty riskPenalty, double technicalAdjustment,
                                                       double skipBonus, boolean isSkip) {
        Map<String, Double> breakdown = new HashMap<>();
        
        breakdown.put("weightedPnL", multiWindowResult.weightedScore);
        breakdown.put("mddPenalty", -riskPenalty.getMddPenalty());
        breakdown.put("volatilityPenalty", -riskPenalty.getVolatilityPenalty());
        breakdown.put("technicalAdjustment", technicalAdjustment);
        
        if (isSkip) {
            breakdown.put("skipBonus", skipBonus);
        }
        
        // 添加各窗口的详细得分
        for (int i = 0; i < multiWindowResult.windowScores.size(); i++) {
            breakdown.put("window" + i + "Score", multiWindowResult.windowScores.get(i));
        }
        
        return breakdown;
    }
    
    /**
     * 创建高级元数据
     */
    private Map<String, Object> createAdvancedMetadata(GameDecision decision, BigDecimal decisionPrice,
                                                      List<OhlcvData> futureData, int[] windows, 
                                                      double[] weights, ScoringConfig config,
                                                      MultiWindowResult multiWindowResult) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("scoringRule", NAME);
        metadata.put("version", VERSION);
        metadata.put("decisionId", decision.getId());
        metadata.put("decisionType", decision.getType().name());
        metadata.put("decisionPrice", decisionPrice.doubleValue());
        metadata.put("windows", windows);
        metadata.put("weights", weights);
        metadata.put("windowPnLs", multiWindowResult.windowPnLs);
        metadata.put("windowScores", multiWindowResult.windowScores);
        metadata.put("futureDataPoints", futureData.size());
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
        config.setParameter("windows", DEFAULT_WINDOWS);
        config.setParameter("weights", DEFAULT_WEIGHTS);
        config.setParameter("mddPenalty", DEFAULT_MDD_PENALTY);
        config.setParameter("volatilityPenalty", DEFAULT_VOLATILITY_PENALTY);
        config.setParameter("tradingFee", DEFAULT_TRADING_FEE);
        config.setParameter("skipBonus", DEFAULT_SKIP_BONUS);
        config.setParameter("momentumWeight", DEFAULT_MOMENTUM_WEIGHT);
        config.setParameter("meanReversionWeight", DEFAULT_MEAN_REVERSION_WEIGHT);
        return config;
    }
    
    @Override
    public boolean validateConfig(ScoringConfig config) {
        if (config == null) return false;
        
        int[] windows = config.getIntArrayParameter("windows", DEFAULT_WINDOWS);
        double[] weights = config.getDoubleArrayParameter("weights", DEFAULT_WEIGHTS);
        
        if (windows.length != weights.length || windows.length == 0) {
            return false;
        }
        
        // 验证窗口大小
        for (int window : windows) {
            if (window <= 0 || window > 100) {
                return false;
            }
        }
        
        // 验证权重
        double weightSum = Arrays.stream(weights).sum();
        if (Math.abs(weightSum - 1.0) > 0.01) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int getMinFutureDataPoints() {
        return Arrays.stream(DEFAULT_WINDOWS).max().orElse(20);
    }
    
    @Override
    public int getPriority() {
        return 5; // 中等优先级
    }
    
    @Override
    public Set<GameDecision.DecisionType> getSupportedDecisionTypes() {
        return Set.of(
            GameDecision.DecisionType.BUY,
            GameDecision.DecisionType.SELL,
            GameDecision.DecisionType.SKIP
        );
    }
    
    /**
     * 多窗口结果内部类
     */
    private static class MultiWindowResult {
        final List<Double> windowPnLs;
        final List<Double> windowScores;
        final double weightedScore;
        final double averagePnL;
        
        MultiWindowResult(List<Double> windowPnLs, List<Double> windowScores, 
                         double weightedScore, double averagePnL) {
            this.windowPnLs = windowPnLs;
            this.windowScores = windowScores;
            this.weightedScore = weightedScore;
            this.averagePnL = averagePnL;
        }
    }
    
    /**
     * 窗口结果内部类
     */
    private static class WindowResult {
        final double pnl;
        final double score;
        
        WindowResult(double pnl, double score) {
            this.pnl = pnl;
            this.score = score;
        }
    }
}