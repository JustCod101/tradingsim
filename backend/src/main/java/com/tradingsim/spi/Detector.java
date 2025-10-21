package com.tradingsim.spi;

import com.tradingsim.domain.model.OhlcvData;
import java.util.List;

/**
 * 关键点检测器 SPI 接口
 * 
 * 用于检测K线数据中的关键交易点，支持多种检测算法:
 * - 局部极值检测
 * - MACD交叉检测
 * - 波动率跳跃检测
 * - 自定义技术指标检测
 * 
 * 实现类需要在 META-INF/services/com.tradingsim.spi.Detector 中注册
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
public interface Detector {
    
    /**
     * 获取检测器名称
     * 
     * @return 检测器唯一名称
     */
    String getName();
    
    /**
     * 获取检测器版本
     * 
     * @return 版本号
     */
    String getVersion();
    
    /**
     * 获取检测器描述
     * 
     * @return 检测器功能描述
     */
    String getDescription();
    
    /**
     * 检测关键点
     * 
     * @param ohlcvData K线数据列表 (按时间顺序排列)
     * @param minCount 最小关键点数量 (可配置)
     * @param maxCount 最大关键点数量 (可配置)
     * @param seedValue 随机种子值 (确保可重现性)
     * @return 关键点检测结果列表
     */
    List<KeypointDetection> detectKeypoints(List<OhlcvData> ohlcvData, 
                                          int minCount, 
                                          int maxCount, 
                                          long seedValue);
    
    /**
     * 验证检测器配置
     * 
     * @param config 检测器配置参数
     * @return 配置是否有效
     */
    default boolean validateConfig(DetectorConfig config) {
        return config != null;
    }
    
    /**
     * 获取默认配置
     * 
     * @return 默认检测器配置
     */
    default DetectorConfig getDefaultConfig() {
        return new DetectorConfig();
    }
    
    /**
     * 检查是否支持实时检测
     * 
     * @return 是否支持实时检测
     */
    default boolean supportsRealTimeDetection() {
        return false;
    }
    
    /**
     * 获取所需的最小数据点数量
     * 
     * @return 最小数据点数量
     */
    default int getMinDataPoints() {
        return 10;  // 可配置: 默认最小数据点数量
    }
    
    /**
     * 获取检测器优先级 (数值越小优先级越高)
     * 
     * @return 优先级值
     */
    default int getPriority() {
        return 100;  // 可配置: 默认优先级
    }
}

/**
 * 关键点检测结果
 */
class KeypointDetection {
    private final int frameIndex;           // 帧索引
    private final String detectorName;      // 检测器名称
    private final double confidence;        // 检测置信度 (0.0-1.0)
    private final KeypointType type;        // 关键点类型
    private final String reason;            // 检测原因
    private final java.util.Map<String, Object> metadata;  // 额外元数据
    
    public KeypointDetection(int frameIndex, String detectorName, double confidence, 
                           KeypointType type, String reason, 
                           java.util.Map<String, Object> metadata) {
        this.frameIndex = frameIndex;
        this.detectorName = detectorName;
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));  // 限制在 [0,1] 范围
        this.type = type;
        this.reason = reason;
        this.metadata = metadata != null ? new java.util.HashMap<>(metadata) : new java.util.HashMap<>();
    }
    
    // Getters
    public int getFrameIndex() { return frameIndex; }
    public String getDetectorName() { return detectorName; }
    public double getConfidence() { return confidence; }
    public KeypointType getType() { return type; }
    public String getReason() { return reason; }
    public java.util.Map<String, Object> getMetadata() { return new java.util.HashMap<>(metadata); }
    
    @Override
    public String toString() {
        return String.format("KeypointDetection{frame=%d, detector='%s', confidence=%.3f, type=%s, reason='%s'}", 
                           frameIndex, detectorName, confidence, type, reason);
    }
}

/**
 * 关键点类型枚举
 */
enum KeypointType {
    LOCAL_HIGH,     // 局部高点
    LOCAL_LOW,      // 局部低点
    TREND_CHANGE,   // 趋势转换
    BREAKOUT,       // 突破
    SUPPORT,        // 支撑
    RESISTANCE,     // 阻力
    VOLUME_SPIKE,   // 成交量异常
    VOLATILITY_JUMP, // 波动率跳跃
    CUSTOM          // 自定义类型
}

/**
 * 检测器配置类
 */
class DetectorConfig {
    private java.util.Map<String, Object> parameters = new java.util.HashMap<>();
    
    public DetectorConfig() {}
    
    public DetectorConfig(java.util.Map<String, Object> parameters) {
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
        return "DetectorConfig{parameters=" + parameters + '}';
    }
}