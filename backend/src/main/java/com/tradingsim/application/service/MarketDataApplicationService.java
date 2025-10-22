package com.tradingsim.application.service;

import com.tradingsim.application.dto.MarketDataResponse;

import java.time.Instant;
import java.util.List;

/**
 * 市场数据应用服务接口
 * 
 * @author TradingSim Team
 */
public interface MarketDataApplicationService {
    
    /**
     * 获取历史数据段
     */
    List<MarketDataResponse> getHistoricalData(String stockCode, String timeframe, 
                                              Instant startTime, Instant endTime);
    
    /**
     * 获取游戏数据段（包含关键点检测）
     */
    List<MarketDataResponse> getGameDataSegment(String stockCode, String timeframe, int segmentSize);
    
    /**
     * 获取指定数量的最新数据
     */
    List<MarketDataResponse> getLatestData(String stockCode, String timeframe, int count);
    
    /**
     * 获取可用的股票代码列表
     */
    List<String> getAvailableStockCodes();
    
    /**
     * 检查股票数据是否充足
     */
    boolean isDataSufficient(String stockCode, String timeframe, int requiredCount);
    
    /**
     * 获取股票的最新价格
     */
    MarketDataResponse getLatestPrice(String stockCode);
}