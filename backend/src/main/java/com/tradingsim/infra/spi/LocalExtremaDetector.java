package com.tradingsim.infra.spi;

import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.spi.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.IntStream;

/**
 * 局部极值检测器实现
 * 
 * 基于价格的局部高点和低点检测关键交易点:
 * - 检测局部最高价和最低价
 * - 支持可配置的检测窗口大小
 * - 使用随机种子确保结果可重现
 * - 支持置信度评估
 * 
 * 算法原理:
 * 1. 使用滑动窗口检测局部极值
 * 2. 计算极值的显著性 (相对于周围价格的偏差)
 * 3. 根据成交量和波动率调整置信度
 * 4. 随机选择符合条件的关键点
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@Component
public class LocalExtremaDetector implements Detector {
    
    private static final String NAME = "LocalExtremaDetector";
    private static final String VERSION = "1.0.0";
    private static final String DESCRIPTION = "基于局部极值的关键点检测器";
    
    // 可配置参数
    private static final int DEFAULT_WINDOW_SIZE = 5;           // 默认检测窗口大小
    private static final double DEFAULT_MIN_SIGNIFICANCE = 0.01; // 默认最小显著性阈值
    private static final double DEFAULT_VOLUME_WEIGHT = 0.3;     // 默认成交量权重
    private static final double DEFAULT_VOLATILITY_WEIGHT = 0.2; // 默认波动率权重
    
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
        int windowSize = config.getIntParameter("windowSize", DEFAULT_WINDOW_SIZE);
        double minSignificance = config.getDoubleParameter("minSignificance", DEFAULT_MIN_SIGNIFICANCE);
        double volumeWeight = config.getDoubleParameter("volumeWeight", DEFAULT_VOLUME_WEIGHT);
        double volatilityWeight = config.getDoubleParameter("volatilityWeight", DEFAULT_VOLATILITY_WEIGHT);
        
        // 检测所有候选关键点
        List<KeypointCandidate> candidates = detectCandidates(ohlcvData, windowSize, 
                                                             minSignificance, volumeWeight, volatilityWeight);
        
        // 按置信度排序
        candidates.sort((a, b) -> Double.compare(b.confidence, a.confidence));
        
        // 随机选择关键点 (在保证最小数量的前提下)
        List<KeypointDetection> selectedKeypoints = selectKeypoints(candidates, minCount, maxCount, random);
        
        // 按帧索引排序
        selectedKeypoints.sort(Comparator.comparingInt(KeypointDetection::getFrameIndex));
        
        return selectedKeypoints;
    }
    
    /**
     * 检测候选关键点
     */
    private List<KeypointCandidate> detectCandidates(List<OhlcvData> ohlcvData, 
                                                    int windowSize,
                                                    double minSignificance,
                                                    double volumeWeight,
                                                    double volatilityWeight) {
        List<KeypointCandidate> candidates = new ArrayList<>();
        int dataSize = ohlcvData.size();
        
        // 计算平均成交量 (用于归一化)
        double avgVolume = ohlcvData.stream()
            .mapToLong(OhlcvData::getVolume)
            .average()
            .orElse(1.0);
        
        // 滑动窗口检测局部极值
        for (int i = windowSize; i < dataSize - windowSize; i++) {
            OhlcvData current = ohlcvData.get(i);
            
            // 检测局部高点
            if (isLocalHigh(ohlcvData, i, windowSize)) {
                double significance = calculateSignificance(ohlcvData, i, windowSize, true);
                if (significance >= minSignificance) {
                    double confidence = calculateConfidence(current, significance, avgVolume, 
                                                          volumeWeight, volatilityWeight);
                    
                    Map<String, Object> metadata = createMetadata(current, significance, "LOCAL_HIGH");
                    candidates.add(new KeypointCandidate(i, confidence, KeypointType.LOCAL_HIGH, 
                                                       "局部高点", metadata));
                }
            }
            
            // 检测局部低点
            if (isLocalLow(ohlcvData, i, windowSize)) {
                double significance = calculateSignificance(ohlcvData, i, windowSize, false);
                if (significance >= minSignificance) {
                    double confidence = calculateConfidence(current, significance, avgVolume, 
                                                          volumeWeight, volatilityWeight);
                    
                    Map<String, Object> metadata = createMetadata(current, significance, "LOCAL_LOW");
                    candidates.add(new KeypointCandidate(i, confidence, KeypointType.LOCAL_LOW, 
                                                       "局部低点", metadata));
                }
            }
        }
        
        return candidates;
    }
    
    /**
     * 检查是否为局部高点
     */
    private boolean isLocalHigh(List<OhlcvData> data, int index, int windowSize) {
        BigDecimal currentHigh = data.get(index).getHigh();
        
        // 检查左侧窗口
        for (int i = index - windowSize; i < index; i++) {
            if (data.get(i).getHigh().compareTo(currentHigh) >= 0) {
                return false;
            }
        }
        
        // 检查右侧窗口
        for (int i = index + 1; i <= index + windowSize; i++) {
            if (data.get(i).getHigh().compareTo(currentHigh) >= 0) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 检查是否为局部低点
     */
    private boolean isLocalLow(List<OhlcvData> data, int index, int windowSize) {
        BigDecimal currentLow = data.get(index).getLow();
        
        // 检查左侧窗口
        for (int i = index - windowSize; i < index; i++) {
            if (data.get(i).getLow().compareTo(currentLow) <= 0) {
                return false;
            }
        }
        
        // 检查右侧窗口
        for (int i = index + 1; i <= index + windowSize; i++) {
            if (data.get(i).getLow().compareTo(currentLow) <= 0) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 计算极值的显著性
     */
    private double calculateSignificance(List<OhlcvData> data, int index, int windowSize, boolean isHigh) {
        OhlcvData current = data.get(index);
        BigDecimal extremePrice = isHigh ? current.getHigh() : current.getLow();
        
        // 计算窗口内的平均价格
        BigDecimal avgPrice = BigDecimal.ZERO;
        int count = 0;
        
        for (int i = Math.max(0, index - windowSize); 
             i <= Math.min(data.size() - 1, index + windowSize); i++) {
            if (i != index) {
                BigDecimal price = isHigh ? data.get(i).getHigh() : data.get(i).getLow();
                avgPrice = avgPrice.add(price);
                count++;
            }
        }
        
        if (count == 0) return 0.0;
        avgPrice = avgPrice.divide(BigDecimal.valueOf(count), 4, java.math.RoundingMode.HALF_UP);
        
        // 计算相对偏差
        BigDecimal deviation = extremePrice.subtract(avgPrice).abs();
        double significance = deviation.divide(avgPrice, 6, java.math.RoundingMode.HALF_UP).doubleValue();
        
        return significance;
    }
    
    /**
     * 计算置信度
     */
    private double calculateConfidence(OhlcvData data, double significance, double avgVolume,
                                     double volumeWeight, double volatilityWeight) {
        // 基础置信度 = 显著性
        double baseConfidence = Math.min(1.0, significance * 10); // 放大显著性
        
        // 成交量调整
        double volumeRatio = data.getVolume() / avgVolume;
        double volumeBonus = Math.min(0.3, volumeRatio * volumeWeight);
        
        // 波动率调整 (价格范围相对于开盘价)
        double volatility = data.getPriceRangePercent().doubleValue() / 100.0;
        double volatilityBonus = Math.min(0.2, volatility * volatilityWeight);
        
        // 综合置信度
        double confidence = baseConfidence + volumeBonus + volatilityBonus;
        
        return Math.max(0.0, Math.min(1.0, confidence));
    }
    
    /**
     * 创建元数据
     */
    private Map<String, Object> createMetadata(OhlcvData data, double significance, String type) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("significance", significance);
        metadata.put("price", data.getClose().doubleValue());
        metadata.put("volume", data.getVolume());
        metadata.put("volatility", data.getPriceRangePercent().doubleValue());
        metadata.put("detectionType", type);
        metadata.put("timestamp", data.getTimestamp().toString());
        return metadata;
    }
    
    /**
     * 选择最终的关键点
     */
    private List<KeypointDetection> selectKeypoints(List<KeypointCandidate> candidates, 
                                                   int minCount, int maxCount, Random random) {
        List<KeypointDetection> selected = new ArrayList<>();
        
        if (candidates.isEmpty()) {
            return selected;
        }
        
        // 确保选择最高置信度的关键点以满足最小数量要求
        int guaranteedCount = Math.min(minCount, candidates.size());
        for (int i = 0; i < guaranteedCount; i++) {
            KeypointCandidate candidate = candidates.get(i);
            selected.add(createKeypointDetection(candidate));
        }
        
        // 随机选择剩余的关键点
        List<KeypointCandidate> remaining = candidates.subList(guaranteedCount, candidates.size());
        int additionalCount = Math.min(maxCount - guaranteedCount, remaining.size());
        
        if (additionalCount > 0) {
            Collections.shuffle(remaining, random);
            for (int i = 0; i < additionalCount; i++) {
                KeypointCandidate candidate = remaining.get(i);
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
    private KeypointDetection createKeypointDetection(KeypointCandidate candidate) {
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
        config.setParameter("windowSize", DEFAULT_WINDOW_SIZE);
        config.setParameter("minSignificance", DEFAULT_MIN_SIGNIFICANCE);
        config.setParameter("volumeWeight", DEFAULT_VOLUME_WEIGHT);
        config.setParameter("volatilityWeight", DEFAULT_VOLATILITY_WEIGHT);
        return config;
    }
    
    @Override
    public boolean validateConfig(DetectorConfig config) {
        if (config == null) return false;
        
        int windowSize = config.getIntParameter("windowSize", DEFAULT_WINDOW_SIZE);
        double minSignificance = config.getDoubleParameter("minSignificance", DEFAULT_MIN_SIGNIFICANCE);
        
        return windowSize > 0 && windowSize <= 20 && 
               minSignificance >= 0.0 && minSignificance <= 1.0;
    }
    
    @Override
    public int getMinDataPoints() {
        return DEFAULT_WINDOW_SIZE * 2 + 1;  // 至少需要两倍窗口大小的数据
    }
    
    @Override
    public int getPriority() {
        return 10;  // 高优先级
    }
    
    /**
     * 关键点候选者内部类
     */
    private static class KeypointCandidate {
        final int frameIndex;
        final double confidence;
        final KeypointType type;
        final String reason;
        final Map<String, Object> metadata;
        
        KeypointCandidate(int frameIndex, double confidence, KeypointType type, 
                         String reason, Map<String, Object> metadata) {
            this.frameIndex = frameIndex;
            this.confidence = confidence;
            this.type = type;
            this.reason = reason;
            this.metadata = metadata;
        }
    }
}