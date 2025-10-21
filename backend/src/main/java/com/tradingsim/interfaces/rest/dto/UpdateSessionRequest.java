package com.tradingsim.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 更新游戏会话请求DTO
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@Schema(description = "更新游戏会话请求")
public class UpdateSessionRequest {
    
    @NotBlank(message = "操作不能为空")
    @Pattern(regexp = "^(START|PAUSE|RESUME|COMPLETE|CANCEL)$", 
             message = "操作必须是: START, PAUSE, RESUME, COMPLETE, CANCEL 之一")
    @Schema(description = "会话操作", 
            allowableValues = {"START", "PAUSE", "RESUME", "COMPLETE", "CANCEL"},
            example = "START", 
            required = true)
    private String action;
    
    // 默认构造函数
    public UpdateSessionRequest() {}
    
    // 构造函数
    public UpdateSessionRequest(String action) {
        this.action = action;
    }
    
    // Getters and Setters
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    @Override
    public String toString() {
        return "UpdateSessionRequest{" +
                "action='" + action + '\'' +
                '}';
    }
}