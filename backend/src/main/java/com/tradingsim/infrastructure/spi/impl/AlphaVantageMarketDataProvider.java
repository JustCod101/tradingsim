package com.tradingsim.infrastructure.spi.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingsim.domain.model.OhlcvData;
import com.tradingsim.domain.model.OhlcvId;
import com.tradingsim.infrastructure.spi.MarketDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Alpha Vantage市场数据提供者
 * 从Alpha Vantage获取真实股票数据
 */
@Component
public class AlphaVantageMarketDataProvider implements MarketDataProvider {

    private static final Logger logger = LoggerFactory.getLogger(AlphaVantageMarketDataProvider.class);
    
    private String apiKey;
    private boolean enabled;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    // Alpha Vantage免费版限制：每分钟5次请求，每天500次请求
    private static final String BASE_URL = "https://www.alphavantage.co/query";
    
    public AlphaVantageMarketDataProvider() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        loadConfiguration();
    }
    
    private void loadConfiguration() {
        try {
            // 尝试从系统属性获取配置
            this.apiKey = System.getProperty("alphavantage.api.key", "demo");
            this.enabled = Boolean.parseBoolean(System.getProperty("alphavantage.api.enabled", "false"));
            
            // 如果系统属性中没有，尝试从环境变量获取
            if ("demo".equals(this.apiKey)) {
                String envApiKey = System.getenv("ALPHAVANTAGE_API_KEY");
                if (envApiKey != null && !envApiKey.isEmpty()) {
                    this.apiKey = envApiKey;
                }
            }
            
            if (!this.enabled) {
                String envEnabled = System.getenv("ALPHAVANTAGE_API_ENABLED");
                if (envEnabled != null && !envEnabled.isEmpty()) {
                    this.enabled = Boolean.parseBoolean(envEnabled);
                }
            }
            
            logger.info("Alpha Vantage配置加载完成 - enabled: {}, apiKey: {}", 
                       this.enabled, this.apiKey.length() > 4 ? this.apiKey.substring(0, 4) + "****" : "****");
        } catch (Exception e) {
            logger.warn("加载Alpha Vantage配置失败，使用默认值", e);
            this.apiKey = "demo";
            this.enabled = false;
        }
    }

    @Override
    public String getProviderName() {
        return "AlphaVantageMarketDataProvider";
    }

    @Override
    public List<String> getSupportedStockCodes() {
        // 支持的主要美股代码
        return Arrays.asList("AAPL", "GOOGL", "MSFT", "TSLA", "AMZN", "META", "NVDA", "NFLX");
    }

    @Override
    public List<OhlcvData> getOhlcvData(String stockCode, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("开始获取历史数据: stockCode={}, startTime={}, endTime={}", stockCode, startTime, endTime);
        try {
            // 使用TIME_SERIES_INTRADAY获取1分钟数据
            String url = String.format("%s?function=TIME_SERIES_INTRADAY&symbol=%s&interval=1min&apikey=%s&outputsize=full",
                    BASE_URL, stockCode, apiKey);
            
            logger.info("请求URL: {}", url.replace(apiKey, "***"));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            logger.info("API响应状态码: {}", response.statusCode());
            
            if (response.statusCode() == 200) {
                List<OhlcvData> result = parseIntradayResponse(response.body(), stockCode, startTime, endTime);
                logger.info("解析得到 {} 条历史数据", result.size());
                return result;
            } else {
                logger.error("Alpha Vantage API请求失败: {}", response.statusCode());
                return new ArrayList<>();
            }
            
        } catch (Exception e) {
            logger.error("获取股票数据失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public OhlcvData getLatestOhlcvData(String stockCode) {
        try {
            // 使用GLOBAL_QUOTE获取最新报价
            String url = String.format("%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                    BASE_URL, stockCode, apiKey);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseQuoteResponse(response.body(), stockCode);
            }
            
        } catch (Exception e) {
            logger.error("获取最新股票数据失败: {}", e.getMessage(), e);
        }
        
        return null;
    }

    @Override
    public boolean isDataAvailable(String stockCode, LocalDateTime timestamp) {
        return getSupportedStockCodes().contains(stockCode) && enabled;
    }

    @Override
    public int getPriority() {
        return 50; // 中等优先级，低于默认数据库提供者
    }

    @Override
    public boolean isEnabled() {
        return enabled && !"demo".equals(apiKey);
    }
    
    /**
     * 解析日内数据响应
     */
    private List<OhlcvData> parseIntradayResponse(String responseBody, String stockCode, 
                                                 LocalDateTime startTime, LocalDateTime endTime) {
        List<OhlcvData> result = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode timeSeries = root.get("Time Series (1min)");
            
            if (timeSeries == null) {
                logger.warn("Alpha Vantage响应中没有时间序列数据");
                return result;
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            timeSeries.fields().forEachRemaining(entry -> {
                try {
                    String timeStr = entry.getKey();
                    JsonNode data = entry.getValue();
                    
                    LocalDateTime timestamp = LocalDateTime.parse(timeStr, formatter);
                    
                    // 过滤时间范围
                    if (timestamp.isBefore(startTime) || timestamp.isAfter(endTime)) {
                        return;
                    }
                    
                    BigDecimal open = new BigDecimal(data.get("1. open").asText());
                    BigDecimal high = new BigDecimal(data.get("2. high").asText());
                    BigDecimal low = new BigDecimal(data.get("3. low").asText());
                    BigDecimal close = new BigDecimal(data.get("4. close").asText());
                    Long volume = data.get("5. volume").asLong();
                    
                    OhlcvId id = new OhlcvId(stockCode, timestamp.toInstant(ZoneOffset.UTC));
                    OhlcvData ohlcvData = new OhlcvData(id, open, high, low, close, volume);
                    
                    result.add(ohlcvData);
                    
                } catch (Exception e) {
                    logger.warn("解析数据点失败: {}", e.getMessage());
                }
            });
            
        } catch (IOException e) {
            logger.error("解析Alpha Vantage响应失败: {}", e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 解析报价响应
     */
    private OhlcvData parseQuoteResponse(String responseBody, String stockCode) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode quote = root.get("Global Quote");
            
            if (quote == null) {
                logger.warn("Alpha Vantage响应中没有报价数据");
                return null;
            }
            
            BigDecimal open = new BigDecimal(quote.get("02. open").asText());
            BigDecimal high = new BigDecimal(quote.get("03. high").asText());
            BigDecimal low = new BigDecimal(quote.get("04. low").asText());
            BigDecimal close = new BigDecimal(quote.get("05. price").asText());
            Long volume = quote.get("06. volume").asLong();
            
            // 使用当前时间作为时间戳
            Instant timestamp = Instant.now();
            OhlcvId id = new OhlcvId(stockCode, timestamp);
            
            return new OhlcvData(id, open, high, low, close, volume);
            
        } catch (Exception e) {
            logger.error("解析Alpha Vantage报价响应失败: {}", e.getMessage(), e);
            return null;
        }
    }
}