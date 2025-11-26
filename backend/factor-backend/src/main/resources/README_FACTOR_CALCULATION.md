# 基金因子计算系统

## 📋 概述

本系统为基金顾问系统提供了完整的因子计算解决方案，支持基础因子的公式化计算和衍生因子的加权组合。系统采用标准化的公式格式，提供灵活的计算策略和完整的管理功能。

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                    基金因子计算系统                                │
├─────────────────────────────────────────────────────────────────┤
│  表现层 (Controllers)                                      │
│  ├── FactorCalculationController     - 因子计算接口            │
│  ├── FactorFormulaController        - 公式管理接口            │
│  └── FactorValidationController     - 因子验证接口            │
├─────────────────────────────────────────────────────────────────┤
│  服务层 (Services)                                          │
│  ├── FactorCalculationEngine         - 计算引擎              │
│  ├── DerivedFactorCalculator         - 衍生因子计算器         │
│  ├── DataProviderService             - 数据提供服务            │
│  └── FactorFormulaParser            - 公式解析器            │
├─────────────────────────────────────────────────────────────────┤
│  数据访问层 (Mappers)                                        │
│  ├── FactorBaseMapper               - 基础因子数据访问      │
│  ├── DerivedFactorMapper            - 衍生因子数据访问      │
│  └── FactorValidationMapper         - 验证结果数据访问      │
├─────────────────────────────────────────────────────────────────┤
│  数据存储层 (Database)                                        │
│  ├── factor_base                     - 基础因子表          │
│  ├── factor_derived                  - 衍生因子表          │
│  ├── derived_factors                 - 衍生因子权重表      │
│  └── calc_strategies                 - 计算策略表          │
└─────────────────────────────────────────────────────────────────┘
```

## 🎯 核心功能

### 1. 基础因子计算
- ✅ **标准公式解析**：支持标准格式 `FACTOR_NAME:表达式:数据字段:过滤条件:聚合方式`
- ✅ **实时计算**：支持单股票和批量股票的实时因子计算
- ✅ **数据质量评估**：自动评估计算结果的完整性和准确性
- ✅ **多数据源支持**：集成Wind、Tushare等主流数据源

### 2. 衍生因子计算
- ✅ **多基础因子组合**：支持多个基础因子的加权组合
- ✅ **多种计算策略**：归一化、等权、市值加权、风险调整等
- ✅ **自定义权重**：灵活的权重配置和验证
- ✅ **实时计算**：支持实时衍生因子值计算

### 3. 公式管理
- ✅ **公式验证**：语法检查和逻辑验证
- ✅ **批量导入**：支持CSV/Excel格式的公式批量导入
- ✅ **模板管理**：提供标准公式模板
- ✅ **兼容性检查**：检查公式与系统兼容性

### 4. 因子验证
- ✅ **IC/IR计算**：自动计算信息系数和信息比率
- ✅ **分层回测**：支持因子分层回测
- ✅ **可视化展示**：IC走势图、分层净值图
- ✅ **批量验证**：支持多因子批量验证

## 📊 标准公式格式

### 基本格式
```
FACTOR_NAME: [计算表达式] : [数据字段] : [过滤条件] : [聚合方式]
```

### 示例公式

#### 估值类因子
```
# 市盈率TTM
PE_TTM: (CLOSE_PRICE / EPS_TTM) : CLOSE_PRICE, EPS_TTM : EPS_TTM > 0 : LAST

# 市净率
PB: (CLOSE_PRICE / BVPS) : CLOSE_PRICE, BVPS : BVPS > 0 : LAST
```

#### 成长类因子
```
# 营收增长率
REVENUE_GROWTH: (REVENUE_CURRENT / REVENUE_LAG1 - 1) * 100 : REVENUE_CURRENT, REVENUE_LAG1 : REVENUE_LAG1 > 0 : LAST

# 净利润增长率
PROFIT_GROWTH: (NET_PROFIT_CURRENT / NET_PROFIT_LAG1 - 1) * 100 : NET_PROFIT_CURRENT, NET_PROFIT_LAG1 : NET_PROFIT_LAG1 > 0 : LAST
```

#### 质量类因子
```
# 净资产收益率
ROE: (NET_PROFIT / NET_ASSETS) * 100 : NET_PROFIT, NET_ASSETS : NET_ASSETS > 0 : LAST

# 毛利率
GROSS_MARGIN: ((REVENUE - COST) / REVENUE) * 100 : REVENUE, COST : REVENUE > 0 : LAST
```

#### 动量类因子
```
# 12个月收益率
RETURN_12M: (CLOSE_PRICE / CLOSE_PRICE_250D - 1) * 100 : CLOSE_PRICE, CLOSE_PRICE_250D : CLOSE_PRICE_250D > 0 : LAST

# 动量因子
MOMENTUM: RANK(RETURN_6M) : RETURN_6M : RETURN_6M IS NOT NULL : LAST
```

## 🚀 快速开始

### 1. 导入基础因子公式

```sql
-- 执行示例SQL脚本
source src/main/resources/db/sample_base_factors.sql;
```

### 2. 计算基础因子

```bash
# 单股票计算
curl -X POST "http://localhost:8080/api/factor/calculation/base-factor/1?stockCode=000001&calculateDate=2024-01-15"

# 批量计算
curl -X POST "http://localhost:8080/api/factor/calculation/base-factor/1/batch?calculateDate=2024-01-15" \
  -H "Content-Type: application/json" \
  -d '["000001", "000002", "000858"]'
```

### 3. 创建衍生因子

```bash
# 创建衍生因子
curl -X POST "http://localhost:8080/api/factor/factor-management/derived-factors/basic" \
  -H "Content-Type: application/json" \
  -d '{
    "operationType": "CREATE_DERIVED_FACTOR",
    "derivedFactorInfo": {
      "factorName": "估值质量复合因子",
      "factorCode": "VAL_QUALITY_COM",
      "factorDesc": "PE与ROE的组合因子",
      "calcStrategyId": 1,
      "baseFactorIds": [1, 2]
    },
    "weightConfigInfo": {
      "baseFactorWeights": "{\"1\":0.6,\"2\":0.4}"
    }
  }'
```

### 4. 验证因子公式

```bash
# 验证公式
curl -X POST "http://localhost:8080/api/factor/calculation/formula/validate" \
  -H "Content-Type: application/json" \
  -d '"PE_TTM: (CLOSE_PRICE / EPS_TTM) : CLOSE_PRICE, EPS_TTM : EPS_TTM > 0 : LAST"'
```

## 📈 计算策略

### 1. 归一化处理后的加权组合 (ID=1)
- **说明**：对基础因子进行标准化处理，然后按权重组合
- **适用场景**：因子量纲差异较大时
- **计算公式**：`Σ(标准化值 × 权重)`

### 2. 简单等权平均 (ID=2)
- **说明**：所有基础因子权重相等，简单平均
- **适用场景**：各因子重要性相同时
- **计算公式**：`(因子1 + 因子2 + ... + 因子n) / n`

### 3. 市值加权 (ID=3)
- **说明**：按股票市值分配因子权重
- **适用场景**：需要考虑市值影响的策略
- **计算公式**：`Σ(因子值 × 市值权重)`

### 4. 风险调整加权 (ID=4)
- **说明**：考虑因子风险调整的权重分配
- **适用场景**：风险敏感型策略
- **计算公式**：`Σ(因子值 × 风险调整权重)`

## 🔧 支持的数据字段

### 价格数据
- `CLOSE_PRICE`: 收盘价
- `OPEN_PRICE`: 开盘价
- `HIGH_PRICE`: 最高价
- `LOW_PRICE`: 最低价
- `CLOSE_PRICE_20D`: 20日前收盘价
- `CLOSE_PRICE_60D`: 60日前收盘价
- `CLOSE_PRICE_250D`: 250日前收盘价

### 财务数据
- `EPS_TTM`: 过去12个月每股收益
- `BVPS`: 每股净资产
- `RPS`: 每股销售收入
- `DPS`: 每股股息

### 财务报表数据
- `REVENUE_CURRENT`: 当期营业收入
- `REVENUE_LAG1`: 上一期营业收入
- `NET_PROFIT_CURRENT`: 当期净利润
- `NET_PROFIT_LAG1`: 上一期净利润
- `NET_ASSETS`: 净资产
- `TOTAL_ASSETS`: 总资产

### 技术指标数据
- `RETURN_6M`: 6个月收益率
- `RANK_PB`: PB排名
- `STDDEV_*`: 标准差
- `RANK_*`: 排名值

## 📊 API接口总览

### 因子计算接口
- `POST /api/factor/calculation/base-factor/{id}` - 计算基础因子
- `POST /api/factor/calculation/base-factor/{id}/batch` - 批量计算基础因子
- `POST /api/factor/calculation/derived-factor/{id}` - 计算衍生因子
- `POST /api/factor/calculation/preview` - 预览计算结果

### 公式管理接口
- `POST /api/factor/calculation/formula/validate` - 验证公式
- `POST /api/factor/calculation/formula/parse` - 解析公式
- `POST /api/factor/formula/import` - 导入公式
- `GET /api/factor/formula/template` - 获取公式模板
- `GET /api/factor/formula/elements` - 获取支持的元素

### 因子管理接口
- `POST /api/factor/factor-management/derived-factors/basic` - 创建衍生因子
- `PUT /api/factor/factor-management/derived-factors/{id}` - 更新衍生因子
- `DELETE /api/factor/factor-management/derived-factors/{id}` - 删除衍生因子
- `GET /api/factor/factor-management/derived-factors` - 查询衍生因子

### 因子验证接口
- `POST /api/factor/validation/tasks` - 创建验证任务
- `GET /api/factor/validation/tasks/{id}/status` - 查询任务状态
- `GET /api/factor/validation/tasks/{id}/results` - 查询验证结果

## 🛠️ 开发指南

### 1. 添加新的基础因子

```java
// 1. 在factor_base表中插入新因子
INSERT INTO factor_base (factor_name, factor_code, factor_formula, ...) VALUES (...);

// 2. 在DataProviderService中添加数据获取逻辑
public Map<String, Object> getSingleDayData(String stockCode, String date, List<String> fields) {
    // 添加新字段的数据获取逻辑
}

// 3. 在公式解析器中添加新字段支持
private static final Set<String> SUPPORTED_FIELDS = Set.of(
    // 添加新字段
    "NEW_FIELD"
);
```

### 2. 添加新的计算策略

```java
// 在DerivedFactorCalculatorImpl中添加新策略
private BigDecimal calculateDerivedValue(...) {
    switch (calcStrategyId) {
        case 5: // 新策略ID
            return calculateCustomStrategy(baseFactorResults, normalizedWeights);
        default:
            return calculateNormalizedWeightedSum(baseFactorResults, normalizedWeights);
    }
}
```

### 3. 扩展数据源

```java
// 实现新的数据提供服务
@Service
public class CustomDataProviderServiceImpl implements DataProviderService {
    // 实现数据获取逻辑
}
```

## 📋 测试

### 运行单元测试
```bash
mvn test -Dtest=FactorCalculationEngineTest
```

### 运行集成测试
```bash
mvn test -Dtest=FactorCalculationIntegrationTest
```

### 测试覆盖率
```bash
mvn jacoco:report
```

## 📊 性能优化

### 1. 缓存策略
- **计算结果缓存**：相同参数的计算结果缓存1小时
- **数据缓存**：基础数据缓存15分钟
- **公式缓存**：解析后的公式上下文永久缓存

### 2. 批量优化
- **并行计算**：多股票并行计算
- **批量数据获取**：一次获取多只股票数据
- **预计算**：常用因子预计算

### 3. 数据库优化
- **索引优化**：关键字段建立索引
- **分区表**：按时间分区存储历史数据
- **连接池**：数据库连接池优化

## 🔒 安全考虑

### 1. 数据安全
- **敏感数据加密**：用户信息加密存储
- **访问控制**：API访问权限控制
- **数据脱敏**：日志中敏感信息脱敏

### 2. 计算安全
- **公式注入防护**：防止SQL注入和代码注入
- **资源限制**：计算资源使用限制
- **异常处理**：完善的异常处理机制

## 📈 监控和运维

### 1. 关键指标
- **计算成功率**：因子计算的成功率
- **平均响应时间**：API响应时间
- **数据质量**：数据完整性指标
- **系统负载**：CPU、内存使用率

### 2. 日志记录
- **计算日志**：详细记录计算过程
- **错误日志**：异常和错误信息
- **性能日志**：性能指标记录
- **访问日志**：API访问记录

## 📚 参考资料

- [因子计算公式标准规范](src/main/resources/doc/factor-formula-standard.md)
- [系统使用指南](src/main/resources/doc/factor-calculation-usage.md)
- [API文档](docs/api/FactorCalculation.md)
- [数据库设计文档](docs/database/DatabaseDesign.md)

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 👥 开发团队

- **架构师** - 负责系统架构设计
- **后端开发** - 负责核心算法和API开发
- **数据工程师** - 负责数据接入和处理
- **量化研究员** - 负责因子模型设计
- **测试工程师** - 负责系统测试和质量保证

## 📞 支持

如有问题或建议，请联系：
- 邮箱：fund-advisor@example.com
- 问题反馈：[GitHub Issues](https://github.com/your-org/fund-advisor/issues)
- 技术文档：[Wiki](https://github.com/your-org/fund-advisor/wiki)