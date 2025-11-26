-- ============================================================
-- 为 portfolios 表添加收益指标字段
-- 这些字段用于存储组合产品的实际运行数据
-- 执行方法：在MySQL客户端中执行此脚本
-- ============================================================

-- 为 portfolios 表添加收益指标字段
-- 注意：如果MySQL版本低于8.0.19，请移除 IF NOT EXISTS 关键字

ALTER TABLE portfolios
    ADD COLUMN return_rate DECIMAL(10,4) NULL COMMENT '策略收益（%）' AFTER target_investor,
    ADD COLUMN annual_return DECIMAL(10,4) NULL COMMENT '年化收益（%）' AFTER return_rate,
    ADD COLUMN max_drawdown DECIMAL(10,4) NULL COMMENT '最大回撤（%）' AFTER annual_return,
    ADD COLUMN sharpe_ratio DECIMAL(10,4) NULL COMMENT '夏普比率' AFTER max_drawdown,
    ADD COLUMN volatility DECIMAL(10,4) NULL COMMENT '波动率（%）' AFTER sharpe_ratio,
    ADD COLUMN win_rate DECIMAL(10,4) NULL COMMENT '胜率（%）' AFTER volatility;

-- 如果上述语句因为字段已存在而报错，请使用以下语句（逐个添加）：
-- ALTER TABLE portfolios ADD COLUMN return_rate DECIMAL(10,4) NULL COMMENT '策略收益（%）' AFTER target_investor;
-- ALTER TABLE portfolios ADD COLUMN annual_return DECIMAL(10,4) NULL COMMENT '年化收益（%）' AFTER return_rate;
-- ALTER TABLE portfolios ADD COLUMN max_drawdown DECIMAL(10,4) NULL COMMENT '最大回撤（%）' AFTER annual_return;
-- ALTER TABLE portfolios ADD COLUMN sharpe_ratio DECIMAL(10,4) NULL COMMENT '夏普比率' AFTER max_drawdown;
-- ALTER TABLE portfolios ADD COLUMN volatility DECIMAL(10,4) NULL COMMENT '波动率（%）' AFTER sharpe_ratio;
-- ALTER TABLE portfolios ADD COLUMN win_rate DECIMAL(10,4) NULL COMMENT '胜率（%）' AFTER volatility;

-- 说明：
-- 1. return_rate: 策略收益（%），可以是累计收益率
-- 2. annual_return: 年化收益（%），基于时间周期计算的年化收益率
-- 3. max_drawdown: 最大回撤（%），历史上最大的跌幅
-- 4. sharpe_ratio: 夏普比率，风险调整后的收益指标
-- 5. volatility: 波动率（%），收益的标准差
-- 6. win_rate: 胜率（%），盈利交易占总交易的比例
--
-- 这些字段初始值为 NULL，表示暂无数据或尚未开始运行
-- 后续可以通过定时任务或手动更新这些指标
