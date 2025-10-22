package com.tradingsim.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * Swagger/OpenAPI 配置类
 * 配置API文档生成和Knife4j界面
 * 
 * @author TradingSim Team
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置OpenAPI文档信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .addSecurityItem(securityRequirement())
                .schemaRequirement("Bearer Authentication", securityScheme());
    }

    /**
     * API基本信息
     */
    private Info apiInfo() {
        return new Info()
                .title("TradingSim API")
                .description("交易模拟系统后端API文档")
                .version("1.0.0")
                .contact(new Contact()
                        .name("TradingSim Team")
                        .email("support@tradingsim.com")
                        .url("https://github.com/tradingsim/tradingsim"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * 服务器列表
     */
    private List<Server> serverList() {
        Server devServer = new Server()
                .url("http://localhost:8080")
                .description("开发环境");
        
        Server prodServer = new Server()
                .url("https://api.tradingsim.com")
                .description("生产环境");
        
        return List.of(devServer, prodServer);
    }

    /**
     * 安全要求
     */
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("Bearer Authentication");
    }

    /**
     * 安全方案
     */
    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name("Bearer Authentication")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("请在请求头中添加 Authorization: Bearer {token}");
    }
}