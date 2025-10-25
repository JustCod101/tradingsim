package com.tradingsim.application.service.impl;

import com.tradingsim.application.service.DataImportService;
import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.domain.repository.OhlcvDataRepository;
import com.tradingsim.infrastructure.spi.MarketDataProvider;
import com.tradingsim.infrastructure.spi.SpiManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 数据导入服务实现
 * 
 * @author TradingSim Team
 */
@Service
public class DataImportServiceImpl implements DataImportService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataImportServiceImpl.class);
    
    @Autowired
    private OhlcvDataRepository ohlcvDataRepository;
    
    @Autowired
    private SpiManager spiManager;
    
    @Override
    @Transactional
    public int importHistoricalData(String stockCode, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("开始导入股票 {} 的历史数据，时间范围: {} - {}", stockCode, startTime, endTime);
        
        // 获取可用的数据提供者
        List<MarketDataProvider> allProviders = spiManager.getProviders(MarketDataProvider.class);
        logger.info("所有可用的数据提供者: {}", allProviders.stream().map(MarketDataProvider::getProviderName).collect(Collectors.toList()));
        
        List<MarketDataProvider> enabledProviders = allProviders.stream()
                .filter(MarketDataProvider::isEnabled)
                .collect(Collectors.toList());
        logger.info("已启用的数据提供者: {}", enabledProviders.stream().map(MarketDataProvider::getProviderName).collect(Collectors.toList()));
        
        List<MarketDataProvider> providers = enabledProviders.stream()
                .filter(p -> p.getSupportedStockCodes().contains(stockCode))
                .sorted((p1, p2) -> Integer.compare(p1.getPriority(), p2.getPriority()))
                .collect(Collectors.toList());
        
        logger.info("支持股票 {} 的数据提供者: {}", stockCode, providers.stream().map(MarketDataProvider::getProviderName).collect(Collectors.toList()));
        
        if (providers.isEmpty()) {
            logger.warn("没有找到支持股票 {} 的数据提供者", stockCode);
            return 0;
        }
        
        int importedCount = 0;
        
        for (MarketDataProvider provider : providers) {
            try {
                logger.info("使用提供者 {} 获取数据", provider.getProviderName());
                
                List<OhlcvData> data = provider.getOhlcvData(stockCode, startTime, endTime);
                
                if (data != null && !data.isEmpty()) {
                    // 批量保存数据
                    for (OhlcvData ohlcvData : data) {
                        try {
                            // 检查数据是否已存在
                            if (!ohlcvDataRepository.findById(ohlcvData.getId()).isPresent()) {
                                ohlcvDataRepository.save(ohlcvData);
                                importedCount++;
                            }
                        } catch (Exception e) {
                            logger.warn("保存数据失败: {}", e.getMessage());
                        }
                    }
                    
                    logger.info("从提供者 {} 成功导入 {} 条数据", provider.getProviderName(), data.size());
                    break; // 成功获取数据后退出循环
                }
                
            } catch (Exception e) {
                logger.error("提供者 {} 获取数据失败: {}", provider.getProviderName(), e.getMessage(), e);
            }
        }
        
        logger.info("股票 {} 历史数据导入完成，共导入 {} 条记录", stockCode, importedCount);
        return importedCount;
    }
    
    @Override
    public ImportResult batchImportHistoricalData(List<String> stockCodes, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("开始批量导入 {} 只股票的历史数据", stockCodes.size());
        
        ImportResult result = new ImportResult();
        result.setTotalStocks(stockCodes.size());
        
        for (String stockCode : stockCodes) {
            try {
                int imported = importHistoricalData(stockCode, startTime, endTime);
                if (imported > 0) {
                    result.setSuccessfulStocks(result.getSuccessfulStocks() + 1);
                    result.setTotalRecords(result.getTotalRecords() + imported);
                } else {
                    result.setFailedStocks(result.getFailedStocks() + 1);
                    result.addError("股票 " + stockCode + " 导入失败：未获取到数据");
                }
                
                // 添加延迟以避免API限制
                Thread.sleep(1000);
                
            } catch (Exception e) {
                result.setFailedStocks(result.getFailedStocks() + 1);
                result.addError("股票 " + stockCode + " 导入失败：" + e.getMessage());
                logger.error("导入股票 {} 失败", stockCode, e);
            }
        }
        
        logger.info("批量导入完成，成功: {}, 失败: {}, 总记录数: {}", 
                result.getSuccessfulStocks(), result.getFailedStocks(), result.getTotalRecords());
        
        return result;
    }
    
    @Override
    @Transactional
    public boolean importLatestData(String stockCode) {
        logger.info("开始导入股票 {} 的最新数据", stockCode);
        
        // 获取可用的数据提供者
        List<MarketDataProvider> providers = spiManager.getProviders(MarketDataProvider.class)
                .stream()
                .filter(MarketDataProvider::isEnabled)
                .filter(p -> p.getSupportedStockCodes().contains(stockCode))
                .sorted((p1, p2) -> Integer.compare(p1.getPriority(), p2.getPriority()))
                .collect(Collectors.toList());
        
        if (providers.isEmpty()) {
            logger.warn("没有找到支持股票 {} 的数据提供者", stockCode);
            return false;
        }
        
        for (MarketDataProvider provider : providers) {
            try {
                OhlcvData latestData = provider.getLatestOhlcvData(stockCode);
                
                if (latestData != null) {
                    // 检查数据是否已存在
                    if (!ohlcvDataRepository.findById(latestData.getId()).isPresent()) {
                        ohlcvDataRepository.save(latestData);
                        logger.info("成功导入股票 {} 的最新数据", stockCode);
                        return true;
                    } else {
                        logger.info("股票 {} 的最新数据已存在", stockCode);
                        return true;
                    }
                }
                
            } catch (Exception e) {
                logger.error("提供者 {} 获取最新数据失败: {}", provider.getProviderName(), e.getMessage(), e);
            }
        }
        
        logger.warn("无法获取股票 {} 的最新数据", stockCode);
        return false;
    }
    
    @Override
    public ImportResult batchImportLatestData(List<String> stockCodes) {
        logger.info("开始批量导入 {} 只股票的最新数据", stockCodes.size());
        
        ImportResult result = new ImportResult();
        result.setTotalStocks(stockCodes.size());
        
        for (String stockCode : stockCodes) {
            try {
                boolean success = importLatestData(stockCode);
                if (success) {
                    result.setSuccessfulStocks(result.getSuccessfulStocks() + 1);
                    result.setTotalRecords(result.getTotalRecords() + 1);
                } else {
                    result.setFailedStocks(result.getFailedStocks() + 1);
                    result.addError("股票 " + stockCode + " 最新数据导入失败");
                }
                
                // 添加延迟以避免API限制
                Thread.sleep(500);
                
            } catch (Exception e) {
                result.setFailedStocks(result.getFailedStocks() + 1);
                result.addError("股票 " + stockCode + " 最新数据导入失败：" + e.getMessage());
                logger.error("导入股票 {} 最新数据失败", stockCode, e);
            }
        }
        
        logger.info("批量导入最新数据完成，成功: {}, 失败: {}", 
                result.getSuccessfulStocks(), result.getFailedStocks());
        
        return result;
    }
    
    @Override
    public List<String> getSupportedStockCodes() {
        return spiManager.getProviders(MarketDataProvider.class)
                .stream()
                .filter(MarketDataProvider::isEnabled)
                .flatMap(p -> p.getSupportedStockCodes().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    @Override
    public DataIntegrityReport checkDataIntegrity(String stockCode, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("检查股票 {} 数据完整性，时间范围: {} - {}", stockCode, startTime, endTime);
        
        DataIntegrityReport report = new DataIntegrityReport();
        report.setStockCode(stockCode);
        report.setStartTime(startTime);
        report.setEndTime(endTime);
        
        // 计算期望的记录数（假设1分钟间隔，工作日9:30-16:00）
        long expectedRecords = calculateExpectedRecords(startTime, endTime);
        report.setExpectedRecords(expectedRecords);
        
        // 获取实际记录数
        List<OhlcvData> dataInRange = ohlcvDataRepository.findByStockCodeAndTimestampBetween(
                stockCode, 
                startTime.atZone(java.time.ZoneOffset.UTC).toInstant(),
                endTime.atZone(java.time.ZoneOffset.UTC).toInstant()
        );
        long actualRecords = dataInRange.size();
        report.setActualRecords(actualRecords);
        
        // 计算完整性百分比
        double completeness = expectedRecords > 0 ? (double) actualRecords / expectedRecords * 100 : 0;
        report.setCompleteness(completeness);
        
        logger.info("股票 {} 数据完整性检查完成，完整性: {:.2f}%", stockCode, completeness);
        
        return report;
    }
    
    /**
     * 计算期望的记录数
     */
    private long calculateExpectedRecords(LocalDateTime startTime, LocalDateTime endTime) {
        // 简化计算：假设每天6.5小时交易时间，每分钟一条记录
        long days = ChronoUnit.DAYS.between(startTime.toLocalDate(), endTime.toLocalDate()) + 1;
        
        // 假设工作日比例为5/7
        long workingDays = days * 5 / 7;
        
        // 每个交易日390分钟（6.5小时）
        return workingDays * 390;
    }
}