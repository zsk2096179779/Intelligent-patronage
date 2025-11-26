-- 分位数累计收益率曲线数据表
CREATE TABLE IF NOT EXISTS factor_quantile_return_curve (
    curve_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '曲线ID',
    task_id INT NOT NULL COMMENT '关联任务ID',
    factor_id INT NOT NULL COMMENT '因子ID',
    factor_code VARCHAR(50) NULL COMMENT '因子编码',
    factor_name VARCHAR(100) NULL COMMENT '因子名称',
    dates_json TEXT NOT NULL COMMENT '日期列表（JSON格式）',
    quantile_returns_json TEXT NOT NULL COMMENT '各分位数累计收益率序列（JSON格式，格式：{"1":[...], "2":[...], ...}）',
    quantile_descriptions_json TEXT NULL COMMENT '分位数描述（JSON格式）',
    calculation_date DATE NOT NULL COMMENT '计算日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_task_factor (task_id, factor_id),
    INDEX idx_task_id (task_id),
    INDEX idx_factor_id (factor_id),
    FOREIGN KEY (task_id) REFERENCES factor_validation_task(task_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分位数累计收益率曲线数据表';

