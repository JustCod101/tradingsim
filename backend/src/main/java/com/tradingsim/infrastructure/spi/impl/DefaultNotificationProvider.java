package com.tradingsim.infrastructure.spi.impl;

import com.tradingsim.infrastructure.spi.NotificationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认通知提供者实现
 * 使用日志记录通知信息
 */
@Component
public class DefaultNotificationProvider implements NotificationProvider {

    private static final Logger logger = LoggerFactory.getLogger(DefaultNotificationProvider.class);

    @Override
    public String getProviderName() {
        return "DefaultNotificationProvider";
    }

    @Override
    public String[] getSupportedTypes() {
        return new String[]{"GAME_START", "GAME_END", "DECISION_RESULT", "SCORE_UPDATE", "ERROR"};
    }

    @Override
    public boolean sendNotification(String type, String recipient, String subject, String content, Map<String, Object> parameters) {
        try {
            // 使用日志记录通知
            logger.info("Notification sent - Type: {}, Recipient: {}, Subject: {}, Content: {}, Parameters: {}", 
                       type, recipient, subject, content, parameters);
            
            // 根据通知类型使用不同的日志级别
            switch (type) {
                case "ERROR":
                    logger.error("Error notification for {}: {} - {}", recipient, subject, content);
                    break;
                case "GAME_START":
                case "GAME_END":
                    logger.info("Game notification for {}: {} - {}", recipient, subject, content);
                    break;
                case "DECISION_RESULT":
                case "SCORE_UPDATE":
                    logger.debug("Game update notification for {}: {} - {}", recipient, subject, content);
                    break;
                default:
                    logger.info("General notification for {}: {} - {}", recipient, subject, content);
                    break;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Failed to send notification", e);
            return false;
        }
    }

    @Override
    public Map<String, Boolean> sendBatchNotification(String type, String[] recipients, String subject, String content, Map<String, Object> parameters) {
        Map<String, Boolean> results = new HashMap<>();
        
        if (recipients == null || recipients.length == 0) {
            return results;
        }

        for (String recipient : recipients) {
            boolean success = sendNotification(type, recipient, subject, content, parameters);
            results.put(recipient, success);
        }

        logger.info("Batch notification completed: {}/{} successful", 
                   results.values().stream().mapToInt(success -> success ? 1 : 0).sum(), 
                   recipients.length);
        
        return results;
    }

    @Override
    public boolean validateConfiguration() {
        // 默认实现不需要特殊配置
        return true;
    }

    @Override
    public int getPriority() {
        return 100; // 默认优先级
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}