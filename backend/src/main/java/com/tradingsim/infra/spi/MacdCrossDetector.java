package com.tradingsim.infra.spi;

import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.spi.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * MACD交叉检测器实现
 * 
 * 基于MACD指标的金叉死叉检测关键交易点:
 * - 检测MACD线与信号线的交叉点
 * - 支持可配置的EMA周期参数
 * - 使用随机种子确保结果可重现
 * - 支持强度和趋势分析
 * 
 * 算法原理:
 * 1. 计算快速EMA(12)和慢速EMA(26)
 * 2. 计算MACD线 = 快速EMA - 慢速EMA
 * 3. 计算信号线 = MACD的EMA(9)
 * 4. 检测MACD线与信号线的交叉点
 * 5. 根据交叉强度和趋势确定置信度
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@Component
public class MacdCrossDetector implements Detector {
    
    private static final String NAME = "MacdCrossDetector";
    private static final String VERSION = "1.0.0";
    private static final String DESCRIPTION = "基于MACD交叉的关键点检测器";
    
    // 可配置参数
    private static final int DEFAULT_FAST_PERIOD = 12;      // 快速EMA周期
    private static final int DEFAULT_SLOW_PERIOD = 26;      // 慢速EMA周期
    private static final int DEFAULT_SIGNAL_PERIOD = 9;     // 信号线EMA周期
    private static final double DEFAULT_MIN_STRENGTH = 0.001; // 最小交叉强度
    private static final double DEFAULT_TREND_WEIGHT = 0.4;   // 趋势权重
    private static final double DEFAULT_VOLUME_WEIGHT = 0.3;  // 成交量权重
    
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
    public List<KeypointDetection> detectKeypoints(List<OhlcvData> ohlcvData, 
                                                  int minCount, 
                                                  int maxCount, 
                                                  long seedValue) {
        if (ohlcvData == null || ohlcvData.size() < getMinDataPoints()) {
            return Collections.emptyList();
        }
        
        // 使用种子初始化随机数生成器
        Random random = new Random(seedValue);
        
        // 获取配置参数
        DetectorConfig config = getDefaultConfig();
        int fastPeriod = config.getIntParameter("fastPeriod", DEFAULT_FAST_PERIOD);
        int slowPeriod = config.getIntParameter("slowPeriod", DEFAULT_SLOW_PERIOD);
        int signalPeriod = config.getIntParameter("signalPeriod", DEFAULT_SIGNAL_PERIOD);
        double minStrength = config.getDoubleParameter("minStrength", DEFAULT_MIN_STRENGTH);
        double trendWeight = config.getDoubleParameter("trendWeight", DEFAULT_TREND_WEIGHT);
        double volumeWeight = config.getDoubleParameter("volumeWeight", DEFAULT_VOLUME_WEIGHT);
        
        // 计算MACD指标
        MacdData macdData = calculateMacd(ohlcvData, fastPeriod, slowPeriod, signalPeriod);
        
        // 检测交叉点
        List<CrossCandidate> candidates = detectCrosses(ohlcvData, macdData, minStrength, 
                                                       trendWeight, volumeWeight);
        
        // 按置信度排序
        candidates.sort((a, b) -> Double.compare(b.confidence, a.confidence));
        
        // 随机选择关键点
        List<KeypointDetection> selectedKeypoints = selectKeypoints(candidates, minCount, maxCount, random);
        
        // 按帧索引排序
        selectedKeypoints.sort(Comparator.comparingInt(KeypointDetection::getFrameIndex));
        
        return selectedKeypoints;
    }
    
    /**
     * 计算MACD指标
     */
    private MacdData calculateMacd(List<OhlcvData> ohlcvData, int fastPeriod, int slowPeriod, int signalPeriod) {
        int dataSize = ohlcvData.size();
        double[] prices = new double[dataSize];
        double[] fastEma = new double[dataSize];
        double[] slowEma = new double[dataSize];
        double[] macdLine = new double[dataSize];
        double[] signalLine = new double[dataSize];
        double[] histogram = new double[dataSize];
        
        // 提取收盘价
        for (int i = 0; i < dataSize; i++) {
            prices[i] = ohlcvData.get(i).getClose().doubleValue();
        }
        
        // 计算快速EMA
        calculateEma(prices, fastEma, fastPeriod);
        
        // 计算慢速EMA
        calculateEma(prices, slowEma, slowPeriod);
        
        // 计算MACD线
        for (int i = 0; i < dataSize; i++) {
            macdLine[i] = fastEma[i] - slowEma[i];
        }
        
        // 计算信号线 (MACD的EMA)
        calculateEma(macdLine, signalLine, signalPeriod);
        
        // 计算柱状图
        for (int i = 0; i < dataSize; i++) {
            histogram[i] = macdLine[i] - signalLine[i];
        }
        
        return new MacdData(macdLine, signalLine, histogram);
    }
    
    /**
     * 计算指数移动平均线
     */
    private void calculateEma(double[] data, double[] ema, int period) {
        if (data.length == 0) return;
        
        double multiplier = 2.0 / (period + 1);
        
        // 第一个值使用简单移动平均
        double sum = 0;
        int startIndex = Math.min(period, data.length);
        for (int i = 0; i < startIndex; i++) {
            sum += data[i];
            ema[i] = sum / (i + 1);  // 前期使用SMA
        }
        
        // 后续使用EMA公式
        for (int i = startIndex; i < data.length; i++) {
            ema[i] = (data[i] * multiplier) + (ema[i - 1] * (1 - multiplier));
        }
    }
    
    /**
     * 检测MACD交叉点
     */
    private List<CrossCandidate> detectCrosses(List<OhlcvData> ohlcvData, MacdData macdData, 
                                              double minStrength, double trendWeight, double volumeWeight) {
        List<CrossCandidate> candidates = new ArrayList<>();
        int dataSize = ohlcvData.size();
        
        // 计算平均成交量
        double avgVolume = ohlcvData.stream()
            .mapToLong(OhlcvData::getVolume)
            .average()
            .orElse(1.0);
        
        // 检测交叉点 (从第2个点开始，需要前一个点做比较)
        for (int i = 1; i < dataSize; i++) {
            double prevHistogram = macdData.histogram[i - 1];
            double currHistogram = macdData.histogram[i];
            
            // 检测金叉 (从负转正)
            if (prevHistogram <= 0 && currHistogram > 0) {
                double strength = Math.abs(currHistogram);
                if (strength >= minStrength) {
                    double confidence = calculateCrossConfidence(ohlcvData, macdData, i, true, 
                                                               strength, avgVolume, trendWeight, volumeWeight);
                    
                    Map<String, Object> metadata = createCrossMetadata(macdData, i, "GOLDEN_CROSS", strength);
                    candidates.add(new CrossCandidate(i, confidence, KeypointType.BULLISH_SIGNAL, 
                                                    "MACD金叉", metadata));
                }
            }
            
            // 检测死叉 (从正转负)
            if (prevHistogram >= 0 && currHistogram < 0) {
                double strength = Math.abs(currHistogram);
                if (strength >= minStrength) {
                    double confidence = calculateCrossConfidence(ohlcvData, macdData, i, false, 
                                                               strength, avgVolume, trendWeight, volumeWeight);
                    
                    Map<String, Object> metadata = createCrossMetadata(macdData, i, "DEATH_CROSS", strength);
                    candidates.add(new CrossCandidate(i, confidence, KeypointType.BEARISH_SIGNAL, 
                                                    "MACD死叉", metadata));
                }
            }
        }
        
        return candidates;
    }
    
    /**
     * 计算交叉点置信度
     */
    private double calculateCrossConfidence(List<OhlcvData> ohlcvData, MacdData macdData, int index, 
                                          boolean isGoldenCross, double strength, double avgVolume,
                                          double trendWeight, double volumeWeight) {
        // 基础置信度 = 交叉强度
        double baseConfidence = Math.min(1.0, strength * 1000); // 放大强度值
        
        // 趋势一致性分析
        double trendBonus = calculateTrendConsistency(macdData, index, isGoldenCross) * trendWeight;
        
        // 成交量确认
        OhlcvData currentData = ohlcvData.get(index);
        double volumeRatio = currentData.getVolume() / avgVolume;
        double volumeBonus = Math.min(0.3, (volumeRatio - 1.0) * volumeWeight);
        
        // MACD位置分析 (零轴上方/下方)
        double macdPosition = macdData.macdLine[index];
        double positionBonus = 0.0;
        if (isGoldenCross && macdPosition > 0) {
            positionBonus = 0.1; // 零轴上方的金叉更强
        } else if (!isGoldenCross && macdPosition < 0) {
            positionBonus = 0.1; // 零轴下方的死叉更强
        }
        
        // 综合置信度
        double confidence = baseConfidence + trendBonus + Math.max(0, volumeBonus) + positionBonus;
        
        return Math.max(0.0, Math.min(1.0, confidence));
    }
    
    /**
     * 计算趋势一致性
     */
    private double calculateTrendConsistency(MacdData macdData, int index, boolean isGoldenCross) {
        if (index < 5) return 0.0; // 数据不足
        
        // 检查前5个周期的MACD趋势
        int consistentCount = 0;
        for (int i = index - 4; i < index; i++) {
            double macdChange = macdData.macdLine[i] - macdData.macdLine[i - 1];
            if (isGoldenCross && macdChange > 0) {
                consistentCount++;
            } else if (!isGoldenCross && macdChange < 0) {
                consistentCount++;
            }
        }
        
        return (double) consistentCount / 4.0; // 返回一致性比例
    }
    
    /**
     * 创建交叉点元数据
     */
    private Map<String, Object> createCrossMetadata(MacdData macdData, int index, String crossType, double strength) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("crossType", crossType);
        metadata.put("strength", strength);
        metadata.put("macdValue", macdData.macdLine[index]);
        metadata.put("signalValue", macdData.signalLine[index]);
        metadata.put("histogram", macdData.histogram[index]);
        metadata.put("detectorName", NAME);
        return metadata;
    }
    
    /**
     * 选择最终的关键点
     */
    private List<KeypointDetection> selectKeypoints(List<CrossCandidate> candidates, 
                                                   int minCount, int maxCount, Random random) {
        List<KeypointDetection> selected = new ArrayList<>();
        
        if (candidates.isEmpty()) {
            return selected;
        }
        
        // 确保选择最高置信度的关键点以满足最小数量要求
        int guaranteedCount = Math.min(minCount, candidates.size());
        for (int i = 0; i < guaranteedCount; i++) {
            CrossCandidate candidate = candidates.get(i);
            selected.add(createKeypointDetection(candidate));
        }
        
        // 随机选择剩余的关键点
        List<CrossCandidate> remaining = candidates.subList(guaranteedCount, candidates.size());
        int additionalCount = Math.min(maxCount - guaranteedCount, remaining.size());
        
        if (additionalCount > 0) {
            Collections.shuffle(remaining, random);
            for (int i = 0; i < additionalCount; i++) {
                CrossCandidate candidate = remaining.get(i);
                // 使用概率选择 (置信度越高，被选中的概率越大)
                if (random.nextDouble() < candidate.confidence) {
                    selected.add(createKeypointDetection(candidate));
                }
            }
        }
        
        return selected;
    }
    
    /**
     * 创建关键点检测结果
     */
    private KeypointDetection createKeypointDetection(CrossCandidate candidate) {
        return new KeypointDetection(
            candidate.frameIndex,
            NAME,
            candidate.confidence,
            candidate.type,
            candidate.reason,
            candidate.metadata
        );
    }
    
    @Override
    public DetectorConfig getDefaultConfig() {
        DetectorConfig config = new DetectorConfig();
        config.setParameter("fastPeriod", DEFAULT_FAST_PERIOD);
        config.setParameter("slowPeriod", DEFAULT_SLOW_PERIOD);
        config.setParameter("signalPeriod", DEFAULT_SIGNAL_PERIOD);
        config.setParameter("minStrength", DEFAULT_MIN_STRENGTH);
        config.setParameter("trendWeight", DEFAULT_TREND_WEIGHT);
        config.setParameter("volumeWeight", DEFAULT_VOLUME_WEIGHT);
        return config;
    }
    
    @Override
    public boolean validateConfig(DetectorConfig config) {
        if (config == null) return false;
        
        int fastPeriod = config.getIntParameter("fastPeriod", DEFAULT_FAST_PERIOD);
        int slowPeriod = config.getIntParameter("slowPeriod", DEFAULT_SLOW_PERIOD);
        int signalPeriod = config.getIntParameter("signalPeriod", DEFAULT_SIGNAL_PERIOD);
        
        return fastPeriod > 0 && slowPeriod > fastPeriod && signalPeriod > 0 &&
               fastPeriod <= 50 && slowPeriod <= 100 && signalPeriod <= 30;
    }
    
    @Override
    public int getMinDataPoints() {
        return DEFAULT_SLOW_PERIOD + DEFAULT_SIGNAL_PERIOD; // 需要足够的数据计算MACD
    }
    
    @Override
    public int getPriority() {
        return 8;  // 中高优先级
    }
    
    /**
     * MACD数据内部类
     */
    private static class MacdData {
        final double[] macdLine;
        final double[] signalLine;
        final double[] histogram;
        
        MacdData(double[] macdLine, double[] signalLine, double[] histogram) {
            this.macdLine = macdLine;
            this.signalLine = signalLine;
            this.histogram = histogram;
        }
    }
    
    /**
     * 交叉候选者内部类
     */
    private static class CrossCandidate {
        final int frameIndex;
        final double confidence;
        final KeypointType type;
        final String reason;
        final Map<String, Object> metadata;
        
        CrossCandidate(int frameIndex, double confidence, KeypointType type, 
                      String reason, Map<String, Object> metadata) {
            this.frameIndex = frameIndex;
            this.confidence = confidence;
            this.type = type;
            this.reason = reason;
            this.metadata = metadata;
        }
    }
}