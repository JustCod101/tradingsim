package com.tradingsim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * TradingSim 主启动类
 * 
 * 功能特性:
 * - Spring Boot 自动配置
 * - 缓存支持 (Redis)
 * - 异步任务支持
 * - 定时任务支持
 * - 事务管理支持
 * - JPA 数据访问
 * - WebSocket 实时通信
 * - Micrometer 监控指标
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching          // 启用缓存支持
@EnableAsync           // 启用异步任务支持
@EnableScheduling      // 启用定时任务支持
@EnableTransactionManagement  // 启用事务管理
public class TradingSimApplication {

    public static void main(String[] args) {
        // 设置系统属性 - 可配置
        System.setProperty("spring.application.name", "tradingsim-backend");
        System.setProperty("java.awt.headless", "true");
        
        // 启动应用
        SpringApplication app = new SpringApplication(TradingSimApplication.class);
        
        // 可配置: 设置默认配置文件
        app.setDefaultProperties(java.util.Map.of(
            "spring.profiles.default", "dev",
            "server.port", "8080",
            "management.endpoints.web.exposure.include", "health,info,metrics,prometheus"
        ));
        
        app.run(args);
    }
}