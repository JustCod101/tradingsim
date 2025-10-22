package com.tradingsim.application.service.impl;

import com.tradingsim.application.dto.MarketDataResponse;
import com.tradingsim.application.service.MarketDataApplicationService;
import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.domain.service.MarketDataDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 市场数据应用服务实现
 * 
 * @author TradingSim Team
 */
@Service
public class MarketDataApplicationServiceImpl implements MarketDataApplicationService {
    
    private final MarketDataDomainService marketDataDomainService;
    
    @Autowired
    public MarketDataApplicationServiceImpl(MarketDataDomainService marketDataDomainService) {
        this.marketDataDomainService = marketDataDomainService;
    }
    
    @Override
    public List<MarketDataResponse> getHistoricalData(String stockCode, String timeframe, 
                                                     Instant startTime, Instant endTime) {
        List<OhlcvData> data = marketDataDomainService.getHistoricalSegment(stockCode, startTime, endTime, 1000);
        return data.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MarketDataResponse> getGameDataSegment(String stockCode, String timeframe, int segmentSize) {
        List<OhlcvData> data = marketDataDomainService.getGameSegment(stockCode, segmentSize, segmentSize);
        return data.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MarketDataResponse> getLatestData(String stockCode, String timeframe, int count) {
        // 获取最新的count条数据
        Instant endTime = Instant.now();
        Instant startTime = endTime.minusSeconds(count * 60 * 60); // 假设每小时一条数据
        List<OhlcvData> data = marketDataDomainService.getHistoricalSegment(stockCode, startTime, endTime, count);
        return data.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<String> getAvailableStockCodes() {
        return marketDataDomainService.getAvailableStockCodes();
    }
    
    @Override
    public boolean isDataSufficient(String stockCode, String timeframe, int requiredCount) {
        return marketDataDomainService.hasEnoughData(stockCode, requiredCount);
    }
    
    @Override
    public MarketDataResponse getLatestPrice(String stockCode) {
        // 获取最新价格并转换为响应对象
        return marketDataDomainService.getLatestPrice(stockCode)
                .map(price -> {
                    MarketDataResponse response = new MarketDataResponse();
                    response.setStockCode(stockCode);
                    response.setTimestamp(Instant.now());
                    response.setClose(price);
                    return response;
                })
                .orElse(null);
    }
    
    /**
     * 将OhlcvData转换为MarketDataResponse
     */
    private MarketDataResponse convertToResponse(OhlcvData data) {
        if (data == null) {
            return null;
        }
        
        MarketDataResponse response = new MarketDataResponse();
        response.setStockCode(data.getId().getCode());
        response.setTimestamp(data.getId().getTimestamp());
        response.setOpen(data.getOpenPrice());
        response.setHigh(data.getHighPrice());
        response.setLow(data.getLowPrice());
        response.setClose(data.getClosePrice());
        response.setVolume(data.getVolume());
        
        return response;
    }
}