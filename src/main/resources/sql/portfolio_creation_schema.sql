-- ============================================================
-- 组合创建完整流程 - 数据库设计脚本（MySQL 8+）
-- 执行顺序：先确认 portfolios 表已存在，再运行本脚本
-- ============================================================

-- 1. 扩展 portfolios 表，支持组合创建流程的额外字段
ALTER TABLE portfolios
    ADD COLUMN IF NOT EXISTS summary TEXT NULL COMMENT '组合简介/亮点' AFTER strategy_type,
    ADD COLUMN IF NOT EXISTS target_investor VARCHAR(200) NULL COMMENT '目标客户/适用人群' AFTER summary,
    ADD COLUMN IF NOT EXISTS cover_image_url VARCHAR(255) NULL COMMENT '展示封面图' AFTER target_investor,
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'draft' COMMENT 'draft/pending_review/approved/rejected' AFTER listed,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) NULL COMMENT '创建人' AFTER status,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100) NULL COMMENT '最后修改人' AFTER created_by,
    ADD COLUMN IF NOT EXISTS created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER updated_by,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER created_at;

-- 2. 产品参数表（费率、金额、开放规则等）
CREATE TABLE IF NOT EXISTS portfolio_product_params (
    id INT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id INT NOT NULL COMMENT '关联 portfolios.id',
    min_invest_amount DECIMAL(16,2) NULL COMMENT '最低投资金额',
    max_invest_amount DECIMAL(16,2) NULL COMMENT '最高投资金额/规模上限',
    subscription_fee DECIMAL(6,4) NULL COMMENT '申购费率 (%)',
    redemption_fee DECIMAL(6,4) NULL COMMENT '赎回费率 (%)',
    management_fee DECIMAL(6,4) NULL COMMENT '管理费率 (%)',
    performance_fee DECIMAL(6,4) NULL COMMENT '业绩报酬费率 (%)',
    lockup_period INT NULL COMMENT '锁定期（天）',
    open_day_rule VARCHAR(100) NULL COMMENT '开放日规则描述',
    redemption_rule VARCHAR(100) NULL COMMENT '赎回规则描述',
    other_params JSON NULL COMMENT '其他产品参数（JSON）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_portfolio_product UNIQUE (portfolio_id),
    CONSTRAINT fk_product_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组合产品参数表';

-- 3. 策略/风控参数表
CREATE TABLE IF NOT EXISTS portfolio_strategy_params (
    id INT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id INT NOT NULL COMMENT '关联 portfolios.id',
    strategy_template_id INT NULL COMMENT '策略模板ID（可选）',
    rebalance_frequency VARCHAR(50) NULL COMMENT '调仓频率（如：monthly/weekly）',
    rebalance_trigger VARCHAR(100) NULL COMMENT '调仓触发条件描述',
    max_drawdown_limit DECIMAL(6,4) NULL COMMENT '最大回撤阈值 (%)',
    volatility_limit DECIMAL(6,4) NULL COMMENT '波动率限制 (%)',
    position_limit DECIMAL(6,4) NULL COMMENT '仓位上限 (%)',
    cash_min_ratio DECIMAL(6,4) NULL COMMENT '现金最小占比 (%)',
    leverage_allowed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否允许杠杆 1-是 0-否',
    stop_loss_limit DECIMAL(6,4) NULL COMMENT '止损阈值 (%)',
    additional_constraints JSON NULL COMMENT '其他策略/风控约束 (JSON)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_portfolio_strategy UNIQUE (portfolio_id),
    CONSTRAINT fk_strategy_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组合策略与风控参数表';

-- 4. 组合持仓表（支持多只基金/证券）
CREATE TABLE IF NOT EXISTS portfolio_holdings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id INT NOT NULL COMMENT '关联 portfolios.id',
    fund_code VARCHAR(30) NOT NULL COMMENT '基金/证券代码',
    fund_name VARCHAR(200) NULL COMMENT '基金/证券名称',
    weight DECIMAL(6,4) NOT NULL COMMENT '当前权重 (%)',
    benchmark_weight DECIMAL(6,4) NULL COMMENT '基准/目标权重 (%)',
    min_weight DECIMAL(6,4) NULL COMMENT '权重下限 (%)',
    max_weight DECIMAL(6,4) NULL COMMENT '权重上限 (%)',
    rebalance_priority INT NULL COMMENT '调仓优先级（值越小越优先）',
    remark VARCHAR(255) NULL COMMENT '备注信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_portfolio_holdings UNIQUE (portfolio_id, fund_code),
    CONSTRAINT fk_holdings_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组合持仓明细表';

-- 5. 组合附件/文档表
CREATE TABLE IF NOT EXISTS portfolio_documents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id INT NOT NULL COMMENT '关联 portfolios.id',
    doc_type VARCHAR(50) NOT NULL COMMENT '文档类型（如：risk_disclosure, statement, others）',
    doc_name VARCHAR(200) NOT NULL COMMENT '文档名称',
    doc_url VARCHAR(500) NOT NULL COMMENT '文档存储地址（OSS路径或URL）',
    file_size BIGINT NULL COMMENT '文件大小（字节）',
    uploaded_by VARCHAR(100) NULL COMMENT '上传人',
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(255) NULL COMMENT '备注',
    CONSTRAINT fk_docs_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组合文档/附件表';

-- 6. 组合审核记录表
CREATE TABLE IF NOT EXISTS portfolio_audit_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id INT NOT NULL COMMENT '关联 portfolios.id',
    from_status VARCHAR(30) NULL COMMENT '变更前状态',
    to_status VARCHAR(30) NOT NULL COMMENT '变更后状态',
    reviewer VARCHAR(100) NULL COMMENT '审核人/操作人',
    opinion TEXT NULL COMMENT '审核意见',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT idx_audit_portfolio (portfolio_id),
    CONSTRAINT fk_audit_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组合审核记录';

-- 7. 组合创建步骤完成情况（可选，用于前端多步骤向导）
CREATE TABLE IF NOT EXISTS portfolio_step_progress (
    id INT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id INT NOT NULL COMMENT '关联 portfolios.id',
    basic_info_completed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '基础信息是否完成',
    product_params_completed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '产品参数是否完成',
    strategy_params_completed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '策略参数是否完成',
    holdings_completed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '持仓配置是否完成',
    documents_completed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '文档上传是否完成',
    last_step VARCHAR(50) NULL COMMENT '最后停留的步骤标识',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_portfolio_step UNIQUE (portfolio_id),
    CONSTRAINT fk_step_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组合创建步骤完成情况';

-- ============================================================
-- 运行完毕后，可手动检查各表结构，并根据需要微调字段
-- ============================================================

