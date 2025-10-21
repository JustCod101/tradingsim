package com.tradingsim.app.service;

import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.domain.repository.OhlcvDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 市场数据应用服务
 * 
 * 负责市场数据的业务逻辑:
 * - OHLCV数据查询和管理
 * - 数据段生成和验证
 * - 缓存优化
 * - 数据统计分析
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class MarketDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(MarketDataService.class);
    
    private final OhlcvDataRepository ohlcvRepository;
    
    @Autowired
    public MarketDataService(OhlcvDataRepository ohlcvRepository) {
        this.ohlcvRepository = ohlcvRepository;
    }
    
    /**
     * 获取股票在指定时间范围的数据
     */
    @Cacheable(value = "ohlcvData", key = "#stockCode + ':' + #startTime + ':' + #endTime")
    public List<OhlcvData> getOhlcvData(String stockCode, LocalDateTime startTime, LocalDateTime endTime) {
        logger.debug("Fetching OHLCV data for {} from {} to {}", stockCode, startTime, endTime);
        
        List<OhlcvData> data = ohlcvRepository.findByStockCodeAndTimestampBetween(
            stockCode, startTime, endTime);
        
        logger.debug("Found {} data points for {}", data.size(), stockCode);
        return data;
    }
    
    /**
     * 获取股票的最新数据
     */
    @Cacheable(value = "latestData", key = "#stockCode + ':' + #limit")
    public List<OhlcvData> getLatestData(String stockCode, int limit) {
        return ohlcvRepository.findLatestByStockCode(stockCode, limit);
    }
    
    /**
     * 获取指定时间点的数据
     */
    public Optional<OhlcvData> getDataAtTime(String stockCode, LocalDateTime timestamp) {
        return ohlcvRepository.findByStockCodeAndTimestamp(stockCode, timestamp);
    }
    
    /**
     * 获取所有可用的股票代码
     */
    @Cacheable(value = "stockCodes")
    public List<String> getAvailableStockCodes() {
        return ohlcvRepository.findAllStockCodes();
    }
    
    /**
     * 获取股票的数据时间范围
     */
    @Cacheable(value = "timeRange", key = "#stockCode")
    public OhlcvDataRepository.TimeRange getDataTimeRange(String stockCode) {
        return ohlcvRepository.getDataTimeRange(stockCode);
    }
    
    /**
     * 生成随机数据段
     */
    public GameSegment generateRandomSegment(String stockCode, int durationMinutes, 
                                           double minVolatility, double maxVolatility) {
        logger.info("Generating random segment for {} with duration {} minutes", 
                   stockCode, durationMinutes);
        
        // 获取数据时间范围
        OhlcvDataRepository.TimeRange timeRange = getDataTimeRange(stockCode);
        if (timeRange == null) {
            throw new IllegalArgumentException("No data available for stock: " + stockCode);
        }
        
        // 查找满足波动率条件的数据段
        List<LocalDateTime> volatileSegments = ohlcvRepository.findVolatileSegments(
            stockCode, minVolatility, maxVolatility, durationMinutes, 100);
        
        if (volatileSegments.isEmpty()) {
            throw new IllegalArgumentException("No segments found matching volatility criteria");
        }
        
        // 随机选择一个段
        LocalDateTime startTime = volatileSegments.get(
            ThreadLocalRandom.current().nextInt(volatileSegments.size()));
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
        
        // 获取数据并验证
        List<OhlcvData> segmentData = getOhlcvData(stockCode, startTime, endTime);
        if (segmentData.size() < durationMinutes * 0.8) { // 至少80%的数据完整性
            throw new IllegalArgumentException("Insufficient data for the selected segment");
        }
        
        // 计算段统计信息
        SegmentStats stats = calculateSegmentStats(segmentData);
        
        String segmentId = generateSegmentId(stockCode, startTime);
        
        logger.info("Generated segment {} with {} data points, volatility: {:.4f}", 
                   segmentId, segmentData.size(), stats.getVolatility());
        
        return new GameSegment(segmentId, stockCode, startTime, endTime, 
                              segmentData.size(), stats);
    }
    
    /**
     * 验证数据段的质量
     */
    public SegmentValidation validateSegment(String stockCode, LocalDateTime startTime, 
                                           LocalDateTime endTime) {
        List<OhlcvData> data = getOhlcvData(stockCode, startTime, endTime);
        
        if (data.isEmpty()) {
            return new SegmentValidation(false, "No data available for the specified time range");
        }
        
        // 检查数据完整性
        long expectedDataPoints = java.time.Duration.between(startTime, endTime).toMinutes();
        double completeness = (double) data.size() / expectedDataPoints;
        
        if (completeness < 0.8) {
            return new SegmentValidation(false, 
                String.format("Data completeness too low: %.2f%% (expected >= 80%%)", 
                             completeness * 100));
        }
        
        // 检查数据质量
        SegmentStats stats = calculateSegmentStats(data);
        
        if (stats.getVolatility() < 0.001) {
            return new SegmentValidation(false, "Volatility too low for meaningful trading");
        }
        
        if (stats.getAverageVolume() < 1000) {
            return new SegmentValidation(false, "Average volume too low");
        }
        
        return new SegmentValidation(true, "Segment validation passed", stats);
    }
    
    /**
     * 获取价格统计信息
     */
    @Cacheable(value = "priceStats", key = "#stockCode + ':' + #startTime + ':' + #endTime")
    public OhlcvDataRepository.PriceStats getPriceStats(String stockCode, LocalDateTime startTime, 
                                                       LocalDateTime endTime) {
        return ohlcvRepository.getPriceStats(stockCode, startTime, endTime);
    }
    
    /**
     * 计算段统计信息
     */
    private SegmentStats calculateSegmentStats(List<OhlcvData> data) {
        if (data.isEmpty()) {
            return new SegmentStats(0.0, 0.0, 0.0, 0L, 0);
        }
        
        // 计算价格统计
        double minPrice = data.stream()
            .mapToDouble(d -> d.getLow().doubleValue())
            .min().orElse(0.0);
        
        double maxPrice = data.stream()
            .mapToDouble(d -> d.getHigh().doubleValue())
            .max().orElse(0.0);
        
        double avgPrice = data.stream()
            .mapToDouble(d -> d.getClose().doubleValue())
            .average().orElse(0.0);
        
        // 计算波动率
        double volatility = calculateVolatility(data);
        
        // 计算平均成交量
        long avgVolume = (long) data.stream()
            .mapToLong(OhlcvData::getVolume)
            .average().orElse(0.0);
        
        return new SegmentStats(minPrice, maxPrice, avgPrice, volatility, avgVolume, data.size());
    }
    
    /**
     * 计算波动率
     */
    private double calculateVolatility(List<OhlcvData> data) {
        if (data.size() < 2) return 0.0;
        
        // 计算收益率
        double[] returns = new double[data.size() - 1];
        for (int i = 1; i < data.size(); i++) {
            double prevPrice = data.get(i - 1).getClose().doubleValue();
            double currPrice = data.get(i).getClose().doubleValue();
            returns[i - 1] = (currPrice - prevPrice) / prevPrice;
        }
        
        // 计算标准差
        double mean = java.util.Arrays.stream(returns).average().orElse(0.0);
        double variance = java.util.Arrays.stream(returns)
            .map(r -> Math.pow(r - mean, 2))
            .average().orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    /**
     * 生成段ID
     */
    private String generateSegmentId(String stockCode, LocalDateTime startTime) {
        return String.format("segment_%s_%d", stockCode, 
                           startTime.toEpochSecond(java.time.ZoneOffset.UTC));
    }
    
    /**
     * 游戏段信息
     */
    public static class GameSegment {
        private final String id;
        private final String stockCode;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final int dataPoints;
        private final SegmentStats stats;
        
        public GameSegment(String id, String stockCode, LocalDateTime startTime, 
                          LocalDateTime endTime, int dataPoints, SegmentStats stats) {
            this.id = id;
            this.stockCode = stockCode;
            this.startTime = startTime;
            this.endTime = endTime;
            this.dataPoints = dataPoints;
            this.stats = stats;
        }
        
        // Getters
        public String getId() { return id; }
        public String getStockCode() { return stockCode; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public int getDataPoints() { return dataPoints; }
        public SegmentStats getStats() { return stats; }
    }
    
    /**
     * 段统计信息
     */
    public static class SegmentStats {
        private final double minPrice;
        private final double maxPrice;
        private final double avgPrice;
        private final double volatility;
        private final long averageVolume;
        private final int dataPoints;
        
        public SegmentStats(double minPrice, double maxPrice, double avgPrice, 
                           double volatility, long averageVolume, int dataPoints) {
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.avgPrice = avgPrice;
            this.volatility = volatility;
            this.averageVolume = averageVolume;
            this.dataPoints = dataPoints;
        }
        
        // Getters
        public double getMinPrice() { return minPrice; }
        public double getMaxPrice() { return maxPrice; }
        public double getAvgPrice() { return avgPrice; }
        public double getVolatility() { return volatility; }
        public long getAverageVolume() { return averageVolume; }
        public int getDataPoints() { return dataPoints; }
    }
    
    /**
     * 段验证结果
     */
    public static class SegmentValidation {
        private final boolean valid;
        private final String message;
        private final SegmentStats stats;
        
        public SegmentValidation(boolean valid, String message) {
            this(valid, message, null);
        }
        
        public SegmentValidation(boolean valid, String message, SegmentStats stats) {
            this.valid = valid;
            this.message = message;
            this.stats = stats;
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public SegmentStats getStats() { return stats; }
    }
}