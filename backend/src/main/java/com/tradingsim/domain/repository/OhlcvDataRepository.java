package com.tradingsim.domain.repository;

import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.domain.model.OhlcvId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * OHLCV数据仓储接口
 * 
 * @author TradingSim Team
 */
public interface OhlcvDataRepository {
    
    /**
     * 保存OHLCV数据
     */
    OhlcvData save(OhlcvData ohlcvData);
    
    /**
     * 根据ID查找OHLCV数据
     */
    Optional<OhlcvData> findById(OhlcvId id);
    
    /**
     * 根据股票代码查找OHLCV数据
     */
    List<OhlcvData> findByStockCode(String stockCode);
    
    /**
     * 根据股票代码和时间范围查找OHLCV数据
     */
    List<OhlcvData> findByStockCodeAndTimestampBetween(
            String stockCode, Instant startTime, Instant endTime);
    
    /**
     * 根据股票代码查找最新的OHLCV数据
     */
    Optional<OhlcvData> findLatestByStockCode(String stockCode);
    
    /**
     * 根据股票代码查找指定数量的最新OHLCV数据
     */
    List<OhlcvData> findLatestByStockCode(String stockCode, int limit);
    
    /**
     * 根据股票代码和时间戳查找之前的N条数据
     */
    List<OhlcvData> findPreviousData(String stockCode, Instant timestamp, int limit);
    
    /**
     * 根据股票代码和时间戳查找之后的N条数据
     */
    List<OhlcvData> findNextData(String stockCode, Instant timestamp, int limit);
    
    /**
     * 检查指定股票代码的数据是否存在
     */
    boolean existsByStockCode(String stockCode);
    
    /**
     * 统计指定股票代码的数据数量
     */
    long countByStockCode(String stockCode);
    
    /**
     * 获取所有可用的股票代码
     */
    List<String> findAllStockCodes();
    
    /**
     * 批量保存OHLCV数据
     */
    List<OhlcvData> saveAll(List<OhlcvData> ohlcvDataList);
    
    /**
     * 删除指定时间之前的数据
     */
    void deleteByTimestampBefore(Instant timestamp);
    
    /**
     * 根据股票代码删除数据
     */
    void deleteByStockCode(String stockCode);
}