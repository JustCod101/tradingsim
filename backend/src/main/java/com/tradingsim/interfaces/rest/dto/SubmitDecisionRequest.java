package com.tradingsim.interfaces.rest.dto;

import com.tradingsim.domain.model.GameDecision;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 提交交易决策请求DTO
 * 
 * @author TradingSim Team
 * @version 1.0.0
 */
@Schema(description = "提交交易决策请求")
public class SubmitDecisionRequest {
    
    @NotNull(message = "帧索引不能为空")
    @Min(value = 0, message = "帧索引不能小于0")
    @Schema(description = "帧索引", example = "150", required = true)
    private Integer frameIndex;
    
    @NotNull(message = "决策类型不能为空")
    @Schema(description = "决策类型", 
            allowableValues = {"BUY", "SELL", "SKIP"},
            example = "BUY", 
            required = true)
    private GameDecision.DecisionType decisionType;
    
    @DecimalMin(value = "0.01", message = "价格必须大于0.01")
    @Digits(integer = 10, fraction = 2, message = "价格格式无效")
    @Schema(description = "交易价格", example = "150.25")
    private BigDecimal price;
    
    @Min(value = 1, message = "数量必须大于0")
    @Max(value = 10000, message = "数量不能超过10000")
    @Schema(description = "交易数量", example = "100")
    private Integer quantity;
    
    @Size(max = 100, message = "客户端ID长度不能超过100个字符")
    @Schema(description = "客户端ID", example = "web_client_001")
    private String clientId;
    
    // 默认构造函数
    public SubmitDecisionRequest() {}
    
    // 全参构造函数
    public SubmitDecisionRequest(Integer frameIndex, GameDecision.DecisionType decisionType,
                                BigDecimal price, Integer quantity, String clientId) {
        this.frameIndex = frameIndex;
        this.decisionType = decisionType;
        this.price = price;
        this.quantity = quantity;
        this.clientId = clientId;
    }
    
    /**
     * 验证决策参数的一致性
     */
    @AssertTrue(message = "BUY和SELL决策必须提供价格和数量")
    public boolean isValidDecisionParams() {
        if (decisionType == null) {
            return true; // 让@NotNull处理
        }
        
        switch (decisionType) {
            case BUY:
            case SELL:
                return price != null && quantity != null && quantity > 0;
            case SKIP:
                return true; // SKIP不需要价格和数量
            default:
                return false;
        }
    }
    
    // Getters and Setters
    public Integer getFrameIndex() {
        return frameIndex;
    }
    
    public void setFrameIndex(Integer frameIndex) {
        this.frameIndex = frameIndex;
    }
    
    public GameDecision.DecisionType getDecisionType() {
        return decisionType;
    }
    
    public void setDecisionType(GameDecision.DecisionType decisionType) {
        this.decisionType = decisionType;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    @Override
    public String toString() {
        return "SubmitDecisionRequest{" +
                "frameIndex=" + frameIndex +
                ", decisionType=" + decisionType +
                ", price=" + price +
                ", quantity=" + quantity +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}