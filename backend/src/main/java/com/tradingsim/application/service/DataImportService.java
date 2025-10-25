package com.tradingsim.application.service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据导入服务接口
 * 
 * @author TradingSim Team
 */
public interface DataImportService {
    
    /**
     * 导入指定股票的历史数据
     * 
     * @param stockCode 股票代码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 导入的数据条数
     */
    int importHistoricalData(String stockCode, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 批量导入多只股票的历史数据
     * 
     * @param stockCodes 股票代码列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 导入结果统计
     */
    ImportResult batchImportHistoricalData(List<String> stockCodes, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 导入最新数据
     * 
     * @param stockCode 股票代码
     * @return 是否成功
     */
    boolean importLatestData(String stockCode);
    
    /**
     * 批量导入最新数据
     * 
     * @param stockCodes 股票代码列表
     * @return 导入结果统计
     */
    ImportResult batchImportLatestData(List<String> stockCodes);
    
    /**
     * 获取支持的股票代码列表
     * 
     * @return 股票代码列表
     */
    List<String> getSupportedStockCodes();
    
    /**
     * 检查数据完整性
     * 
     * @param stockCode 股票代码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 数据完整性报告
     */
    DataIntegrityReport checkDataIntegrity(String stockCode, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 导入结果统计
     */
    class ImportResult {
        private int totalStocks;
        private int successfulStocks;
        private int failedStocks;
        private int totalRecords;
        private List<String> errors;
        
        public ImportResult() {
            this.errors = new java.util.ArrayList<>();
        }
        
        // Getters and Setters
        public int getTotalStocks() { return totalStocks; }
        public void setTotalStocks(int totalStocks) { this.totalStocks = totalStocks; }
        
        public int getSuccessfulStocks() { return successfulStocks; }
        public void setSuccessfulStocks(int successfulStocks) { this.successfulStocks = successfulStocks; }
        
        public int getFailedStocks() { return failedStocks; }
        public void setFailedStocks(int failedStocks) { this.failedStocks = failedStocks; }
        
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public void addError(String error) { this.errors.add(error); }
        
        public double getSuccessRate() {
            return totalStocks > 0 ? (double) successfulStocks / totalStocks * 100 : 0;
        }
    }
    
    /**
     * 数据完整性报告
     */
    class DataIntegrityReport {
        private String stockCode;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private long expectedRecords;
        private long actualRecords;
        private double completeness;
        private List<String> missingPeriods;
        
        public DataIntegrityReport() {
            this.missingPeriods = new java.util.ArrayList<>();
        }
        
        // Getters and Setters
        public String getStockCode() { return stockCode; }
        public void setStockCode(String stockCode) { this.stockCode = stockCode; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public long getExpectedRecords() { return expectedRecords; }
        public void setExpectedRecords(long expectedRecords) { this.expectedRecords = expectedRecords; }
        
        public long getActualRecords() { return actualRecords; }
        public void setActualRecords(long actualRecords) { this.actualRecords = actualRecords; }
        
        public double getCompleteness() { return completeness; }
        public void setCompleteness(double completeness) { this.completeness = completeness; }
        
        public List<String> getMissingPeriods() { return missingPeriods; }
        public void setMissingPeriods(List<String> missingPeriods) { this.missingPeriods = missingPeriods; }
        
        public boolean isComplete() { return completeness >= 95.0; }
    }
}