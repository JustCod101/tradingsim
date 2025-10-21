package com.tradingsim.domain.repository;

import com.tradingsim.domain.model.OhlcvData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * OHLCV数据仓储接口
 * 
 * 定义时序数据的访问操作，专门用于TimescaleDB:
 * - 支持时间范围查询
 * - 提供数据聚合功能
 * - 支持股票代码过滤
 * - 包含数据统计分析
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
public interface OhlcvDataRepository {
    
    /**
     * 保存OHLCV数据
     * 
     * @param ohlcvData OHLCV数据
     * @return 保存后的数据
     */
    OhlcvData save(OhlcvData ohlcvData);
    
    /**
     * 批量保存OHLCV数据
     * 
     * @param ohlcvDataList OHLCV数据列表
     * @return 保存后的数据列表
     */
    List<OhlcvData> saveAll(List<OhlcvData> ohlcvDataList);
    
    /**
     * 根据股票代码和时间戳查找数据
     * 
     * @param stockCode 股票代码
     * @param timestamp 时间戳
     * @return OHLCV数据，如果不存在则返回空
     */
    Optional<OhlcvData> findByStockCodeAndTimestamp(String stockCode, LocalDateTime timestamp);
    
    /**
     * 查找指定股票在时间范围内的数据
     * 
     * @param stockCode 股票代码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 按时间升序排列的OHLCV数据列表
     */
    List<OhlcvData> findByStockCodeAndTimestampBetween(String stockCode, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查找指定股票的最新N条数据
     * 
     * @param stockCode 股票代码
     * @param limit 数据条数
     * @return 按时间降序排列的OHLCV数据列表
     */
    List<OhlcvData> findLatestByStockCode(String stockCode, int limit);
    
    /**
     * 查找指定股票在指定时间之后的数据
     * 
     * @param stockCode 股票代码
     * @param afterTime 起始时间
     * @param limit 数据条数限制
     * @return 按时间升序排列的OHLCV数据列表
     */
    List<OhlcvData> findByStockCodeAndTimestampAfter(String stockCode, LocalDateTime afterTime, int limit);
    
    /**
     * 查找指定股票在指定时间之前的数据
     * 
     * @param stockCode 股票代码
     * @param beforeTime 截止时间
     * @param limit 数据条数限制
     * @return 按时间降序排列的OHLCV数据列表
     */
    List<OhlcvData> findByStockCodeAndTimestampBefore(String stockCode, LocalDateTime beforeTime, int limit);
    
    /**
     * 获取所有可用的股票代码
     * 
     * @return 股票代码列表
     */
    List<String> findAllStockCodes();
    
    /**
     * 获取指定股票的数据时间范围
     * 
     * @param stockCode 股票代码
     * @return 时间范围 [最早时间, 最晚时间]
     */
    TimeRange getDataTimeRange(String stockCode);
    
    /**
     * 统计指定股票的数据条数
     * 
     * @param stockCode 股票代码
     * @return 数据条数
     */
    long countByStockCode(String stockCode);
    
    /**
     * 统计指定时间范围内的数据条数
     * 
     * @param stockCode 股票代码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 数据条数
     */
    long countByStockCodeAndTimestampBetween(String stockCode, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查找满足波动率条件的数据段
     * 
     * @param stockCode 股票代码
     * @param minVolatility 最小波动率
     * @param maxVolatility 最大波动率
     * @param windowSize 计算窗口大小
     * @param limit 返回数量限制
     * @return 符合条件的数据段起始时间列表
     */
    List<LocalDateTime> findVolatileSegments(String stockCode, double minVolatility, double maxVolatility, 
                                           int windowSize, int limit);
    
    /**
     * 查找满足成交量条件的数据
     * 
     * @param stockCode 股票代码
     * @param minVolume 最小成交量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 符合条件的OHLCV数据列表
     */
    List<OhlcvData> findByVolumeGreaterThan(String stockCode, long minVolume, 
                                          LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取指定股票的价格统计信息
     * 
     * @param stockCode 股票代码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 价格统计信息
     */
    PriceStats getPriceStats(String stockCode, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 删除过期数据
     * 
     * @param beforeTime 删除此时间之前的数据
     * @return 删除的数据条数
     */
    int deleteDataBefore(LocalDateTime beforeTime);
    
    /**
     * 检查数据是否存在
     * 
     * @param stockCode 股票代码
     * @param timestamp 时间戳
     * @return 是否存在
     */
    boolean existsByStockCodeAndTimestamp(String stockCode, LocalDateTime timestamp);
    
    /**
     * 时间范围
     */
    interface TimeRange {
        LocalDateTime getStartTime();
        LocalDateTime getEndTime();
    }
    
    /**
     * 价格统计信息
     */
    interface PriceStats {
        double getMinPrice();
        double getMaxPrice();
        double getAvgPrice();
        double getVolatility();
        long getTotalVolume();
        long getDataPoints();
    }
}