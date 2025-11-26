package com.fengqi.fund.fundadvisor.dto.factor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 因子管理请求DTO
 * 统一管理基础因子、衍生因子、权重配置等操作
 */
public class FactorManagementRequest {

    @Schema(description = "操作类型", example = "CREATE_DERIVED_FACTOR")
    @NotBlank(message = "操作类型不能为空")
    private String operationType;



    @Schema(description = "衍生因子信息")
    private DerivedFactorInfo derivedFactorInfo;

    @Schema(description = "权重配置信息")
    private WeightConfigInfo weightConfigInfo;

    @Schema(description = "因子树操作信息")
    private TreeOperationInfo treeOperationInfo;

    /**
     * 基础因子信息
     */
    @Schema(description = "基础因子信息")
    public static class BaseFactorInfo {
        @Schema(description = "因子名称", example = "市盈率TTM")
        @NotBlank(message = "因子名称不能为空")
        @Size(max = 100, message = "因子名称长度不能超过100字符")
        private String factorName;

        @Schema(description = "因子编码", example = "PE_TTM")
        @NotBlank(message = "因子编码不能为空")
        @Size(max = 50, message = "因子编码长度不能超过50字符")
        private String factorCode;

        @Schema(description = "因子公式", example = "股价/过去12个月每股收益")
        private String factorFormula;

        @Schema(description = "数据来源", example = "Tushare")
        private String dataSource;

        @Schema(description = "更新频率", example = "日度")
        @NotBlank(message = "更新频率不能为空")
        private String updateFrequency;

        @Schema(description = "数据起始日期", example = "2020-01-01")
        private String dataStartDate;

        @Schema(description = "因子说明", example = "反映公司估值水平")
        private String dataDesc;

        // Getters and Setters
        public String getFactorName() {
            return factorName;
        }

        public void setFactorName(String factorName) {
            this.factorName = factorName;
        }

        public String getFactorCode() {
            return factorCode;
        }

        public void setFactorCode(String factorCode) {
            this.factorCode = factorCode;
        }

        public String getFactorFormula() {
            return factorFormula;
        }

        public void setFactorFormula(String factorFormula) {
            this.factorFormula = factorFormula;
        }

        public String getDataSource() {
            return dataSource;
        }

        public void setDataSource(String dataSource) {
            this.dataSource = dataSource;
        }

        public String getUpdateFrequency() {
            return updateFrequency;
        }

        public void setUpdateFrequency(String updateFrequency) {
            this.updateFrequency = updateFrequency;
        }

        public String getDataStartDate() {
            return dataStartDate;
        }

        public void setDataStartDate(String dataStartDate) {
            this.dataStartDate = dataStartDate;
        }

        public String getDataDesc() {
            return dataDesc;
        }

        public void setDataDesc(String dataDesc) {
            this.dataDesc = dataDesc;
        }
    }

    /**
     * 衍生因子信息
     */
    @Schema(description = "衍生因子信息")
    public static class DerivedFactorInfo {
        @Schema(description = "衍生因子名称", example = "估值综合因子")
        @NotBlank(message = "衍生因子名称不能为空")
        @Size(max = 100, message = "衍生因子名称长度不能超过100字符")
        private String factorName;

        @Schema(description = "衍生因子编码", example = "VAL_COM")
        @NotBlank(message = "衍生因子编码不能为空")
        @Size(max = 50, message = "衍生因子编码长度不能超过50字符")
        private String factorCode;

        @Schema(description = "衍生因子描述", example = "PE与PB加权组合")
        private String factorDesc;

        @Schema(description = "计算策略ID", example = "1")
        // 注释掉@NotNull约束，因为现在只用一种归一化处理后的加权组合策略
        private Integer calcStrategyId;

        @Schema(description = "关联的风格标签编码列表（多个用逗号分隔）", example = "001,002,003", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private String styleTagCodes;

        @Schema(description = "所属因子树节点ID", example = "10")
        private Integer treeNodeId;

        @Schema(description = "创建人ID", example = "1")
        private Integer createUserId;

        @Schema(description = "选中的基础因子ID列表")
        private List<Integer> baseFactorIds;

        // Getters and Setters
        public String getFactorName() {
            return factorName;
        }

        public void setFactorName(String factorName) {
            this.factorName = factorName;
        }

        public String getFactorCode() {
            return factorCode;
        }

        public void setFactorCode(String factorCode) {
            this.factorCode = factorCode;
        }

        public String getFactorDesc() {
            return factorDesc;
        }

        public void setFactorDesc(String factorDesc) {
            this.factorDesc = factorDesc;
        }

        public Integer getCalcStrategyId() {
            return calcStrategyId;
        }

        public void setCalcStrategyId(Integer calcStrategyId) {
            this.calcStrategyId = calcStrategyId;
        }

        public String getStyleTagCodes() {
            return styleTagCodes;
        }

        public void setStyleTagCodes(String styleTagCodes) {
            this.styleTagCodes = styleTagCodes;
        }

        public Integer getTreeNodeId() {
            return treeNodeId;
        }

        public void setTreeNodeId(Integer treeNodeId) {
            this.treeNodeId = treeNodeId;
        }

        public Integer getCreateUserId() {
            return createUserId;
        }

        public void setCreateUserId(Integer createUserId) {
            this.createUserId = createUserId;
        }

        public List<Integer> getBaseFactorIds() {
            return baseFactorIds;
        }

        public void setBaseFactorIds(List<Integer> baseFactorIds) {
            this.baseFactorIds = baseFactorIds;
        }
    }

    /**
     * 权重配置信息
     */
    @Schema(description = "权重配置信息")
    public static class WeightConfigInfo {
        @JsonIgnore  // 不在请求参数中显示，由系统自动设置
        @Schema(description = "衍生因子ID（内部使用，自动设置）", hidden = true)
        private Integer derivedId;

        @Schema(description = "基础因子权重配置", example = "{'1': 0.6, '2': 0.4}")
        private String baseFactorWeights;

        @Schema(description = "权重说明", example = "用户自定义权重")
        private String weightDesc;

        @Schema(description = "公式组件备注", example = "基础因子PB需剔除负值后参与计算")
        private String formulaRemark;

        // Getters and Setters
        public Integer getDerivedId() {
            return derivedId;
        }

        public void setDerivedId(Integer derivedId) {
            this.derivedId = derivedId;
        }

        public String getBaseFactorWeights() {
            return baseFactorWeights;
        }

        public void setBaseFactorWeights(String baseFactorWeights) {
            this.baseFactorWeights = baseFactorWeights;
        }

        public String getWeightDesc() {
            return weightDesc;
        }

        public void setWeightDesc(String weightDesc) {
            this.weightDesc = weightDesc;
        }

        public String getFormulaRemark() {
            return formulaRemark;
        }

        public void setFormulaRemark(String formulaRemark) {
            this.formulaRemark = formulaRemark;
        }
    }

    /**
     * 因子树操作信息
     */
    @Schema(description = "因子树操作信息")
    public static class TreeOperationInfo {
        @JsonIgnore  // 不在请求参数中显示，由系统自动生成
        @Schema(description = "树节点ID（内部使用，自动生成）", hidden = true)
        private Integer nodeId;

        @Schema(description = "父节点ID（0或null表示添加到根节点）", example = "0")
        private Integer parentId;

        @Schema(description = "节点名称（可选，如果不提供则使用衍生因子名称）", example = "价值因子")
        private String nodeName;

        @Schema(description = "节点类型（可选，默认为FACTOR）", example = "CATEGORY")
        private String nodeType;

        @JsonIgnore  // 不在请求参数中显示，由系统自动设置
        @Schema(description = "因子ID（内部使用，自动设置）", hidden = true)
        private Integer factorId;

        @Schema(description = "是否叶子节点", example = "false")
        private Boolean isLeaf;

        @Schema(description = "排序顺序", example = "1")
        private Integer sortOrder;

        @Schema(description = "节点描述", example = "包含所有价值相关因子")
        private String description;

        @Schema(description = "场景ID", example = "EQUITY")
        private String sceneId;

        // Getters and Setters
        public Integer getNodeId() {
            return nodeId;
        }

        public void setNodeId(Integer nodeId) {
            this.nodeId = nodeId;
        }

        public Integer getParentId() {
            return parentId;
        }

        public void setParentId(Integer parentId) {
            this.parentId = parentId;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public String getNodeType() {
            return nodeType;
        }

        public void setNodeType(String nodeType) {
            this.nodeType = nodeType;
        }

        public Integer getFactorId() {
            return factorId;
        }

        public void setFactorId(Integer factorId) {
            this.factorId = factorId;
        }

        public Boolean getIsLeaf() {
            return isLeaf;
        }

        public void setIsLeaf(Boolean isLeaf) {
            this.isLeaf = isLeaf;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSceneId() {
            return sceneId;
        }

        public void setSceneId(String sceneId) {
            this.sceneId = sceneId;
        }
    }

    // Getters and Setters
    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }



    public DerivedFactorInfo getDerivedFactorInfo() {
        return derivedFactorInfo;
    }

    public void setDerivedFactorInfo(DerivedFactorInfo derivedFactorInfo) {
        this.derivedFactorInfo = derivedFactorInfo;
    }

    public WeightConfigInfo getWeightConfigInfo() {
        return weightConfigInfo;
    }

    public void setWeightConfigInfo(WeightConfigInfo weightConfigInfo) {
        this.weightConfigInfo = weightConfigInfo;
    }

    public TreeOperationInfo getTreeOperationInfo() {
        return treeOperationInfo;
    }

    public void setTreeOperationInfo(TreeOperationInfo treeOperationInfo) {
        this.treeOperationInfo = treeOperationInfo;
    }
}