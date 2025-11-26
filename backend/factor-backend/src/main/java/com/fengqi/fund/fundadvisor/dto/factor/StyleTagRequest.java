package com.fengqi.fund.fundadvisor.dto.factor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 风格标签请求DTO
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@Schema(description = "风格标签请求")
public class StyleTagRequest {
    
    @Schema(description = "风格标签名称", example = "价值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "风格标签名称不能为空")
    @Size(max = 50, message = "风格标签名称长度不能超过50字符")
    private String tagName;
    
    @Schema(description = "风格标签编码", example = "VALUE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "风格标签编码不能为空")
    @Size(max = 50, message = "风格标签编码长度不能超过50字符")
    private String tagCode;
    
    @Schema(description = "标签描述", example = "价值风格标签，强调低估值、高分红的投资策略")
    @Size(max = 500, message = "标签描述长度不能超过500字符")
    private String description;
    
    @Schema(description = "创建人ID", example = "1")
    private Integer createUserId;
    
    // Getters and Setters
    public String getTagName() {
        return tagName;
    }
    
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    
    public String getTagCode() {
        return tagCode;
    }
    
    public void setTagCode(String tagCode) {
        this.tagCode = tagCode;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getCreateUserId() {
        return createUserId;
    }
    
    public void setCreateUserId(Integer createUserId) {
        this.createUserId = createUserId;
    }
}

