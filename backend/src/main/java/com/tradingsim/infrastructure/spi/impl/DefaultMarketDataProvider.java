package com.tradingsim.infrastructure.spi.impl;

import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.domain.repository.OhlcvDataRepository;
import com.tradingsim.infrastructure.spi.MarketDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认市场数据提供者实现
 * 从数据库获取市场数据
 */
@Component
public class DefaultMarketDataProvider implements MarketDataProvider {

    @Autowired
    private OhlcvDataRepository ohlcvDataRepository;

    @Override
    public String getProviderName() {
        return "DefaultMarketDataProvider";
    }

    @Override
    public List<String> getSupportedStockCodes() {
        // 由于当前仓储接口没有此方法，返回一些常见股票代码
        return Arrays.asList("AAPL", "GOOGL", "MSFT", "TSLA", "AMZN");
    }

    @Override
    public List<OhlcvData> getOhlcvData(String stockCode, LocalDateTime startTime, LocalDateTime endTime) {
        Instant startInstant = startTime.toInstant(ZoneOffset.UTC);
        Instant endInstant = endTime.toInstant(ZoneOffset.UTC);
        return ohlcvDataRepository.findByStockCodeAndTimestampBetween(stockCode, startInstant, endInstant);
    }

    @Override
    public OhlcvData getLatestOhlcvData(String stockCode) {
        return ohlcvDataRepository.findLatestByStockCode(stockCode).orElse(null);
    }

    @Override
    public boolean isDataAvailable(String stockCode, LocalDateTime timestamp) {
        return ohlcvDataRepository.existsByStockCode(stockCode);
    }

    @Override
    public int getPriority() {
        return 100; // 默认优先级
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}