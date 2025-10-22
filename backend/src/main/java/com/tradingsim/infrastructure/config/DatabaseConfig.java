package com.tradingsim.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 数据库配置
 * 
 * @author TradingSim Team
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.tradingsim.infrastructure.persistence")
@EntityScan(basePackages = "com.tradingsim.domain.model")
@EnableTransactionManagement
public class DatabaseConfig {
    
    // JPA配置由Spring Boot自动配置
}