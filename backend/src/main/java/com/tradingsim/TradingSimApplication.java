package com.tradingsim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 交易模拟器Spring Boot应用主类
 * 
 * @author TradingSim Team
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@EnableConfigurationProperties
@ComponentScan(basePackages = {"com.tradingsim"})
public class TradingSimApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradingSimApplication.class, args);
    }
}