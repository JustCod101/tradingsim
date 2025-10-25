package com.tradingsim.api.controller;

import com.tradingsim.application.service.DataImportService;
import com.tradingsim.application.service.DataImportService.ImportResult;
import com.tradingsim.application.service.DataImportService.DataIntegrityReport;
import com.tradingsim.infrastructure.spi.MarketDataProvider;
import com.tradingsim.infrastructure.spi.SpiManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据导入控制器
 * 提供股票数据导入相关的REST API
 * 
 * @author TradingSim Team
 */
@Tag(name = "数据导入", description = "股票数据导入相关API")
@RestController
@RequestMapping("/data-import")
@CrossOrigin(origins = "*")
public class DataImportController {
    
    private static final Logger logger = LoggerFactory.getLogger(DataImportController.class);
    
    @Autowired
    private DataImportService dataImportService;
    
    @Autowired
    private SpiManager spiManager;
    
    /**
     * 导入单个股票的历史数据
     */
    @Operation(summary = "导入历史数据", description = "导入指定股票在指定时间范围内的历史数据")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "导入成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "导入失败")
    })
    @PostMapping("/historical/{stockCode}")
    public ResponseEntity<Map<String, Object>> importHistoricalData(
            @Parameter(description = "股票代码", example = "AAPL") @PathVariable String stockCode,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        try {
            logger.info("开始导入股票 {} 的历史数据", stockCode);
            
            int importedCount = dataImportService.importHistoricalData(stockCode, startTime, endTime);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "数据导入成功",
                "stockCode", stockCode,
                "importedRecords", importedCount,
                "startTime", startTime,
                "endTime", endTime
            ));
            
        } catch (Exception e) {
            logger.error("导入历史数据失败", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "数据导入失败: " + e.getMessage(),
                "stockCode", stockCode
            ));
        }
    }
    
    /**
     * 批量导入多个股票的历史数据
     */
    @Operation(summary = "批量导入历史数据", description = "批量导入多个股票在指定时间范围内的历史数据")
    @PostMapping("/historical/batch")
    public ResponseEntity<ImportResult> batchImportHistoricalData(
            @Parameter(description = "股票代码列表") @RequestParam List<String> stockCodes,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        try {
            logger.info("开始批量导入 {} 只股票的历史数据", stockCodes.size());
            
            ImportResult result = dataImportService.batchImportHistoricalData(stockCodes, startTime, endTime);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("批量导入历史数据失败", e);
            ImportResult errorResult = new ImportResult();
            errorResult.setTotalStocks(stockCodes.size());
            errorResult.setFailedStocks(stockCodes.size());
            errorResult.addError("批量导入失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }
    
    /**
     * 导入单个股票的最新数据
     */
    @Operation(summary = "导入最新数据", description = "导入指定股票的最新数据")
    @PostMapping("/latest/{stockCode}")
    public ResponseEntity<Map<String, Object>> importLatestData(
            @Parameter(description = "股票代码", example = "AAPL") @PathVariable String stockCode) {
        
        try {
            logger.info("开始导入股票 {} 的最新数据", stockCode);
            
            boolean success = dataImportService.importLatestData(stockCode);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "最新数据导入成功",
                    "stockCode", stockCode
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "无法获取最新数据",
                    "stockCode", stockCode
                ));
            }
            
        } catch (Exception e) {
            logger.error("导入最新数据失败", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "数据导入失败: " + e.getMessage(),
                "stockCode", stockCode
            ));
        }
    }
    
    /**
     * 批量导入多个股票的最新数据
     */
    @Operation(summary = "批量导入最新数据", description = "批量导入多个股票的最新数据")
    @PostMapping("/latest/batch")
    public ResponseEntity<ImportResult> batchImportLatestData(
            @Parameter(description = "股票代码列表") @RequestParam List<String> stockCodes) {
        
        try {
            logger.info("开始批量导入 {} 只股票的最新数据", stockCodes.size());
            
            ImportResult result = dataImportService.batchImportLatestData(stockCodes);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("批量导入最新数据失败", e);
            ImportResult errorResult = new ImportResult();
            errorResult.setTotalStocks(stockCodes.size());
            errorResult.setFailedStocks(stockCodes.size());
            errorResult.addError("批量导入失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }
    
    /**
     * 获取支持的股票代码列表
     */
    @Operation(summary = "获取支持的股票代码", description = "获取所有支持的股票代码列表")
    @GetMapping("/supported-stocks")
    public ResponseEntity<List<String>> getSupportedStockCodes() {
        logger.info("收到获取支持股票代码的请求");
        try {
            List<String> stockCodes = dataImportService.getSupportedStockCodes();
            logger.info("成功获取到 {} 个支持的股票代码", stockCodes.size());
            return ResponseEntity.ok(stockCodes);
        } catch (Exception e) {
            logger.error("获取支持的股票代码失败", e);
            return ResponseEntity.status(500).body(Arrays.asList());
        }
    }
    
    /**
     * 获取数据提供者状态
     */
    @Operation(summary = "获取提供者状态", description = "获取所有数据提供者的状态信息")
    @GetMapping("/providers/status")
    public ResponseEntity<Map<String, Object>> getProvidersStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            List<MarketDataProvider> providers = spiManager.getProviders(MarketDataProvider.class);
            for (MarketDataProvider provider : providers) {
                Map<String, Object> providerInfo = new HashMap<>();
                providerInfo.put("enabled", provider.isEnabled());
                providerInfo.put("priority", provider.getPriority());
                providerInfo.put("supportedStocks", provider.getSupportedStockCodes());
                status.put(provider.getProviderName(), providerInfo);
            }
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("获取提供者状态失败", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 检查数据完整性
     */
    @Operation(summary = "检查数据完整性", description = "检查指定股票在指定时间范围内的数据完整性")
    @GetMapping("/integrity/{stockCode}")
    public ResponseEntity<DataIntegrityReport> checkDataIntegrity(
            @Parameter(description = "股票代码", example = "AAPL") @PathVariable String stockCode,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        try {
            DataIntegrityReport report = dataImportService.checkDataIntegrity(stockCode, startTime, endTime);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("检查数据完整性失败", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 快速导入示例数据
     */
    @Operation(summary = "导入示例数据", description = "快速导入AAPL、TSLA、MSFT等示例股票的近期数据")
    @PostMapping("/sample-data")
    public ResponseEntity<ImportResult> importSampleData() {
        try {
            logger.info("开始导入示例数据");
            
            // 导入最近30天的数据
            List<String> sampleStocks = Arrays.asList("AAPL", "TSLA", "MSFT", "GOOGL", "AMZN");
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(30);
            
            ImportResult result = dataImportService.batchImportHistoricalData(sampleStocks, startTime, endTime);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("导入示例数据失败", e);
            ImportResult errorResult = new ImportResult();
            errorResult.addError("导入示例数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }
}