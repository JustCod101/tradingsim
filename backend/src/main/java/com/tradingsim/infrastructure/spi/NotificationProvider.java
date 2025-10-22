package com.tradingsim.infrastructure.spi;

import java.util.Map;

/**
 * 通知提供者SPI接口
 * 允许插件化的通知实现（邮件、短信、推送等）
 */
public interface NotificationProvider {

    /**
     * 获取提供者名称
     */
    String getProviderName();

    /**
     * 获取支持的通知类型
     */
    String[] getSupportedTypes();

    /**
     * 发送通知
     * 
     * @param type 通知类型
     * @param recipient 接收者
     * @param subject 主题
     * @param content 内容
     * @param parameters 额外参数
     * @return 是否发送成功
     */
    boolean sendNotification(String type, String recipient, String subject, String content, Map<String, Object> parameters);

    /**
     * 批量发送通知
     * 
     * @param type 通知类型
     * @param recipients 接收者列表
     * @param subject 主题
     * @param content 内容
     * @param parameters 额外参数
     * @return 发送结果（接收者 -> 是否成功）
     */
    Map<String, Boolean> sendBatchNotification(String type, String[] recipients, String subject, String content, Map<String, Object> parameters);

    /**
     * 验证配置是否正确
     */
    boolean validateConfiguration();

    /**
     * 获取提供者优先级（数字越小优先级越高）
     */
    int getPriority();

    /**
     * 检查提供者是否启用
     */
    boolean isEnabled();
}