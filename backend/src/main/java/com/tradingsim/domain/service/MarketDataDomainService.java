package com.tradingsim.domain.service;

import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.domain.model.OhlcvId;
import com.tradingsim.domain.repository.OhlcvDataRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 市场数据领域服务
 * 
 * @author TradingSim Team
 */
@Service
public class MarketDataDomainService {
    
    private final OhlcvDataRepository ohlcvDataRepository;
    
    public MarketDataDomainService(OhlcvDataRepository ohlcvDataRepository) {
        this.ohlcvDataRepository = ohlcvDataRepository;
    }
    
    /**
     * 获取指定股票的历史数据段
     */
    public List<OhlcvData> getHistoricalSegment(String stockCode, Instant startTime, 
                                               Instant endTime, int maxPoints) {
        List<OhlcvData> data = ohlcvDataRepository.findByStockCodeAndTimestampBetween(
                stockCode, startTime, endTime);
        
        // 如果数据点过多，进行采样
        if (data.size() > maxPoints) {
            return sampleData(data, maxPoints);
        }
        
        return data;
    }
    
    /**
     * 获取游戏数据段（包含关键点检测）
     */
    public List<OhlcvData> getGameSegment(String stockCode, int minPoints, int maxPoints) {
        List<OhlcvData> allData = ohlcvDataRepository.findByStockCode(stockCode);
        
        if (allData.size() < minPoints) {
            throw new IllegalArgumentException("Insufficient data for stock: " + stockCode);
        }
        
        // 选择一个随机段落
        int segmentSize = Math.min(maxPoints, allData.size());
        int startIndex = (int) (Math.random() * (allData.size() - segmentSize));
        
        List<OhlcvData> segment = allData.subList(startIndex, startIndex + segmentSize);
        
        // 验证段落质量
        if (!isValidSegment(segment)) {
            // 如果段落质量不好，递归尝试获取新段落
            return getGameSegment(stockCode, minPoints, maxPoints);
        }
        
        return segment;
    }
    
    /**
     * 检测关键点（局部极值）
     */
    public List<Integer> detectKeyPoints(List<OhlcvData> data, int windowSize) {
        List<Integer> keyPoints = new java.util.ArrayList<>();
        
        for (int i = windowSize; i < data.size() - windowSize; i++) {
            if (isLocalMaximum(data, i, windowSize) || isLocalMinimum(data, i, windowSize)) {
                keyPoints.add(i);
            }
        }
        
        return keyPoints;
    }
    
    /**
     * 计算技术指标
     */
    public BigDecimal calculateSMA(List<OhlcvData> data, int period) {
        if (data.size() < period) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = data.size() - period; i < data.size(); i++) {
            sum = sum.add(data.get(i).getClosePrice());
        }
        
        return sum.divide(BigDecimal.valueOf(period), 4, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 计算波动率
     */
    public BigDecimal calculateVolatility(List<OhlcvData> data, int period) {
        if (data.size() < period + 1) {
            return BigDecimal.ZERO;
        }
        
        // 计算收益率
        List<BigDecimal> returns = new java.util.ArrayList<>();
        for (int i = data.size() - period; i < data.size(); i++) {
            if (i > 0) {
                BigDecimal prevPrice = data.get(i - 1).getClosePrice();
                BigDecimal currentPrice = data.get(i).getClosePrice();
                BigDecimal returnRate = currentPrice.subtract(prevPrice)
                        .divide(prevPrice, 6, BigDecimal.ROUND_HALF_UP);
                returns.add(returnRate);
            }
        }
        
        // 计算标准差
        BigDecimal mean = returns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 6, BigDecimal.ROUND_HALF_UP);
        
        BigDecimal variance = returns.stream()
                .map(r -> r.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 6, BigDecimal.ROUND_HALF_UP);
        
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
    }
    
    /**
     * 验证数据段质量
     */
    private boolean isValidSegment(List<OhlcvData> segment) {
        if (segment.size() < 10) {
            return false;
        }
        
        // 检查波动率
        BigDecimal volatility = calculateVolatility(segment, segment.size());
        if (volatility.compareTo(BigDecimal.valueOf(0.01)) < 0 || 
            volatility.compareTo(BigDecimal.valueOf(0.1)) > 0) {
            return false;
        }
        
        // 检查价格范围
        BigDecimal minPrice = segment.stream()
                .map(OhlcvData::getLowPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = segment.stream()
                .map(OhlcvData::getHighPrice)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal priceRange = maxPrice.subtract(minPrice).divide(minPrice, 4, BigDecimal.ROUND_HALF_UP);
        return priceRange.compareTo(BigDecimal.valueOf(0.05)) > 0; // 至少5%的价格变动
    }
    
    /**
     * 数据采样
     */
    private List<OhlcvData> sampleData(List<OhlcvData> data, int targetSize) {
        if (data.size() <= targetSize) {
            return data;
        }
        
        List<OhlcvData> sampled = new java.util.ArrayList<>();
        double step = (double) data.size() / targetSize;
        
        for (int i = 0; i < targetSize; i++) {
            int index = (int) (i * step);
            sampled.add(data.get(index));
        }
        
        return sampled;
    }
    
    /**
     * 检查是否为局部最大值
     */
    private boolean isLocalMaximum(List<OhlcvData> data, int index, int windowSize) {
        BigDecimal currentHigh = data.get(index).getHighPrice();
        
        for (int i = index - windowSize; i <= index + windowSize; i++) {
            if (i != index && i >= 0 && i < data.size()) {
                if (data.get(i).getHighPrice().compareTo(currentHigh) > 0) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * 检查是否为局部最小值
     */
    private boolean isLocalMinimum(List<OhlcvData> data, int index, int windowSize) {
        BigDecimal currentLow = data.get(index).getLowPrice();
        
        for (int i = index - windowSize; i <= index + windowSize; i++) {
            if (i != index && i >= 0 && i < data.size()) {
                if (data.get(i).getLowPrice().compareTo(currentLow) < 0) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * 获取可用的股票代码列表
     */
    public List<String> getAvailableStockCodes() {
        return ohlcvDataRepository.findAllStockCodes();
    }
    
    /**
     * 检查股票数据是否充足
     */
    public boolean hasEnoughData(String stockCode, int minDataPoints) {
        return ohlcvDataRepository.countByStockCode(stockCode) >= minDataPoints;
    }
    
    /**
     * 获取最新价格
     */
    public Optional<BigDecimal> getLatestPrice(String stockCode) {
        return ohlcvDataRepository.findLatestByStockCode(stockCode)
                .map(OhlcvData::getClosePrice);
    }
}