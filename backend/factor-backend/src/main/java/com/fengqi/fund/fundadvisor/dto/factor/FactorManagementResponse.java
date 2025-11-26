package com.fengqi.fund.fundadvisor.dto.factor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 因子管理响应DTO
 * 统一返回各种因子操作的结果
 */
@Data
@Schema(description = "因子管理响应")
public class FactorManagementResponse {

    @Schema(description = "操作类型", example = "CREATE_DERIVED_FACTOR")
    private String operationType;

    @Schema(description = "操作是否成功", example = "true")
    private Boolean success;

    @Schema(description = "响应消息", example = "衍生因子创建成功")
    private String message;

    @Schema(description = "基础因子信息")
    private BaseFactorData baseFactorData;

    @Schema(description = "衍生因子列表")
    private List<DerivedFactorData> derivedFactorDataList;

    @Schema(description = "权重配置信息")
    private WeightConfigData weightConfigData;

    @Schema(description = "因子树信息")
    private TreeData treeData;

    @Schema(description = "验证结果")
    private ValidationResult validationResult;

    @Schema(description = "操作时间", example = "2025-11-21T10:30:00")
    private LocalDateTime operationTime;

    /**
     * 基础因子数据
     */
    @Data
    @Schema(description = "基础因子数据")
    public static class BaseFactorData {
        @Schema(description = "基础因子ID", example = "1")
        private Integer baseId;

        @Schema(description = "因子名称", example = "市盈率TTM")
        private String factorName;

        @Schema(description = "因子编码", example = "PE_TTM")
        private String factorCode;

        @Schema(description = "因子公式", example = "股价/过去12个月每股收益")
        private String factorFormula;

        @Schema(description = "数据来源", example = "Tushare")
        private String dataSource;

        @Schema(description = "更新频率", example = "日度")
        private String updateFrequency;

        @Schema(description = "数据起始日期", example = "2020-01-01")
        private String dataStartDate;

        @Schema(description = "最新数据日期", example = "2025-11-20")
        private String latestDataDate;

        @Schema(description = "因子说明", example = "反映公司估值水平")
        private String dataDesc;

        @Schema(description = "是否有效", example = "1")
        private Integer isValid;

        @Schema(description = "创建时间", example = "2025-11-21T10:30:00")
        private LocalDateTime createTime;
    }

    /**
     * 衍生因子数据
     */
    @Data
    @Schema(description = "衍生因子数据")
    public static class DerivedFactorData {
        @Schema(description = "衍生因子ID", example = "1")
        private Integer derivedId;

        @Schema(description = "因子名称", example = "估值综合因子")
        private String factorName;

        @Schema(description = "因子编码", example = "VAL_COM")
        private String factorCode;

        @Schema(description = "因子描述", example = "PE与PB加权组合")
        private String factorDesc;

        @Schema(description = "计算策略ID", example = "1")
        private Integer calcStrategyId;

        @Schema(description = "计算策略名称", example = "加权求和")
        private String calcStrategyName;

        @Schema(description = "风格标签ID列表", example = "1,2,3")
        private String styleTagIds;

        @Schema(description = "风格标签名称列表", example = "价值型,成长型")
        private List<String> styleTagNames;

        @Schema(description = "风格标签详情列表")
        private List<StyleTagInfo> styleTags;

        @Schema(description = "所属因子树节点ID", example = "10")
        private Integer treeNodeId;

        @Schema(description = "节点名称", example = "价值因子")
        private String nodeName;

        @Schema(description = "创建人ID", example = "1")
        private Integer createUserId;

        @Schema(description = "创建人姓名", example = "张三")
        private String createUserName;

        @Schema(description = "是否有效", example = "1")
        private Integer isValid;

        @Schema(description = "创建时间", example = "2025-11-21T10:30:00")
        private LocalDateTime createTime;

        @Schema(description = "基础因子列表")
        private List<BaseFactorWithWeight> baseFactors;
    }

    /**
     * 基础因子及权重信息
     */
    @Data
    @Schema(description = "基础因子及权重信息")
    public static class BaseFactorWithWeight {
        @Schema(description = "基础因子ID", example = "1")
        private Integer baseId;

        @Schema(description = "因子名称", example = "市盈率TTM")
        private String factorName;

        @Schema(description = "因子编码", example = "PE_TTM")
        private String factorCode;

        @Schema(description = "权重", example = "0.6")
        private Double weight;

        @Schema(description = "权重说明", example = "用户自定义权重")
        private String weightDesc;
    }

    /**
     * 权重配置数据
     */
    @Data
    @Schema(description = "权重配置数据")
    public static class WeightConfigData {
        @Schema(description = "衍生因子ID", example = "1")
        private Integer derivedId;

        @Schema(description = "基础因子权重配置", example = "{'1': 0.6, '2': 0.4}")
        private Map<Integer, Double> baseFactorWeights;

        @Schema(description = "权重总和", example = "1.0")
        private Double totalWeight;

        @Schema(description = "权重说明", example = "用户自定义权重")
        private String weightDesc;

        @Schema(description = "公式组件备注", example = "基础因子PB需剔除负值后参与计算")
        private String formulaRemark;

        @Schema(description = "基础数据校验状态", example = "PASSED")
        private String baseDataCheckStatus;

        @Schema(description = "校验时间", example = "2025-11-21T10:30:00")
        private LocalDateTime checkTime;
    }

    /**
     * 因子树数据
     */
    @Data
    @Schema(description = "因子树数据")
    public static class TreeData {
        @Schema(description = "树节点ID", example = "1")
        private Integer nodeId;

        @Schema(description = "父节点ID", example = "0")
        private Integer parentId;

        @Schema(description = "节点名称", example = "价值因子")
        private String nodeName;

        @Schema(description = "节点类型", example = "CATEGORY")
        private String nodeType;

        @Schema(description = "因子ID", example = "1")
        private Integer factorId;

        @Schema(description = "是否叶子节点", example = "false")
        private Boolean isLeaf;

        @Schema(description = "排序顺序", example = "1")
        private Integer sortOrder;

        @Schema(description = "节点描述", example = "包含所有价值相关因子")
        private String description;

        @Schema(description = "场景ID", example = "EQUITY")
        private String sceneId;

        @Schema(description = "场景名称", example = "权益投资场景")
        private String sceneName;

        @Schema(description = "子节点列表")
        private List<TreeData> children;
    }

    /**
     * 验证结果
     */
    @Data
    @Schema(description = "验证结果")
    public static class ValidationResult {
        @Schema(description = "验证是否通过", example = "true")
        private Boolean valid;

        @Schema(description = "错误消息列表")
        private List<String> errorMessages;

        @Schema(description = "警告消息列表")
        private List<String> warningMessages;
    }

    /**
     * 风格标签信息
     */
    @Data
    @Schema(description = "风格标签信息")
    public static class StyleTagInfo {
        @Schema(description = "风格标签ID", example = "1")
        private Integer tagId;

        @Schema(description = "风格标签名称", example = "价值")
        private String tagName;

        @Schema(description = "风格标签编码", example = "VALUE")
        private String tagCode;

        @Schema(description = "标签描述", example = "价值风格标签，强调低估值、高分红的投资策略")
        private String description;
    }
}