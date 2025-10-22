package com.tradingsim.api.controller;

import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.domain.repository.OhlcvDataRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 市场数据控制器
 * 处理OHLCV数据的REST API
 * 
 * @author TradingSim Team
 */
@Tag(name = "市场数据", description = "股票市场OHLCV数据相关API")
@RestController
@RequestMapping("/market-data")
@CrossOrigin(origins = "*")
public class MarketDataController {
    
    @Autowired
    private OhlcvDataRepository ohlcvDataRepository;
    
    /**
     * 获取指定股票的OHLCV数据
     */
    @Operation(summary = "获取股票OHLCV数据", description = "根据股票代码获取OHLCV数据，支持时间范围和数量限制")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取数据"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    @GetMapping("/{stockCode}")
    public ResponseEntity<List<OhlcvData>> getOhlcvData(
            @Parameter(description = "股票代码", example = "AAPL") @PathVariable String stockCode,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,
            @Parameter(description = "数据条数限制", example = "100") @RequestParam(defaultValue = "100") int limit) {
        
        List<OhlcvData> data;
        
        if (startTime != null && endTime != null) {
            data = ohlcvDataRepository.findByStockCodeAndTimestampBetween(stockCode, startTime, endTime);
        } else {
            data = ohlcvDataRepository.findLatestByStockCode(stockCode, limit);
        }
        
        return ResponseEntity.ok(data);
    }
    
    /**
     * 获取最新的OHLCV数据
     */
    @GetMapping("/{stockCode}/latest")
    public ResponseEntity<OhlcvData> getLatestOhlcvData(@PathVariable String stockCode) {
        Optional<OhlcvData> data = ohlcvDataRepository.findLatestByStockCode(stockCode);
        return data.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取指定时间之前的数据
     */
    @GetMapping("/{stockCode}/before")
    public ResponseEntity<List<OhlcvData>> getPreviousData(
            @PathVariable String stockCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant timestamp,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<OhlcvData> data = ohlcvDataRepository.findPreviousData(stockCode, timestamp, limit);
        return ResponseEntity.ok(data);
    }
    
    /**
     * 获取指定时间之后的数据
     */
    @GetMapping("/{stockCode}/after")
    public ResponseEntity<List<OhlcvData>> getNextData(
            @PathVariable String stockCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant timestamp,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<OhlcvData> data = ohlcvDataRepository.findNextData(stockCode, timestamp, limit);
        return ResponseEntity.ok(data);
    }
    
    /**
     * 检查数据是否存在
     */
    @GetMapping("/{stockCode}/exists")
    public ResponseEntity<Boolean> checkDataExists(@PathVariable String stockCode) {
        boolean exists = ohlcvDataRepository.existsByStockCode(stockCode);
        return ResponseEntity.ok(exists);
    }
    
    /**
     * 获取数据统计信息
     */
    @GetMapping("/{stockCode}/stats")
    public ResponseEntity<DataStats> getDataStats(@PathVariable String stockCode) {
        long count = ohlcvDataRepository.countByStockCode(stockCode);
        Optional<OhlcvData> latest = ohlcvDataRepository.findLatestByStockCode(stockCode);
        
        DataStats stats = new DataStats();
        stats.setStockCode(stockCode);
        stats.setTotalRecords(count);
        stats.setLatestTimestamp(latest.map(data -> data.getId().getTimestamp()).orElse(null));
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 获取所有可用的股票代码
     */
    @GetMapping("/stocks")
    public ResponseEntity<List<String>> getAllStockCodes() {
        List<String> stockCodes = ohlcvDataRepository.findAllStockCodes();
        return ResponseEntity.ok(stockCodes);
    }
    
    /**
     * 批量保存OHLCV数据
     */
    @PostMapping("/batch")
    public ResponseEntity<String> saveBatchData(@RequestBody List<OhlcvData> dataList) {
        try {
            ohlcvDataRepository.saveAll(dataList);
            return ResponseEntity.ok("成功保存 " + dataList.size() + " 条数据");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("保存失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除指定时间之前的数据
     */
    @DeleteMapping("/{stockCode}/before")
    public ResponseEntity<String> deleteDataBefore(
            @PathVariable String stockCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant timestamp) {
        
        try {
            ohlcvDataRepository.deleteByTimestampBefore(timestamp);
            return ResponseEntity.ok("成功删除指定时间之前的数据");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除指定股票的所有数据
     */
    @DeleteMapping("/{stockCode}")
    public ResponseEntity<String> deleteStockData(@PathVariable String stockCode) {
        try {
            ohlcvDataRepository.deleteByStockCode(stockCode);
            return ResponseEntity.ok("成功删除股票 " + stockCode + " 的所有数据");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 数据统计信息DTO
     */
    public static class DataStats {
        private String stockCode;
        private long totalRecords;
        private Instant latestTimestamp;
        
        // Getters and Setters
        public String getStockCode() {
            return stockCode;
        }
        
        public void setStockCode(String stockCode) {
            this.stockCode = stockCode;
        }
        
        public long getTotalRecords() {
            return totalRecords;
        }
        
        public void setTotalRecords(long totalRecords) {
            this.totalRecords = totalRecords;
        }
        
        public Instant getLatestTimestamp() {
            return latestTimestamp;
        }
        
        public void setLatestTimestamp(Instant latestTimestamp) {
            this.latestTimestamp = latestTimestamp;
        }
    }
}