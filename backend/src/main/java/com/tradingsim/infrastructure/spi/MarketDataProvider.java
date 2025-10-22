package com.tradingsim.infrastructure.spi;

import com.tradingsim.domain.model.OhlcvData;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 市场数据提供者SPI接口
 * 允许插件化的市场数据源实现
 */
public interface MarketDataProvider {

    /**
     * 获取提供者名称
     */
    String getProviderName();

    /**
     * 获取支持的股票代码列表
     */
    List<String> getSupportedStockCodes();

    /**
     * 获取指定股票的OHLCV数据
     * 
     * @param stockCode 股票代码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return OHLCV数据列表
     */
    List<OhlcvData> getOhlcvData(String stockCode, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取最新的OHLCV数据
     * 
     * @param stockCode 股票代码
     * @return 最新的OHLCV数据
     */
    OhlcvData getLatestOhlcvData(String stockCode);

    /**
     * 检查数据是否可用
     * 
     * @param stockCode 股票代码
     * @param timestamp 时间戳
     * @return 是否可用
     */
    boolean isDataAvailable(String stockCode, LocalDateTime timestamp);

    /**
     * 获取提供者优先级（数字越小优先级越高）
     */
    int getPriority();

    /**
     * 检查提供者是否启用
     */
    boolean isEnabled();
}