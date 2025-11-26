package com.fengqi.fund.fundadvisor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 * 数据库初始化组件
 * 在应用启动时自动创建缺失的表
 * 
 * @author fund-advisor
 * @since 2025-11-22
 */
@Slf4j
@Component
@Order(1)
public class DatabaseInitializer implements CommandLineRunner {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("开始检查数据库表结构...");
        initializeStyleTagTable();
        initializeFactorValidationTables();
        initializeLayeredBacktestTables();
        log.info("数据库表结构检查完成");
    }
    
    /**
     * 初始化 style_tag 表
     */
    private void initializeStyleTagTable() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "style_tag", null);
            
            if (!tables.next()) {
                // 表不存在，创建表
                log.info("检测到 style_tag 表不存在，开始创建...");
                String createTableSql = 
                    "CREATE TABLE style_tag (" +
                    "    tag_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '风格标签ID'," +
                    "    tag_name VARCHAR(50) NOT NULL COMMENT '风格标签名称（如：价值、成长、质量、低波动、动量）'," +
                    "    tag_code VARCHAR(50) NOT NULL UNIQUE COMMENT '风格标签编码（唯一标识，如：VALUE、GROWTH、QUALITY、LOW_VOLATILITY、MOMENTUM）'," +
                    "    description TEXT NULL COMMENT '标签描述（详细说明该风格标签的定义、特征和投资意义）'," +
                    "    create_user_id INT NULL COMMENT '创建人ID（记录风格标签的创建者信息，预留权限关联）'," +
                    "    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "    is_valid TINYINT(1) DEFAULT 1 COMMENT '是否有效（1=有效，0=失效）'," +
                    "    UNIQUE KEY uk_tag_code (tag_code)," +
                    "    UNIQUE KEY uk_tag_name (tag_name)," +
                    "    INDEX idx_valid (is_valid)," +
                    "    INDEX idx_create_time (create_time)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风格标签表：存储风格分类标签的定义和描述信息'";
                
                try (var statement = connection.createStatement()) {
                    statement.execute(createTableSql);
                    log.info("成功创建 style_tag 表");
                }
            } else {
                log.info("style_tag 表已存在，跳过创建");
            }
        } catch (Exception e) {
            log.error("初始化 style_tag 表失败", e);
            // 不抛出异常，避免阻止应用启动
        }
    }
    
    /**
     * 初始化因子检验相关表
     */
    private void initializeFactorValidationTables() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 检查并创建 factor_validation_task 表
            ResultSet taskTable = metaData.getTables(null, null, "factor_validation_task", null);
            if (!taskTable.next()) {
                log.info("检测到 factor_validation_task 表不存在，开始创建...");
                String createTaskTableSql = 
                    "CREATE TABLE factor_validation_task (" +
                    "    task_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '检验任务ID'," +
                    "    task_name VARCHAR(200) NOT NULL COMMENT '任务名称（用户自定义，如：价值因子IC检验）'," +
                    "    task_type VARCHAR(50) NOT NULL DEFAULT 'IC_IR' COMMENT '任务类型（IC_IR=IC/IR计算，LAYERED=分层回测）'," +
                    "    factor_ids VARCHAR(500) NOT NULL COMMENT '检验因子ID列表（多个用逗号分隔，如：1,2,3）'," +
                    "    start_date DATE NOT NULL COMMENT '回测开始日期'," +
                    "    end_date DATE NOT NULL COMMENT '回测结束日期'," +
                    "    task_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '任务状态（PENDING=待执行，RUNNING=执行中，SUCCESS=成功，FAILED=失败）'," +
                    "    progress INT DEFAULT 0 COMMENT '任务进度（0-100）'," +
                    "    error_message TEXT NULL COMMENT '错误信息（任务失败时记录）'," +
                    "    create_user_id INT NULL COMMENT '创建人ID'," +
                    "    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "    start_time DATETIME NULL COMMENT '开始执行时间'," +
                    "    end_time DATETIME NULL COMMENT '结束执行时间'," +
                    "    INDEX idx_status (task_status)," +
                    "    INDEX idx_create_time (create_time)," +
                    "    INDEX idx_user_id (create_user_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='因子检验任务表：存储因子检验任务的基本信息'";
                
                try (var statement = connection.createStatement()) {
                    statement.execute(createTaskTableSql);
                    log.info("成功创建 factor_validation_task 表");
                }
            } else {
                log.info("factor_validation_task 表已存在，跳过创建");
            }
            
            // 检查并创建 factor_ic_ir_result 表
            ResultSet resultTable = metaData.getTables(null, null, "factor_ic_ir_result", null);
            if (!resultTable.next()) {
                log.info("检测到 factor_ic_ir_result 表不存在，开始创建...");
                String createResultTableSql = 
                    "CREATE TABLE factor_ic_ir_result (" +
                    "    result_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '结果ID'," +
                    "    task_id INT NOT NULL COMMENT '关联任务ID'," +
                    "    factor_id INT NOT NULL COMMENT '因子ID（关联factor_derived.derived_id）'," +
                    "    factor_code VARCHAR(50) NULL COMMENT '因子编码（冗余字段，便于查询）'," +
                    "    factor_name VARCHAR(100) NULL COMMENT '因子名称（冗余字段，便于查询）'," +
                    "    ic_mean DECIMAL(10, 6) NULL COMMENT 'IC均值（Information Coefficient Mean）'," +
                    "    ic_std DECIMAL(10, 6) NULL COMMENT 'IC标准差'," +
                    "    ir_value DECIMAL(10, 6) NULL COMMENT 'IR值（Information Ratio = IC均值 / IC标准差）'," +
                    "    ic_positive_ratio DECIMAL(5, 4) NULL COMMENT 'IC正相关比例（IC>0的比例）'," +
                    "    ic_sequence TEXT NULL COMMENT 'IC序列（JSON格式，存储每日IC值）'," +
                    "    calculation_date DATE NOT NULL COMMENT '计算日期（回测结束日期）'," +
                    "    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "    INDEX idx_task_id (task_id)," +
                    "    INDEX idx_factor_id (factor_id)," +
                    "    INDEX idx_calculation_date (calculation_date)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='因子IC/IR计算结果表：存储每个因子的IC/IR计算结果'";
                
                try (var statement = connection.createStatement()) {
                    statement.execute(createResultTableSql);
                    log.info("成功创建 factor_ic_ir_result 表");
                }
            } else {
                log.info("factor_ic_ir_result 表已存在，跳过创建");
            }
            
            // 检查并创建 factor_ic_sequence 表
            ResultSet sequenceTable = metaData.getTables(null, null, "factor_ic_sequence", null);
            if (!sequenceTable.next()) {
                log.info("检测到 factor_ic_sequence 表不存在，开始创建...");
                String createSequenceTableSql = 
                    "CREATE TABLE factor_ic_sequence (" +
                    "    sequence_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '序列ID'," +
                    "    result_id INT NOT NULL COMMENT '关联结果ID'," +
                    "    trade_date DATE NOT NULL COMMENT '交易日期'," +
                    "    ic_value DECIMAL(10, 6) NOT NULL COMMENT '当日IC值'," +
                    "    rank_ic DECIMAL(10, 6) NULL COMMENT 'Rank IC值（可选）'," +
                    "    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "    INDEX idx_result_id (result_id)," +
                    "    INDEX idx_trade_date (trade_date)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='因子IC序列明细表：存储每日IC值明细'";
                
                try (var statement = connection.createStatement()) {
                    statement.execute(createSequenceTableSql);
                    log.info("成功创建 factor_ic_sequence 表");
                }
            } else {
                log.info("factor_ic_sequence 表已存在，跳过创建");
            }
        } catch (Exception e) {
            log.error("初始化因子检验表失败", e);
            // 不抛出异常，避免阻止应用启动
        }
    }
    
    /**
     * 初始化分层回测相关表
     */
    private void initializeLayeredBacktestTables() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 检查并创建 factor_layered_result 表
            ResultSet layeredResultTable = metaData.getTables(null, null, "factor_layered_result", null);
            if (!layeredResultTable.next()) {
                log.info("检测到 factor_layered_result 表不存在，开始创建...");
                String createLayeredResultTableSql = 
                    "CREATE TABLE factor_layered_result (" +
                    "    result_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '结果ID'," +
                    "    task_id INT NOT NULL COMMENT '关联任务ID'," +
                    "    factor_id INT NOT NULL COMMENT '因子ID（关联factor_derived.derived_id）'," +
                    "    factor_code VARCHAR(50) NULL COMMENT '因子编码（冗余字段，便于查询）'," +
                    "    factor_name VARCHAR(100) NULL COMMENT '因子名称（冗余字段，便于查询）'," +
                    "    quantile INT NOT NULL COMMENT '分位数（1-5，1=低分位组，5=高分位组）'," +
                    "    quantile_name VARCHAR(20) NOT NULL COMMENT '分位数名称（Q1,Q2,Q3,Q4,Q5）'," +
                    "    stock_count INT DEFAULT 0 COMMENT '股票数量'," +
                    "    weight_sum DECIMAL(10, 6) DEFAULT 1.000000 COMMENT '权重总和'," +
                    "    total_return DECIMAL(15, 6) DEFAULT 0.000000 COMMENT '总收益率'," +
                    "    annualized_return DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '年化收益率'," +
                    "    annualized_volatility DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '年化波动率'," +
                    "    sharpe_ratio DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '夏普比率'," +
                    "    max_drawdown DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '最大回撤'," +
                    "    win_rate DECIMAL(5, 4) DEFAULT 0.0000 COMMENT '胜率（正收益交易日比例）'," +
                    "    calmar_ratio DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '卡尔玛比率（年化收益/最大回撤）'," +
                    "    sortino_ratio DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '索提诺比率'," +
                    "    information_ratio DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '信息比率'," +
                    "    tracking_error DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '跟踪误差'," +
                    "    beta DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '贝塔系数'," +
                    "    alpha DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '阿尔法'," +
                    "    start_date DATE NOT NULL COMMENT '回测开始日期'," +
                    "    end_date DATE NOT NULL COMMENT '回测结束日期'," +
                    "    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "    INDEX idx_task_id (task_id)," +
                    "    INDEX idx_factor_id (factor_id)," +
                    "    INDEX idx_quantile (quantile)," +
                    "    INDEX idx_task_quantile (task_id, quantile)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分层回测结果表：存储每个分位组的回测统计指标'";
                
                try (var statement = connection.createStatement()) {
                    statement.execute(createLayeredResultTableSql);
                    log.info("成功创建 factor_layered_result 表");
                }
            } else {
                log.info("factor_layered_result 表已存在，跳过创建");
            }
            
            // 检查并创建 factor_layered_nav_series 表
            ResultSet navSeriesTable = metaData.getTables(null, null, "factor_layered_nav_series", null);
            if (!navSeriesTable.next()) {
                log.info("检测到 factor_layered_nav_series 表不存在，开始创建...");
                String createNavSeriesTableSql = 
                    "CREATE TABLE factor_layered_nav_series (" +
                    "    series_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '序列ID'," +
                    "    result_id INT NOT NULL COMMENT '关联结果ID'," +
                    "    trade_date DATE NOT NULL COMMENT '交易日期'," +
                    "    nav_value DECIMAL(15, 6) NOT NULL DEFAULT 1.000000 COMMENT '净值值'," +
                    "    daily_return DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '日收益率'," +
                    "    cumulative_return DECIMAL(15, 6) DEFAULT 0.000000 COMMENT '累计收益率'," +
                    "    stock_count INT DEFAULT 0 COMMENT '当期股票数量'," +
                    "    weight_sum DECIMAL(10, 6) DEFAULT 1.000000 COMMENT '权重总和'," +
                    "    turnover_rate DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '换手率'," +
                    "    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "    INDEX idx_result_id (result_id)," +
                    "    INDEX idx_trade_date (trade_date)," +
                    "    INDEX idx_result_date (result_id, trade_date)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分层回测净值序列表：存储每日净值和收益数据'";
                
                try (var statement = connection.createStatement()) {
                    statement.execute(createNavSeriesTableSql);
                    log.info("成功创建 factor_layered_nav_series 表");
                }
            } else {
                log.info("factor_layered_nav_series 表已存在，跳过创建");
            }
            
            // 检查并创建 factor_layered_holding_stats 表
            ResultSet holdingStatsTable = metaData.getTables(null, null, "factor_layered_holding_stats", null);
            if (!holdingStatsTable.next()) {
                log.info("检测到 factor_layered_holding_stats 表不存在，开始创建...");
                String createHoldingStatsTableSql = 
                    "CREATE TABLE factor_layered_holding_stats (" +
                    "    stats_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID'," +
                    "    result_id INT NOT NULL COMMENT '关联结果ID'," +
                    "    holding_period INT NOT NULL COMMENT '持有期（天）'," +
                    "    sample_count INT DEFAULT 0 COMMENT '样本数量'," +
                    "    mean_return DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '平均收益率'," +
                    "    std_return DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '收益率标准差'," +
                    "    min_return DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '最小收益率'," +
                    "    q1_return DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '第一四分位数收益率(Q1)'," +
                    "    median_return DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '中位数收益率(Q2)'," +
                    "    q3_return DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '第三四分位数收益率(Q3)'," +
                    "    max_return DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '最大收益率'," +
                    "    skewness DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '偏度'," +
                    "    kurtosis DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '峰度'," +
                    "    win_rate DECIMAL(5, 4) DEFAULT 0.0000 COMMENT '胜率（正收益比例）'," +
                    "    loss_rate DECIMAL(5, 4) DEFAULT 0.0000 COMMENT '亏损率'," +
                    "    profit_loss_ratio DECIMAL(10, 6) DEFAULT 0.000000 COMMENT '盈亏比'," +
                    "    max_consecutive_wins INT DEFAULT 0 COMMENT '最大连续盈利次数'," +
                    "    max_consecutive_losses INT DEFAULT 0 COMMENT '最大连续亏损次数'," +
                    "    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "    INDEX idx_result_id (result_id)," +
                    "    INDEX idx_holding_period (holding_period)," +
                    "    INDEX idx_result_period (result_id, holding_period)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分层回测持有期收益统计表：存储不同持有期的收益分布统计'";
                
                try (var statement = connection.createStatement()) {
                    statement.execute(createHoldingStatsTableSql);
                    log.info("成功创建 factor_layered_holding_stats 表");
                }
            } else {
                log.info("factor_layered_holding_stats 表已存在，跳过创建");
            }
            
        } catch (Exception e) {
            log.error("初始化分层回测表失败", e);
            // 不抛出异常，避免阻止应用启动
        }
    }
}

