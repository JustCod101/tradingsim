package com.tradingsim.infra.persistence;

import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.domain.repository.OhlcvDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * OHLCV数据仓储实现
 * 
 * 基于Spring Data JPA实现的OhlcvData仓储
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@Repository
public class OhlcvDataRepositoryImpl implements OhlcvDataRepository {
    
    private final OhlcvDataJpaRepository jpaRepository;
    
    @Autowired
    public OhlcvDataRepositoryImpl(OhlcvDataJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public OhlcvData save(OhlcvData data) {
        return jpaRepository.save(data);
    }
    
    @Override
    public List<OhlcvData> saveAll(List<OhlcvData> dataList) {
        return jpaRepository.saveAll(dataList);
    }
    
    @Override
    public Optional<OhlcvData> findByStockCodeAndTimestamp(String stockCode, LocalDateTime timestamp) {
        return jpaRepository.findByStockCodeAndTimestamp(stockCode, timestamp);
    }
    
    @Override
    public List<OhlcvData> findByStockCodeAndTimestampBetween(String stockCode, 
                                                             LocalDateTime startTime, 
                                                             LocalDateTime endTime) {
        return jpaRepository.findByStockCodeAndTimestampBetweenOrderByTimestamp(
            stockCode, startTime, endTime);
    }
    
    @Override
    public List<OhlcvData> findLatestByStockCode(String stockCode, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return jpaRepository.findByStockCodeOrderByTimestampDesc(stockCode, pageable);
    }
    
    @Override
    public Optional<OhlcvData> findFirstByStockCodeAndTimestampBefore(String stockCode, 
                                                                     LocalDateTime timestamp) {
        return jpaRepository.findFirstByStockCodeAndTimestampBeforeOrderByTimestampDesc(
            stockCode, timestamp);
    }
    
    @Override
    public Optional<OhlcvData> findFirstByStockCodeAndTimestampAfter(String stockCode, 
                                                                    LocalDateTime timestamp) {
        return jpaRepository.findFirstByStockCodeAndTimestampAfterOrderByTimestamp(
            stockCode, timestamp);
    }
    
    @Override
    public List<String> findAllStockCodes() {
        return jpaRepository.findDistinctStockCodes();
    }
    
    @Override
    public TimeRange getDataTimeRange(String stockCode) {
        return jpaRepository.getDataTimeRange(stockCode);
    }
    
    @Override
    public long countByStockCode(String stockCode) {
        return jpaRepository.countByStockCode(stockCode);
    }
    
    @Override
    public List<LocalDateTime> findVolatileSegments(String stockCode, double minVolatility, 
                                                   double maxVolatility, int windowMinutes, 
                                                   int maxResults) {
        return jpaRepository.findVolatileSegments(stockCode, minVolatility, maxVolatility, 
                                                 windowMinutes, maxResults);
    }
    
    @Override
    public List<OhlcvData> findByStockCodeAndVolumeGreaterThan(String stockCode, long minVolume) {
        return jpaRepository.findByStockCodeAndVolumeGreaterThanOrderByTimestamp(stockCode, minVolume);
    }
    
    @Override
    public PriceStats getPriceStats(String stockCode, LocalDateTime startTime, LocalDateTime endTime) {
        return jpaRepository.getPriceStats(stockCode, startTime, endTime);
    }
    
    @Override
    public void deleteOldData(LocalDateTime beforeTime) {
        jpaRepository.deleteByTimestampBefore(beforeTime);
    }
    
    @Override
    public boolean existsByStockCodeAndTimestamp(String stockCode, LocalDateTime timestamp) {
        return jpaRepository.existsByStockCodeAndTimestamp(stockCode, timestamp);
    }
}

/**
 * Spring Data JPA Repository接口
 */
interface OhlcvDataJpaRepository extends JpaRepository<OhlcvData, OhlcvData.OhlcvId> {
    
    Optional<OhlcvData> findByStockCodeAndTimestamp(String stockCode, LocalDateTime timestamp);
    
    List<OhlcvData> findByStockCodeAndTimestampBetweenOrderByTimestamp(String stockCode, 
                                                                      LocalDateTime startTime, 
                                                                      LocalDateTime endTime);
    
    List<OhlcvData> findByStockCodeOrderByTimestampDesc(String stockCode, Pageable pageable);
    
    Optional<OhlcvData> findFirstByStockCodeAndTimestampBeforeOrderByTimestampDesc(String stockCode, 
                                                                                  LocalDateTime timestamp);
    
    Optional<OhlcvData> findFirstByStockCodeAndTimestampAfterOrderByTimestamp(String stockCode, 
                                                                             LocalDateTime timestamp);
    
    @Query("SELECT DISTINCT o.stockCode FROM OhlcvData o ORDER BY o.stockCode")
    List<String> findDistinctStockCodes();
    
    @Query("SELECT new com.tradingsim.infra.persistence.TimeRangeImpl(" +
           "MIN(o.timestamp), MAX(o.timestamp)" +
           ") FROM OhlcvData o WHERE o.stockCode = :stockCode")
    TimeRange getDataTimeRange(@Param("stockCode") String stockCode);
    
    long countByStockCode(String stockCode);
    
    @Query(value = """
        WITH volatility_windows AS (
            SELECT 
                timestamp,
                STDDEV(close_price) OVER (
                    ORDER BY timestamp 
                    ROWS BETWEEN :windowMinutes PRECEDING AND CURRENT ROW
                ) / AVG(close_price) OVER (
                    ORDER BY timestamp 
                    ROWS BETWEEN :windowMinutes PRECEDING AND CURRENT ROW
                ) as volatility
            FROM ohlcv_data 
            WHERE stock_code = :stockCode
            ORDER BY timestamp
        )
        SELECT timestamp 
        FROM volatility_windows 
        WHERE volatility BETWEEN :minVolatility AND :maxVolatility
        ORDER BY RANDOM()
        LIMIT :maxResults
        """, nativeQuery = true)
    List<LocalDateTime> findVolatileSegments(@Param("stockCode") String stockCode,
                                           @Param("minVolatility") double minVolatility,
                                           @Param("maxVolatility") double maxVolatility,
                                           @Param("windowMinutes") int windowMinutes,
                                           @Param("maxResults") int maxResults);
    
    List<OhlcvData> findByStockCodeAndVolumeGreaterThanOrderByTimestamp(String stockCode, long minVolume);
    
    @Query("SELECT new com.tradingsim.infra.persistence.PriceStatsImpl(" +
           "MIN(o.low), MAX(o.high), AVG(o.close), " +
           "STDDEV(o.close), AVG(o.volume)" +
           ") FROM OhlcvData o WHERE o.stockCode = :stockCode " +
           "AND o.timestamp BETWEEN :startTime AND :endTime")
    PriceStats getPriceStats(@Param("stockCode") String stockCode,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime);
    
    @Modifying
    @Query("DELETE FROM OhlcvData o WHERE o.timestamp < :beforeTime")
    void deleteByTimestampBefore(@Param("beforeTime") LocalDateTime beforeTime);
    
    boolean existsByStockCodeAndTimestamp(String stockCode, LocalDateTime timestamp);
}

/**
 * TimeRange实现类
 */
class TimeRangeImpl implements OhlcvDataRepository.TimeRange {
    
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    
    public TimeRangeImpl(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }
}

/**
 * PriceStats实现类
 */
class PriceStatsImpl implements OhlcvDataRepository.PriceStats {
    
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final BigDecimal avgPrice;
    private final double volatility;
    private final double avgVolume;
    
    public PriceStatsImpl(BigDecimal minPrice, BigDecimal maxPrice, BigDecimal avgPrice,
                         double volatility, double avgVolume) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.avgPrice = avgPrice;
        this.volatility = volatility;
        this.avgVolume = avgVolume;
    }
    
    @Override
    public BigDecimal getMinPrice() {
        return minPrice;
    }
    
    @Override
    public BigDecimal getMaxPrice() {
        return maxPrice;
    }
    
    @Override
    public BigDecimal getAvgPrice() {
        return avgPrice;
    }
    
    @Override
    public double getVolatility() {
        return volatility;
    }
    
    @Override
    public double getAvgVolume() {
        return avgVolume;
    }
}