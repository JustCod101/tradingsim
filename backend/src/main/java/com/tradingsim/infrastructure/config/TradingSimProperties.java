package com.tradingsim.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用配置属性类
 * 映射application.yml中的tradingsim配置
 */
@Component
@ConfigurationProperties(prefix = "tradingsim")
public class TradingSimProperties {

    private Game game = new Game();
    private Market market = new Market();
    private Security security = new Security();
    private Cache cache = new Cache();
    private Monitoring monitoring = new Monitoring();

    // Getters and Setters
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public Market getMarket() { return market; }
    public void setMarket(Market market) { this.market = market; }

    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }

    public Cache getCache() { return cache; }
    public void setCache(Cache cache) { this.cache = cache; }

    public Monitoring getMonitoring() { return monitoring; }
    public void setMonitoring(Monitoring monitoring) { this.monitoring = monitoring; }

    /**
     * 游戏配置
     */
    public static class Game {
        private int maxSessionsPerUser = 5;
        private int sessionTimeoutMinutes = 30;
        private double defaultInitialBalance = 100000.0;
        private int maxDecisionTimeSeconds = 30;

        // Getters and Setters
        public int getMaxSessionsPerUser() { return maxSessionsPerUser; }
        public void setMaxSessionsPerUser(int maxSessionsPerUser) { this.maxSessionsPerUser = maxSessionsPerUser; }

        public int getSessionTimeoutMinutes() { return sessionTimeoutMinutes; }
        public void setSessionTimeoutMinutes(int sessionTimeoutMinutes) { this.sessionTimeoutMinutes = sessionTimeoutMinutes; }

        public double getDefaultInitialBalance() { return defaultInitialBalance; }
        public void setDefaultInitialBalance(double defaultInitialBalance) { this.defaultInitialBalance = defaultInitialBalance; }

        public int getMaxDecisionTimeSeconds() { return maxDecisionTimeSeconds; }
        public void setMaxDecisionTimeSeconds(int maxDecisionTimeSeconds) { this.maxDecisionTimeSeconds = maxDecisionTimeSeconds; }
    }

    /**
     * 市场数据配置
     */
    public static class Market {
        private int dataRetentionDays = 365;
        private int cacheTtlMinutes = 60;
        private int batchSize = 1000;

        // Getters and Setters
        public int getDataRetentionDays() { return dataRetentionDays; }
        public void setDataRetentionDays(int dataRetentionDays) { this.dataRetentionDays = dataRetentionDays; }

        public int getCacheTtlMinutes() { return cacheTtlMinutes; }
        public void setCacheTtlMinutes(int cacheTtlMinutes) { this.cacheTtlMinutes = cacheTtlMinutes; }

        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    }

    /**
     * 安全配置
     */
    public static class Security {
        private Cors cors = new Cors();

        public Cors getCors() { return cors; }
        public void setCors(Cors cors) { this.cors = cors; }

        public static class Cors {
            private List<String> allowedOrigins;
            private List<String> allowedMethods;
            private String allowedHeaders = "*";
            private boolean allowCredentials = true;
            private long maxAge = 3600;

            // Getters and Setters
            public List<String> getAllowedOrigins() { return allowedOrigins; }
            public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }

            public List<String> getAllowedMethods() { return allowedMethods; }
            public void setAllowedMethods(List<String> allowedMethods) { this.allowedMethods = allowedMethods; }

            public String getAllowedHeaders() { return allowedHeaders; }
            public void setAllowedHeaders(String allowedHeaders) { this.allowedHeaders = allowedHeaders; }

            public boolean isAllowCredentials() { return allowCredentials; }
            public void setAllowCredentials(boolean allowCredentials) { this.allowCredentials = allowCredentials; }

            public long getMaxAge() { return maxAge; }
            public void setMaxAge(long maxAge) { this.maxAge = maxAge; }
        }
    }

    /**
     * 缓存配置
     */
    public static class Cache {
        private long defaultTtl = 3600;
        private long marketDataTtl = 1800;
        private long userStatsTtl = 300;

        // Getters and Setters
        public long getDefaultTtl() { return defaultTtl; }
        public void setDefaultTtl(long defaultTtl) { this.defaultTtl = defaultTtl; }

        public long getMarketDataTtl() { return marketDataTtl; }
        public void setMarketDataTtl(long marketDataTtl) { this.marketDataTtl = marketDataTtl; }

        public long getUserStatsTtl() { return userStatsTtl; }
        public void setUserStatsTtl(long userStatsTtl) { this.userStatsTtl = userStatsTtl; }
    }

    /**
     * 监控配置
     */
    public static class Monitoring {
        private Metrics metrics = new Metrics();
        private Alerts alerts = new Alerts();

        public Metrics getMetrics() { return metrics; }
        public void setMetrics(Metrics metrics) { this.metrics = metrics; }

        public Alerts getAlerts() { return alerts; }
        public void setAlerts(Alerts alerts) { this.alerts = alerts; }

        public static class Metrics {
            private boolean enabled = true;
            private int exportInterval = 60;

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }

            public int getExportInterval() { return exportInterval; }
            public void setExportInterval(int exportInterval) { this.exportInterval = exportInterval; }
        }

        public static class Alerts {
            private boolean enabled = true;
            private Thresholds thresholds = new Thresholds();

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }

            public Thresholds getThresholds() { return thresholds; }
            public void setThresholds(Thresholds thresholds) { this.thresholds = thresholds; }

            public static class Thresholds {
                private double errorRate = 0.05;
                private long responseTime = 1000;

                public double getErrorRate() { return errorRate; }
                public void setErrorRate(double errorRate) { this.errorRate = errorRate; }

                public long getResponseTime() { return responseTime; }
                public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
            }
        }
    }
}