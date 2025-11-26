create table asset_allocation
(
    asset_id    int auto_increment
        primary key,
    name        varchar(100) null,
    benchmark   varchar(100) null,
    feature     varchar(200) null,
    audience    varchar(100) null,
    method      varchar(50)  null,
    weight_type varchar(50)  null,
    fund_code   varchar(30)  null,
    risk_level  varchar(50)  null
)
    collate = utf8mb4_unicode_ci;

create table asset_allocation_list
(
    listid        int auto_increment
        primary key,
    allocation_id int         null,
    fund_code     varchar(30) null,
    weight        double      null
)
    collate = utf8mb4_unicode_ci;

create table category_factors
(
    category_id int auto_increment
        primary key,
    name        varchar(100) null,
    description varchar(200) null,
    factor_list json         null
)
    collate = utf8mb4_unicode_ci;

create table constraint_groups
(
    groupid          int auto_increment
        primary key,
    index_name       varchar(100) null,
    reference_metric varchar(100) null,
    ma_reference     varchar(50)  null,
    buy_limit        int          null,
    sell_limit       int          null,
    user_id          int          null
)
    collate = utf8mb4_unicode_ci;

create table constraint_items
(
    itemid             int auto_increment
        primary key,
    group_id           int          null,
    type               varchar(20)  null,
    param_name         varchar(100) null,
    operator           varchar(20)  null,
    threshold          double       null,
    condition_relation varchar(10)  null
)
    collate = utf8mb4_unicode_ci;

create table custom_indexes
(
    customid  int auto_increment
        primary key,
    index_id  int         null,
    fund_code varchar(30) null,
    weight    double      null,
    fund_type varchar(50) null
)
    collate = utf8mb4_unicode_ci;

create table custom_style_factor_mix
(
    mixid           int auto_increment
        primary key,
    style_factor_id int        null,
    factor_id       int        null,
    weight          double     null,
    normalized      tinyint(1) null
)
    collate = utf8mb4_unicode_ci;

create table custom_style_factors
(
    styleid          int auto_increment
        primary key,
    name             varchar(100) null,
    display_name     varchar(100) null,
    description      varchar(200) null,
    calc_method      varchar(200) null,
    create_time      datetime     null,
    update_frequency varchar(50)  null,
    enabled          tinyint(1)   null,
    style_tag        varchar(50)  null
)
    collate = utf8mb4_unicode_ci;

create table customer_portfolios
(
    id             int auto_increment
        primary key,
    customer_id    int      null,
    portfolio_id   int      null,
    effective_time datetime null
)
    collate = utf8mb4_unicode_ci;

create table derived_factors
(
    factorid   int auto_increment
        primary key,
    derived_id int    null,
    base_id    int    null,
    weight     double null
)
    collate = utf8mb4_unicode_ci;

create table factor_definitions
(
    definitionid     int auto_increment
        primary key,
    name             varchar(100) null,
    display_name     varchar(100) null,
    factor_type      varchar(50)  null,
    data_type        varchar(50)  null,
    calc_method      varchar(200) null,
    update_frequency varchar(50)  null,
    enabled          tinyint(1)   null
)
    collate = utf8mb4_unicode_ci;

create table factor_tree
(
    treeid      int auto_increment
        primary key,
    parent_id   int          null,
    node_name   varchar(100) null,
    node_type   varchar(20)  null,
    factor_id   int          null,
    is_leaf     tinyint(1)   null,
    sort_order  int          null,
    description text         null
)
    collate = utf8mb4_unicode_ci;

create table fof_portfolio
(
    fofid                    int auto_increment
        primary key,
    portfolio_name           varchar(100) null,
    benchmark_index          varchar(100) null,
    portfolio_feature        varchar(200) null,
    target_audience          varchar(100) null,
    allocation_method        varchar(50)  null,
    weighting_scheme         varchar(50)  null,
    representative_fund_code varchar(30)  null,
    risk_rating              varchar(50)  null
)
    collate = utf8mb4_unicode_ci;

create table fund_alerts
(
    alertid      int auto_increment
        primary key,
    fund_code    varchar(30)  null,
    alert_param  varchar(100) null,
    trigger_date date         null,
    actual_value double       null,
    threshold    double       null,
    description  varchar(200) null,
    status       tinyint      null,
    create_time  datetime     null
)
    collate = utf8mb4_unicode_ci;

create table fund_announcements
(
    announcementid int auto_increment
        primary key,
    fund_code      varchar(30)  null,
    title          varchar(255) null,
    type           varchar(50)  null,
    url            varchar(200) null,
    pub_date       date         null,
    summary        varchar(200) null,
    source         varchar(100) null
)
    collate = utf8mb4_unicode_ci;

create table fund_companies
(
    company_name       varchar(100) not null
        primary key,
    establishment_date date         null,
    registered_capital double       null,
    first_fund_date    date         null,
    manager_count      int          null,
    fund_count         int          null,
    equity_capital     double       null,
    effective_assets   double       null,
    equity_return      double       null,
    bond_return        double       null,
    company_id         int          null
)
    collate = utf8mb4_unicode_ci;

create table fund_core_metrics
(
    coreid          int auto_increment
        primary key,
    fund_code       varchar(30) null,
    stat_date       date        null,
    return_1m       double      null,
    return_ytd      double      null,
    max_drawdown_1y double      null,
    annual_sharpe   double      null,
    risk_level      varchar(20) null,
    quality_score   double      null,
    risk_adj_score  double      null,
    rating          varchar(20) null,
    asset_score     double      null,
    research_score  double      null,
    risk_mgmt_score double      null,
    tenure_score    double      null
)
    collate = utf8mb4_unicode_ci;

create table fund_factor_mapping
(
    id           int auto_increment
        primary key,
    fund_id      varchar(20) null,
    factor_names json        null
)
    collate = utf8mb4_unicode_ci;

create table fund_holdings
(
    holdingid       int auto_increment
        primary key,
    fund_code       varchar(30)  null,
    stock_code      varchar(20)  null,
    stock_name      varchar(100) null,
    ratio           double       null,
    market_value    double       null,
    share_count     double       null,
    disclosure_date date         null,
    ranking         int          null,
    industry_tag    varchar(50)  null
)
    collate = utf8mb4_unicode_ci;

create table fund_index_mapping
(
    fund_id     varchar(20) not null
        primary key,
    index_names json        null
)
    collate = utf8mb4_unicode_ci;

create table fund_managers
(
    manager_name      varchar(100) not null
        primary key,
    company_name      varchar(100) null,
    managed_assets    double       null,
    managed_count     int          null,
    highest_education varchar(50)  null,
    tenure_years      int          null,
    effective_assets  double       null,
    equity_return     double       null,
    bond_return       double       null,
    annualized_return double       null,
    win_rate          double       null,
    manager_id        int          null
)
    collate = utf8mb4_unicode_ci;

create table fund_nav_history
(
    historyid       int auto_increment
        primary key,
    fund_code       varchar(30) null,
    nav_date        date        null,
    unit_nav        double      null,
    accumulated_nav double      null,
    daily_return    double      null,
    share_total     double      null
)
    collate = utf8mb4_unicode_ci;

create table fund_tags
(
    tagid     int auto_increment
        primary key,
    fund_code varchar(30) null,
    tag       varchar(50) null
)
    collate = utf8mb4_unicode_ci;

create table fund_watchlist
(
    watchlistid int auto_increment
        primary key,
    fund_code   varchar(30)  null,
    reason      varchar(200) null,
    alert_flag  tinyint(1)   null
)
    collate = utf8mb4_unicode_ci;

create table funds
(
    fund_code        varchar(20)   not null
        primary key,
    fund_name        varchar(100)  not null,
    fund_description varchar(1000) null,
    manager_id       int           not null,
    company_id       int           not null,
    fund_type        varchar(50)   null,
    category         varchar(50)   null,
    operation_cycle  varchar(50)   null,
    fund_size        double        null,
    inception_date   date          null,
    fee_rate         double        null,
    stock_asset      double        null,
    cash_asset       double        null,
    bond_asset       double        null,
    deposit_asset    double        null,
    proportion       double        null
)
    collate = utf8mb4_unicode_ci;

create table index_definitions
(
    definitionid int auto_increment
        primary key,
    name         varchar(100) null,
    code         varchar(50)  null,
    category     varchar(50)  null,
    index_type   varchar(50)  null,
    enabled      tinyint(1)   null,
    description  varchar(500) null
)
    collate = utf8mb4_unicode_ci;

create table index_tree
(
    indexid     int auto_increment
        primary key,
    parent_id   int          null,
    node_name   varchar(100) null,
    node_type   varchar(20)  null,
    index_id    int          null,
    is_leaf     tinyint(1)   null,
    sort_order  int          null,
    description varchar(200) null
)
    collate = utf8mb4_unicode_ci;

create table plans
(
    plan_id    int auto_increment
        primary key,
    plan_name  varchar(100) not null,
    index_list json         not null,
    start_date date         null,
    end_date   date         null
)
    collate = utf8mb4_unicode_ci;

create table portfolios
(
    id            int auto_increment
        primary key,
    name          varchar(100) null,
    risk_level    varchar(50)  null,
    strategy_type varchar(50)  null,
    strategy_id   int          null,
    listed        tinyint(1)   null
)
    collate = utf8mb4_unicode_ci;

create table rebalance_config
(
    configid     int auto_increment
        primary key,
    auto         tinyint(1)  null,
    strategy_id  int         null,
    trigger_type varchar(50) null,
    change_ratio double      null,
    frequency    varchar(50) null
)
    collate = utf8mb4_unicode_ci;

create table rebalance_details
(
    rebalanceid int auto_increment
        primary key,
    task_id     int         null,
    fund_code   varchar(30) null,
    old_weight  double      null,
    new_weight  double      null,
    diff        double      null
)
    collate = utf8mb4_unicode_ci;

create table rebalance_tasks
(
    id           int auto_increment
        primary key,
    strategy_id  int          null,
    trigger_time datetime     null,
    task_type    varchar(50)  null,
    execute_time datetime     null,
    reason       varchar(500) null,
    operator     varchar(100) null
)
    collate = utf8mb4_unicode_ci;

create table settlement_order1s
(
    id             bigint auto_increment comment '主键 ID'
        primary key,
    fund_code      varchar(20)                        not null comment '基金代码',
    fund_name      varchar(100)                       not null comment '基金名称',
    portfolio_id   int                                not null comment '组合 ID',
    portfolio_name varchar(100)                       not null comment '组合名称',
    batch_no       varchar(50)                        not null comment '批次号',
    trade_order_id int                                not null comment '关联的交易订单 ID',
    created_at     datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '交割单表';

create table settlement_orders
(
    id             int auto_increment
        primary key,
    batch_no       varchar(50)  null,
    portfolio_name varchar(100) null,
    portfolio_id   int          null,
    fund_name      varchar(100) null,
    fund_code      varchar(30)  null,
    ratio          double       null
)
    collate = utf8mb4_unicode_ci;

create table strategies
(
    strategyid      int auto_increment
        primary key,
    name            varchar(100) null,
    strategy_type   varchar(50)  null,
    description     varchar(500) null,
    strategy_ref_id int          null,
    create_time     datetime     null,
    scale           double       null,
    funds           varchar(500) null,
    fee_rate        double       null,
    return_rate     double       null,
    annual_return   double       null,
    volatility      double       null,
    sharpe_ratio    double       null,
    max_drawdown    double       null,
    win_rate        double       null
)
    collate = utf8mb4_unicode_ci;

create table timing_portfolio_items
(
    id           int auto_increment
        primary key,
    portfolio_id int         null,
    fund_code    varchar(30) null,
    weight       double      null,
    fee_rate     double      null
)
    collate = utf8mb4_unicode_ci;

create table timing_portfolios
(
    timingid          int auto_increment
        primary key,
    name              varchar(100) null,
    benchmark         varchar(100) null,
    feature           varchar(200) null,
    audience          varchar(100) null,
    scale             double       null,
    fee_rate          double       null,
    allocation_method varchar(50)  null,
    fund_count        int          null
)
    collate = utf8mb4_unicode_ci;

create table trade_orders
(
    tradeid          int auto_increment
        primary key,
    customer_id      int          null,
    fund_code        varchar(30)  null,
    portfolio_id     int          null,
    rebalance_id     int          null,
    amount           double       null,
    shares           double       null,
    trade_type       varchar(20)  null,
    reason           varchar(200) null,
    trade_time       datetime     null,
    status           varchar(20)  null,
    fail_reason      varchar(200) null,
    replace_order_id int          null
)
    collate = utf8mb4_unicode_ci;

