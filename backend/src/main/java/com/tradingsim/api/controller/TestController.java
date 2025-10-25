package com.tradingsim.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 测试控制器
 * 用于验证Spring Boot配置是否正确
 */
@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*")
public class TestController {
    
    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Hello from TradingSim API!",
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    @GetMapping("/data-import-test")
    public ResponseEntity<Map<String, Object>> dataImportTest() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Data import endpoint test successful!",
            "supportedStocks", new String[]{"AAPL", "TSLA", "MSFT", "GOOGL", "AMZN"}
        ));
    }
}