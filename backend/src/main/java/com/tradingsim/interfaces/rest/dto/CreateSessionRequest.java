package com.tradingsim.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 创建游戏会话请求DTO
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@Schema(description = "创建游戏会话请求")
public class CreateSessionRequest {
    
    @NotBlank(message = "用户ID不能为空")
    @Size(max = 50, message = "用户ID长度不能超过50个字符")
    @Schema(description = "用户ID", example = "user123", required = true)
    private String userId;
    
    @NotBlank(message = "数据段ID不能为空")
    @Size(max = 100, message = "数据段ID长度不能超过100个字符")
    @Schema(description = "数据段ID", example = "segment_AAPL_1640995200", required = true)
    private String segmentId;
    
    @Size(max = 100, message = "客户端ID长度不能超过100个字符")
    @Schema(description = "客户端ID", example = "web_client_001")
    private String clientId;
    
    // 默认构造函数
    public CreateSessionRequest() {}
    
    // 全参构造函数
    public CreateSessionRequest(String userId, String segmentId, String clientId) {
        this.userId = userId;
        this.segmentId = segmentId;
        this.clientId = clientId;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getSegmentId() {
        return segmentId;
    }
    
    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    @Override
    public String toString() {
        return "CreateSessionRequest{" +
                "userId='" + userId + '\'' +
                ", segmentId='" + segmentId + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}