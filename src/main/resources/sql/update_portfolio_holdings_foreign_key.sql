-- ============================================================
-- 更新 portfolio_holdings 表，添加基金表外键关联
-- ============================================================

-- 1. 检查并添加外键约束（关联 funds 表的 fund_code）
-- 注意：如果表中已有数据，需要确保 fund_code 在 funds 表中都存在

-- 先删除可能存在的旧外键（如果有）
-- ALTER TABLE portfolio_holdings DROP FOREIGN KEY IF EXISTS fk_holdings_fund;

-- 添加外键约束（关联 funds.fund_code）
ALTER TABLE portfolio_holdings
    ADD CONSTRAINT fk_holdings_fund 
    FOREIGN KEY (fund_code) REFERENCES funds(fund_code) 
    ON DELETE RESTRICT 
    ON UPDATE CASCADE;

-- 添加索引（提高查询性能）
CREATE INDEX IF NOT EXISTS idx_holdings_fund_code ON portfolio_holdings(fund_code);

-- 说明：
-- 1. fund_code 作为外键关联到 funds.fund_code（主键）
-- 2. fund_name 可以保留作为冗余字段，便于查询（避免频繁 JOIN）
-- 3. 或者可以删除 fund_name，通过 JOIN funds 表获取名称
-- 4. ON DELETE RESTRICT：如果基金被删除，不允许删除持仓（保护数据完整性）
-- 5. ON UPDATE CASCADE：如果基金代码更新，自动更新持仓表中的代码

