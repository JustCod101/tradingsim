package com.tradingsim.infrastructure.persistence;

import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.domain.model.OhlcvId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * OHLCV数据JPA仓储接口
 * 
 * @author TradingSim Team
 */
interface OhlcvDataJpaRepository extends JpaRepository<OhlcvData, OhlcvId> {
    
    @Query("SELECT o FROM OhlcvData o WHERE o.id.code = :stockCode ORDER BY o.id.timestamp ASC")
    List<OhlcvData> findByStockCode(@Param("stockCode") String stockCode);
    
    @Query("SELECT o FROM OhlcvData o WHERE o.id.code = :stockCode AND o.id.timestamp BETWEEN :startTime AND :endTime ORDER BY o.id.timestamp ASC")
    List<OhlcvData> findByStockCodeAndTimestampBetween(@Param("stockCode") String stockCode,
                                                       @Param("startTime") Instant startTime,
                                                       @Param("endTime") Instant endTime);
    
    @Query(value = "SELECT * FROM ohlcv_1m o WHERE o.code = :stockCode ORDER BY o.ts DESC LIMIT 1", nativeQuery = true)
    Optional<OhlcvData> findLatestByStockCode(@Param("stockCode") String stockCode);
    
    @Query(value = "SELECT * FROM ohlcv_1m o WHERE o.code = :stockCode ORDER BY o.ts DESC LIMIT :limit", nativeQuery = true)
    List<OhlcvData> findLatestByStockCodeWithLimit(@Param("stockCode") String stockCode, @Param("limit") int limit);
    
    @Query(value = "SELECT * FROM ohlcv_1m o WHERE o.code = :stockCode AND o.ts < :timestamp ORDER BY o.ts DESC LIMIT :limit", nativeQuery = true)
    List<OhlcvData> findPreviousData(@Param("stockCode") String stockCode, 
                                    @Param("timestamp") Instant timestamp, 
                                    @Param("limit") int limit);
    
    @Query(value = "SELECT * FROM ohlcv_1m o WHERE o.code = :stockCode AND o.ts > :timestamp ORDER BY o.ts ASC LIMIT :limit", nativeQuery = true)
    List<OhlcvData> findNextData(@Param("stockCode") String stockCode, 
                                @Param("timestamp") Instant timestamp, 
                                @Param("limit") int limit);
    
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM OhlcvData o WHERE o.id.code = :stockCode")
    boolean existsByStockCode(@Param("stockCode") String stockCode);
    
    @Query("SELECT COUNT(o) FROM OhlcvData o WHERE o.id.code = :stockCode")
    long countByStockCode(@Param("stockCode") String stockCode);
    
    @Query("SELECT DISTINCT o.id.code FROM OhlcvData o ORDER BY o.id.code")
    List<String> findAllStockCodes();
    
    @Query("DELETE FROM OhlcvData o WHERE o.id.timestamp < :timestamp")
    void deleteByTimestampBefore(@Param("timestamp") Instant timestamp);
    
    @Query("DELETE FROM OhlcvData o WHERE o.id.code = :stockCode")
    void deleteByStockCode(@Param("stockCode") String stockCode);
}

/**
 * OHLCV数据仓储实现类
 * 
 * @author TradingSim Team
 */
@org.springframework.stereotype.Repository
public class OhlcvDataRepositoryImpl implements com.tradingsim.domain.repository.OhlcvDataRepository {
    
    private final OhlcvDataJpaRepository jpaRepository;
    
    public OhlcvDataRepositoryImpl(OhlcvDataJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public OhlcvData save(OhlcvData ohlcvData) {
        return jpaRepository.save(ohlcvData);
    }
    
    @Override
    public List<OhlcvData> saveAll(List<OhlcvData> ohlcvDataList) {
        return jpaRepository.saveAll(ohlcvDataList);
    }
    
    @Override
    public Optional<OhlcvData> findById(OhlcvId id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public List<OhlcvData> findByStockCode(String stockCode) {
        return jpaRepository.findByStockCode(stockCode);
    }
    
    @Override
    public List<OhlcvData> findByStockCodeAndTimestampBetween(String stockCode, Instant startTime, Instant endTime) {
        return jpaRepository.findByStockCodeAndTimestampBetween(stockCode, startTime, endTime);
    }
    
    @Override
    public Optional<OhlcvData> findLatestByStockCode(String stockCode) {
        return jpaRepository.findLatestByStockCode(stockCode);
    }
    
    @Override
    public List<OhlcvData> findLatestByStockCode(String stockCode, int limit) {
        return jpaRepository.findLatestByStockCodeWithLimit(stockCode, limit);
    }
    
    @Override
    public List<OhlcvData> findPreviousData(String stockCode, Instant timestamp, int limit) {
        return jpaRepository.findPreviousData(stockCode, timestamp, limit);
    }
    
    @Override
    public List<OhlcvData> findNextData(String stockCode, Instant timestamp, int limit) {
        return jpaRepository.findNextData(stockCode, timestamp, limit);
    }
    
    @Override
    public boolean existsByStockCode(String stockCode) {
        return jpaRepository.existsByStockCode(stockCode);
    }
    
    @Override
    public long countByStockCode(String stockCode) {
        return jpaRepository.countByStockCode(stockCode);
    }
    
    @Override
    public List<String> findAllStockCodes() {
        return jpaRepository.findAllStockCodes();
    }
    
    @Override
    public void deleteByTimestampBefore(Instant timestamp) {
        jpaRepository.deleteByTimestampBefore(timestamp);
    }
    
    @Override
    public void deleteByStockCode(String stockCode) {
        jpaRepository.deleteByStockCode(stockCode);
    }
}