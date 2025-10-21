package com.tradingsim.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

/**
 * 游戏重放请求DTO
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@Schema(description = "游戏重放请求")
public class ReplayRequest {
    
    @NotNull(message = "重放速度不能为空")
    @DecimalMin(value = "0.1", message = "重放速度不能小于0.1")
    @DecimalMax(value = "10.0", message = "重放速度不能大于10.0")
    @Schema(description = "重放速度倍数", example = "2.0", required = true)
    private Double speed;
    
    @Schema(description = "是否包含决策详情", example = "true")
    private boolean includeDecisions = true;
    
    @Schema(description = "是否包含评分详情", example = "true")
    private boolean includeScoring = true;
    
    // 默认构造函数
    public ReplayRequest() {}
    
    // 构造函数
    public ReplayRequest(Double speed, boolean includeDecisions, boolean includeScoring) {
        this.speed = speed;
        this.includeDecisions = includeDecisions;
        this.includeScoring = includeScoring;
    }
    
    // Getters and Setters
    public Double getSpeed() {
        return speed;
    }
    
    public void setSpeed(Double speed) {
        this.speed = speed;
    }
    
    public boolean isIncludeDecisions() {
        return includeDecisions;
    }
    
    public void setIncludeDecisions(boolean includeDecisions) {
        this.includeDecisions = includeDecisions;
    }
    
    public boolean isIncludeScoring() {
        return includeScoring;
    }
    
    public void setIncludeScoring(boolean includeScoring) {
        this.includeScoring = includeScoring;
    }
    
    @Override
    public String toString() {
        return "ReplayRequest{" +
                "speed=" + speed +
                ", includeDecisions=" + includeDecisions +
                ", includeScoring=" + includeScoring +
                '}';
    }
}