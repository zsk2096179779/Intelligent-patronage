-- 策略再平衡回测结果表
-- 如果表不存在，可以执行此 SQL 创建表

CREATE TABLE IF NOT EXISTS `strategy_rebalance_backtest` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `strategy_id` BIGINT NOT NULL,
    `cumulative_return` DECIMAL(5, 2) NOT NULL COMMENT '累计收益（百分比）',
    `max_drawdown` DECIMAL(5, 2) NOT NULL COMMENT '最大回撤（百分比）',
    `sharpe_ratio` DECIMAL(5, 2) NOT NULL COMMENT '夏普比率',
    `trades` INT NOT NULL COMMENT '交易次数',
    `run_at` DATETIME NOT NULL COMMENT '回测执行时间',
    FOREIGN KEY (`strategy_id`) REFERENCES `strategy`(`id`) ON DELETE CASCADE,
    INDEX `idx_strategy_id` (`strategy_id`),
    INDEX `idx_run_at` (`run_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='策略再平衡回测结果表';

