package com.tradingsim.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * SPI配置类
 * 配置和管理所有SPI提供者
 */
@Configuration
@ComponentScan(basePackages = "com.tradingsim.infrastructure.spi")
public class SpiConfiguration {
    // SpiManager已经使用@Component注解，会被自动扫描和注册
    // 默认SPI实现也会被自动扫描和注册
}