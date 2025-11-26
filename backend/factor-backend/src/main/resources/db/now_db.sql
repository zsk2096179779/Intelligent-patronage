create table SPRING_SESSION
(
    PRIMARY_ID            char(36)     not null
        primary key,
    SESSION_ID            char(36)     not null,
    CREATION_TIME         bigint       not null,
    LAST_ACCESS_TIME      bigint       not null,
    MAX_INACTIVE_INTERVAL int          not null,
    EXPIRY_TIME           bigint       not null,
    PRINCIPAL_NAME        varchar(100) null,
    constraint SPRING_SESSION_IX1
        unique (SESSION_ID)
)
    row_format = DYNAMIC;

create index SPRING_SESSION_IX2
    on SPRING_SESSION (EXPIRY_TIME);

create index SPRING_SESSION_IX3
    on SPRING_SESSION (PRINCIPAL_NAME);

create table SPRING_SESSION_ATTRIBUTES
(
    SESSION_PRIMARY_ID char(36)     not null,
    ATTRIBUTE_NAME     varchar(200) not null,
    ATTRIBUTE_BYTES    blob         not null,
    primary key (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    constraint SPRING_SESSION_ATTRIBUTES_FK
        foreign key (SESSION_PRIMARY_ID) references SPRING_SESSION (PRIMARY_ID)
            on delete cascade
)
    row_format = DYNAMIC;

create table agreement_template
(
    id                  int unsigned auto_increment comment '主键ID'
        primary key,
    agreement_code      varchar(50)                        not null comment '协议编码',
    agreement_type      varchar(50)                        not null comment '协议类型',
    title               varchar(200)                       not null comment '协议标题',
    content             longtext                           not null comment '协议正文',
    content_hash        varchar(64)                        null comment '内容哈希',
    applicable_scenario varchar(100)                       null comment '适用场景',
    version             varchar(20)                        not null comment '版本号',
    is_latest           tinyint  default 1                 not null comment '是否最新版本',
    status              tinyint  default 1                 not null comment '状态',
    effective_date      date                               not null comment '生效日期',
    expire_date         date                               null comment '失效日期',
    view_count          int      default 0                 null comment '查看次数',
    sign_count          int      default 0                 null comment '签署次数',
    created_at          datetime default CURRENT_TIMESTAMP not null,
    updated_at          datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_code_version
        unique (agreement_code, version)
)
    comment '协议模板表' collate = utf8mb4_unicode_ci;

create index idx_type_status
    on agreement_template (agreement_type, status);

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

create table calc_strategies
(
    strategy_id   int auto_increment comment '计算策略ID'
        primary key,
    strategy_name varchar(50)          not null comment '策略名称（如：加权求和、等权平均、市值加权）',
    strategy_desc text                 null comment '策略说明（如：加权求和=Σ(基础因子值×权重)）',
    strategy_code varchar(50)          null comment '策略标识（如：WEIGHTED_SUM，用于代码调用）',
    is_custom     tinyint(1) default 0 null comment '是否自定义策略（1=用户拓展，0=系统内置）',
    is_valid      tinyint(1) default 1 null comment '是否有效（1=有效，0=失效）'
)
    comment '计算策略表：存储衍生因子公式的聚合策略（如加权、等权）' collate = utf8mb4_unicode_ci;

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
    factorid               int auto_increment
        primary key,
    derived_id             int                                   null,
    base_id                int                                   null,
    weight                 double                                null,
    weight_desc            varchar(100)                          null comment '权重说明（如：用户自定义权重、系统默认等权）',
    base_data_check_status varchar(20) default 'UNCHECKED'       null comment '基础数据校验状态（UNCHECKED=未校验，PASSED=通过，FAILED=失败）',
    check_time             datetime                              null comment '基础数据校验时间',
    calc_strategy_id       int                                   null comment '计算策略ID（关联calc_strategies表，如加权求和、等权平均）',
    formula_remark         text                                  null comment '公式组件备注（如：基础因子PB需剔除负值后参与计算）',
    update_time            datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '记录更新时间'
)
    collate = utf8mb4_unicode_ci;

create table etf_hist_159707
(
    date          datetime null,
    open          double   null,
    close         double   null,
    high          double   null,
    low           double   null,
    volume        bigint   null,
    turnover      double   null,
    amplitude     double   null,
    change_pct    double   null,
    change_amount double   null,
    turnover_rate double   null
);

create index ix_etf_hist_159707_date
    on etf_hist_159707 (date);

create table factor_base
(
    base_id          int auto_increment comment '基础因子ID'
        primary key,
    factor_name      varchar(100)                         not null comment '基础因子名称（如：市盈率TTM）',
    factor_code      varchar(50)                          not null comment '基础因子编码（唯一标识，如：PE_TTM）',
    factor_formula   text                                 null comment '基础因子固定计算公式（如：股价/过去12个月每股收益）',
    data_source      varchar(100)                         null comment '数据来源（如：Wind、Tushare）',
    update_frequency varchar(50)                          not null comment '更新频率（日度/周度，支撑数据更新监控）',
    data_start_date  date                                 null comment '数据起始日期',
    latest_data_date date                                 null comment '最新数据日期（用于校验数据完整性）',
    data_desc        text                                 null comment '因子说明（如：反映公司估值水平）',
    is_valid         tinyint(1) default 1                 null comment '是否有效（1=有效，0=失效）',
    create_time      datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    constraint factor_code
        unique (factor_code)
)
    comment '基础因子表：存储系统内置基础因子及固定公式' collate = utf8mb4_unicode_ci;

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

create table factor_derived
(
    derived_id       int auto_increment comment '衍生因子ID'
        primary key,
    factor_name      varchar(100)                         not null comment '衍生因子名称（用户自定义，如：估值综合因子）',
    factor_code      varchar(50)                          not null comment '衍生因子编码（唯一标识，如：VAL_COM）',
    factor_desc      text                                 null comment '衍生因子描述（用户自定义，如：PE与PB加权组合）',
    calc_strategy_id int                                  null comment '计算策略ID（关联calc_strategies表，如加权求和）',
    style_tag_ids    varchar(200)                         null comment '关联风格标签ID（多个用逗号分隔，如：价值型=1）',
    tree_node_id     int                                  null comment '所属因子树节点ID（关联factor_tree表，支撑自动挂树）',
    create_user_id   int                                  null comment '创建人ID（如量化研究员ID，预留权限关联）',
    create_time      datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    is_valid         tinyint(1) default 1                 null comment '是否有效（1=有效，0=失效）',
    constraint factor_code
        unique (factor_code)
)
    comment '衍生因子表：存储用户创建的衍生因子核心信息' collate = utf8mb4_unicode_ci;

create table factor_script
(
    script_id           int auto_increment comment '脚本ID'
        primary key,
    script_name         varchar(100)                          not null comment '脚本名称（如：自定义动量因子脚本）',
    script_file_path    varchar(200)                          not null comment '脚本存储路径（如：/scripts/momentum.py）',
    script_formula_desc text                                  null comment '脚本公式描述（文字说明计算逻辑，如：动量=12月收益率-1月收益率）',
    script_status       varchar(20) default 'INIT'            null comment '脚本状态（INIT=初始，RUNNING=运行中，SUCCESS=成功，FAILED=失败）',
    last_run_time       datetime                              null comment '上次运行时间',
    last_run_log        text                                  null comment '上次运行日志（支撑脚本监控）',
    output_derived_ids  varchar(200)                          null comment '输出衍生因子ID（多个用逗号分隔，关联factor_derived表）',
    create_user_id      int                                   null comment '上传人ID（预留权限关联）',
    create_time         datetime    default CURRENT_TIMESTAMP null comment '上传时间'
)
    comment '因子脚本表：存储自定义Python脚本及脚本生成的因子公式' collate = utf8mb4_unicode_ci;

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
    description text         null,
    scene_id    varchar(50)  null comment '场景ID（关联 factor_tree_scene 表的 scene_id，如：EQUITY=权益场景）'
)
    collate = utf8mb4_unicode_ci;

create table factor_tree_scene
(
    scene_id         varchar(50)                          not null comment '场景ID（如：EQUITY=FIXED_INCOME）'
        primary key,
    scene_name       varchar(100)                         not null comment '场景名称（如：权益投资场景）',
    scene_desc       text                                 null comment '场景描述（如：用于权益类FOF产品的因子筛选、回测）',
    manager_user_id  int                                  null comment '场景负责人ID（预留用户关联）',
    valid_start_date date                                 null comment '场景有效期起始日期',
    valid_end_date   date                                 null comment '场景有效期结束日期',
    is_valid         tinyint(1) default 1                 null comment '场景是否有效（1=有效，0=失效）',
    create_time      datetime   default CURRENT_TIMESTAMP null comment '场景创建时间'
)
    comment '因子树场景字典表：管理不同业务场景的配置' collate = utf8mb4_unicode_ci;

create table factor_validation_task
(
    task_id        int auto_increment comment '检验任务ID'
        primary key,
    task_name      varchar(200)                          not null comment '任务名称（用户自定义，如：价值因子IC检验）',
    task_type      varchar(50) default 'IC_IR'           not null comment '任务类型（IC_IR=IC/IR计算，LAYERED=分层回测）',
    factor_ids     varchar(500)                          not null comment '检验因子ID列表（多个用逗号分隔，如：1,2,3）',
    start_date     date                                  not null comment '回测开始日期',
    end_date       date                                  not null comment '回测结束日期',
    task_status    varchar(20) default 'PENDING'         null comment '任务状态（PENDING=待执行，RUNNING=执行中，SUCCESS=成功，FAILED=失败）',
    progress       int         default 0                 null comment '任务进度（0-100）',
    error_message  text                                  null comment '错误信息（任务失败时记录）',
    create_user_id int                                   null comment '创建人ID',
    create_time    datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    start_time     datetime                              null comment '开始执行时间',
    end_time       datetime                              null comment '结束执行时间'
)
    comment '因子检验任务表：存储因子检验任务的基本信息' collate = utf8mb4_unicode_ci;

create table factor_ic_ir_result
(
    result_id         int auto_increment comment '结果ID'
        primary key,
    task_id           int                                not null comment '关联任务ID',
    factor_id         int                                not null comment '因子ID（关联factor_derived.derived_id）',
    factor_code       varchar(50)                        null comment '因子编码（冗余字段，便于查询）',
    factor_name       varchar(100)                       null comment '因子名称（冗余字段，便于查询）',
    ic_mean           decimal(10, 6)                     null comment 'IC均值（Information Coefficient Mean）',
    ic_std            decimal(10, 6)                     null comment 'IC标准差',
    ir_value          decimal(10, 6)                     null comment 'IR值（Information Ratio = IC均值 / IC标准差）',
    ic_positive_ratio decimal(5, 4)                      null comment 'IC正相关比例（IC>0的比例）',
    ic_sequence       text                               null comment 'IC序列（JSON格式，存储每日IC值）',
    calculation_date  date                               not null comment '计算日期（回测结束日期）',
    create_time       datetime default CURRENT_TIMESTAMP null comment '创建时间',
    constraint factor_ic_ir_result_ibfk_1
        foreign key (task_id) references factor_validation_task (task_id)
            on delete cascade
)
    comment '因子IC/IR计算结果表：存储每个因子的IC/IR计算结果' collate = utf8mb4_unicode_ci;

create index idx_calculation_date
    on factor_ic_ir_result (calculation_date);

create index idx_factor_id
    on factor_ic_ir_result (factor_id);

create index idx_task_id
    on factor_ic_ir_result (task_id);

create table factor_ic_sequence
(
    sequence_id int auto_increment comment '序列ID'
        primary key,
    result_id   int                                not null comment '关联结果ID',
    trade_date  date                               not null comment '交易日期',
    ic_value    decimal(10, 6)                     not null comment '当日IC值',
    rank_ic     decimal(10, 6)                     null comment 'Rank IC值（可选）',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    constraint factor_ic_sequence_ibfk_1
        foreign key (result_id) references factor_ic_ir_result (result_id)
            on delete cascade
)
    comment '因子IC序列明细表：存储每日IC值明细' collate = utf8mb4_unicode_ci;

create index idx_result_id
    on factor_ic_sequence (result_id);

create index idx_trade_date
    on factor_ic_sequence (trade_date);

create index idx_create_time
    on factor_validation_task (create_time);

create index idx_status
    on factor_validation_task (task_status);

create index idx_user_id
    on factor_validation_task (create_user_id);

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

create table fund_closed_fund_price
(
    code     varchar(20) not null,
    date     date        not null,
    open     float       null,
    high     float       null,
    low      float       null,
    close    float       null,
    volume   bigint      null,
    turnover bigint      null,
    primary key (code, date)
);

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

create table fund_etf_price
(
    code     varchar(20) not null,
    date     date        not null,
    open     float       null,
    high     float       null,
    low      float       null,
    close    float       null,
    volume   bigint      null,
    turnover bigint      null,
    primary key (code, date)
);

create table fund_etf_spot_ths
(
    code              varchar(20)  not null,
    name              varchar(100) null,
    unit_nav          float        null,
    acc_nav           float        null,
    prev_unit_nav     float        null,
    prev_acc_nav      float        null,
    change_value      float        null,
    change_rate       float        null,
    redeem_status     varchar(50)  null,
    subscribe_status  varchar(50)  null,
    latest_trade_date date         null,
    latest_unit_nav   float        null,
    latest_acc_nav    float        null,
    fund_type         varchar(50)  null,
    query_date        date         not null,
    primary key (code, query_date)
);

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

create table fund_list
(
    code        varchar(20)                         not null
        primary key,
    name        varchar(100)                        null,
    pinyin_abbr varchar(50)                         null,
    fund_type   varchar(100)                        null,
    category    varchar(50)                         null,
    updated_at  timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

create table fund_lof_price
(
    code     varchar(20) not null,
    date     date        not null,
    open     float       null,
    high     float       null,
    low      float       null,
    close    float       null,
    volume   bigint      null,
    turnover bigint      null,
    primary key (code, date)
);

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

create table fund_open_fund_nav
(
    code         varchar(20) not null,
    date         date        not null,
    unit_nav     float       null,
    acc_nav      float       null,
    daily_change float       null,
    primary key (code, date)
);

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
    id              int auto_increment
        primary key,
    name            varchar(100)                          null,
    risk_level      varchar(50)                           null,
    strategy_type   varchar(50)                           null,
    summary         text                                  null comment '组合简介',
    target_investor varchar(200)                          null comment '目标客户',
    return_rate     decimal(10, 4)                        null comment '策略收益（%）',
    annual_return   decimal(10, 4)                        null comment '年化收益（%）',
    max_drawdown    decimal(10, 4)                        null comment '最大回撤（%）',
    sharpe_ratio    decimal(10, 4)                        null comment '夏普比率',
    volatility      decimal(10, 4)                        null comment '波动率（%）',
    win_rate        decimal(10, 4)                        null comment '胜率（%）',
    strategy_id     int                                   null,
    listed          tinyint(1)                            null,
    status          varchar(30) default 'draft'           not null comment '状态：draft/pending_review/approved/rejected',
    created_at      datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at      datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    reject_reason   varchar(500)                          null comment '审核拒绝原因'
)
    collate = utf8mb4_unicode_ci;

create table portfolio_holdings
(
    id           int auto_increment
        primary key,
    portfolio_id int                                    not null comment '关联 portfolios.id',
    fund_code    varchar(30) collate utf8mb4_unicode_ci null,
    fund_name    varchar(200)                           null comment '名称',
    weight       decimal(6, 4)                          not null comment '目标权重 (%)',
    remark       varchar(255)                           null comment '备注',
    created_at   datetime default CURRENT_TIMESTAMP     not null,
    updated_at   datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP,
    constraint uk_holdings
        unique (portfolio_id, fund_code),
    constraint fk_holdings_portfolio
        foreign key (portfolio_id) references portfolios (id)
            on delete cascade
)
    comment '组合持仓明细（简化版）';

create table portfolio_product_params
(
    id                int auto_increment
        primary key,
    portfolio_id      int                                not null comment '关联 portfolios.id',
    min_invest_amount decimal(16, 2)                     null comment '最低投资额',
    max_invest_amount decimal(16, 2)                     null comment '最高投资额/规模上限',
    subscription_fee  decimal(6, 4)                      null comment '申购费率 (%)',
    redemption_fee    decimal(6, 4)                      null comment '赎回费率 (%)',
    management_fee    decimal(6, 4)                      null comment '管理费率 (%)',
    open_day_rule     varchar(100)                       null comment '开放日规则',
    redemption_rule   varchar(100)                       null comment '赎回规则',
    created_at        datetime default CURRENT_TIMESTAMP not null,
    updated_at        datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_product_portfolio
        unique (portfolio_id),
    constraint fk_product_portfolio
        foreign key (portfolio_id) references portfolios (id)
            on delete cascade
)
    comment '组合产品参数（简化版）';

create index idx_listed
    on portfolios (listed);

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

create table risk_questionnaire
(
    id            int unsigned auto_increment comment '主键ID'
        primary key,
    question_code varchar(50)                             not null comment '题目编码',
    question_text text                                    not null comment '题目内容',
    question_type varchar(20)   default 'single'          not null comment '题型：single/multiple',
    options       json                                    not null comment '选项列表',
    category      varchar(50)                             null comment '题目分类',
    order_num     int           default 0                 not null comment '排序号',
    weight        decimal(5, 2) default 1.00              null comment '题目权重',
    version       varchar(20)   default '1.0'             null comment '问卷版本',
    status        tinyint       default 1                 not null comment '状态：0-停用，1-启用',
    created_at    datetime      default CURRENT_TIMESTAMP not null,
    updated_at    datetime      default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_question_code
        unique (question_code)
)
    comment '风险测评问卷表' collate = utf8mb4_unicode_ci;

create index idx_version_status
    on risk_questionnaire (version, status);

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

create table strategy
(
    id          bigint auto_increment
        primary key,
    name        varchar(255)               not null,
    type        varchar(50)                not null,
    description varchar(255)               null,
    risk_level  int                        null,
    status      varchar(20) default 'stop' not null,
    created_at  datetime                   not null,
    updated_at  datetime                   not null,
    owner       int                        null comment '策略拥有者ID'
);

create table strategy_config
(
    id                  bigint auto_increment
        primary key,
    strategy_id         bigint                                not null,
    rebalance_period    varchar(20) default 'monthly'         not null,
    max_rebalance_ratio tinyint     default 100               not null,
    is_active_rebalance tinyint(1)  default 1                 not null,
    created_at          datetime    default CURRENT_TIMESTAMP not null,
    updated_at          datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint strategy_config_ibfk_1
        foreign key (strategy_id) references strategy (id)
            on delete cascade
);

create index strategy_id
    on strategy_config (strategy_id);

create table strategy_factor
(
    id          bigint auto_increment
        primary key,
    strategy_id bigint                     not null,
    factor_name varchar(100)               not null,
    weight      decimal(5, 2) default 0.00 null,
    frequency   varchar(20)                null,
    created_at  datetime                   not null,
    updated_at  datetime                   not null,
    constraint strategy_factor_ibfk_1
        foreign key (strategy_id) references strategy (id)
            on delete cascade
);

create index strategy_id
    on strategy_factor (strategy_id);

create table strategy_filter_rule
(
    id          bigint auto_increment
        primary key,
    strategy_id bigint      not null,
    created_at  datetime    not null,
    updated_at  datetime    not null,
    top_n       int         null comment '筛选排名前N只',
    type_limit  varchar(10) null comment '类型限制(独热编码)',
    scale_limit int         null comment '规模限制',
    constraint strategy_filter_rule_ibfk_1
        foreign key (strategy_id) references strategy (id)
            on delete cascade
);

create index strategy_id
    on strategy_filter_rule (strategy_id);

create table strategy_heatmap
(
    id                   bigint auto_increment comment '主键ID'
        primary key,
    strategy_id          bigint                                   not null comment '关联策略ID',
    industry             varchar(100)                             not null comment '行业名称 (X轴)',
    comparison_dimension varchar(100)                             not null comment '比较维度 (Y轴)',
    deviation_value      decimal(10, 4) default 0.0000            not null comment '偏离度数值 (百分比)',
    created_at           datetime       default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at           datetime       default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '策略持仓偏离度热力图数据表';

create index idx_strategy_id
    on strategy_heatmap (strategy_id)
    comment '策略ID索引，用于快速查询特定策略的热力图数据';

create table strategy_profit_curve
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    strategy_id bigint                                   not null comment '关联策略ID',
    point_date  date                                     not null comment '日期点 (X轴)',
    net_value   decimal(16, 4) default 1.0000            not null comment '该日期的净值 (Y轴)',
    created_at  datetime       default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at  datetime       default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '策略预计收益曲线数据表';

create index idx_strategy_date
    on strategy_profit_curve (strategy_id, point_date)
    comment '策略ID+日期联合索引，用于快速查询并排序';

create table strategy_warning
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    strategy_id bigint                                 not null comment '关联策略ID',
    title       varchar(255) default ''                not null comment '预警标题',
    description text                                   null comment '预警详细描述',
    event_time  datetime                               not null comment '预警发生时间',
    risk_level  varchar(20)  default 'low'             not null comment '风险等级: high, medium, low',
    resolved    tinyint(1)   default 0                 not null comment '是否已解决: 0-未解决, 1-已解决',
    created_at  datetime     default CURRENT_TIMESTAMP not null comment '记录创建时间',
    updated_at  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '记录更新时间'
)
    comment '策略监控预警表';

create index idx_strategy_id
    on strategy_warning (strategy_id)
    comment '策略ID索引，优化查询';

create table style_tag
(
    tag_id         int auto_increment comment '风格标签ID'
        primary key,
    tag_name       varchar(50)                          not null comment '风格标签名称（如：价值、成长、质量、低波动、动量）',
    tag_code       varchar(50)                          not null comment '风格标签编码（唯一标识，如：VALUE、GROWTH、QUALITY、LOW_VOLATILITY、MOMENTUM）',
    description    text                                 null comment '标签描述（详细说明该风格标签的定义、特征和投资意义）',
    create_user_id int                                  null comment '创建人ID（记录风格标签的创建者信息，预留权限关联）',
    create_time    datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    is_valid       tinyint(1) default 1                 null comment '是否有效（1=有效，0=失效）',
    constraint tag_code
        unique (tag_code),
    constraint uk_tag_code
        unique (tag_code),
    constraint uk_tag_name
        unique (tag_name)
)
    comment '风格标签表：存储风格分类标签的定义和描述信息' collate = utf8mb4_unicode_ci;

create index idx_create_time
    on style_tag (create_time);

create index idx_valid
    on style_tag (is_valid);

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

create table users
(
    id         int auto_increment comment '用户ID'
        primary key,
    username   varchar(50)                                                 not null comment '用户名',
    password   varchar(255)                                                not null comment '密码（加密后）',
    email      varchar(100)                                                null comment '邮箱',
    phone      varchar(20)                                                 null comment '手机号',
    role       enum ('USER', 'STAFF', 'AUDITOR') default 'USER'            not null comment '用户角色：USER-普通用户，STAFF-工作人员，AUDITOR-审核人员',
    status     tinyint                           default 1                 not null comment '状态：1-正常，0-禁用',
    created_at datetime                          default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at datetime                          default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint username
        unique (username)
)
    comment '用户表';

create table investor_profile
(
    id                    bigint unsigned auto_increment comment '主键ID'
        primary key,
    user_id               int                                not null comment '用户ID（关联users表）',
    otc_account_no        varchar(50)                        null comment 'OTC账户号',
    otc_status            tinyint  default 0                 not null comment 'OTC状态：0-未开通，1-已开通',
    otc_open_date         datetime                           null comment 'OTC开通时间',
    real_name             varchar(100)                       null comment '真实姓名（加密）',
    id_card_no            varchar(200)                       null comment '身份证号（加密）',
    mobile                varchar(200)                       null comment '手机号（加密）',
    bank_card_no          varchar(200)                       null comment '银行卡号（加密）',
    bank_name             varchar(100)                       null comment '开户银行',
    risk_level            varchar(10)                        null comment '风险等级：C1/C2/C3/C4/C5',
    risk_score            int                                null comment '风险测评分数',
    risk_assessment_date  datetime                           null comment '最近测评时间',
    risk_expire_date      datetime                           null comment '测评过期时间',
    annual_income_range   varchar(50)                        null comment '年收入范围',
    total_asset_range     varchar(50)                        null comment '总资产范围',
    investment_experience varchar(50)                        null comment '投资经验',
    status                tinyint  default 1                 not null comment '状态：0-禁用，1-正常',
    created_at            datetime default CURRENT_TIMESTAMP not null,
    updated_at            datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_otc_account
        unique (otc_account_no),
    constraint uk_user_id
        unique (user_id),
    constraint fk_investor_profile_user
        foreign key (user_id) references users (id)
            on delete cascade
)
    comment '投资者画像表' collate = utf8mb4_unicode_ci;

create index idx_risk_level
    on investor_profile (risk_level);

create table risk_assessment_record
(
    id                    bigint unsigned auto_increment comment '主键ID'
        primary key,
    user_id               int                                not null comment '用户ID',
    assessment_no         varchar(50)                        not null comment '测评编号',
    questionnaire_version varchar(20)                        not null comment '问卷版本',
    answers               json                               not null comment '答题详情',
    total_score           int                                not null comment '总得分',
    risk_level            varchar(10)                        not null comment '风险等级',
    ip_address            varchar(50)                        null comment 'IP地址',
    device_info           varchar(200)                       null comment '设备信息',
    started_at            datetime                           not null comment '开始时间',
    completed_at          datetime                           null comment '完成时间',
    expire_at             datetime                           null comment '过期时间',
    is_completed          tinyint  default 0                 not null comment '是否完成',
    is_current            tinyint  default 0                 not null comment '是否当前有效',
    created_at            datetime default CURRENT_TIMESTAMP not null,
    updated_at            datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_assessment_no
        unique (assessment_no),
    constraint fk_risk_assessment_user
        foreign key (user_id) references users (id)
            on delete cascade
)
    comment '风险测评记录表' collate = utf8mb4_unicode_ci;

create index idx_user_current
    on risk_assessment_record (user_id, is_current);

create index idx_user_id
    on risk_assessment_record (user_id);

create table subscription_order
(
    id                      bigint unsigned auto_increment comment '主键ID'
        primary key,
    order_no                varchar(50)                              not null comment '订单号',
    user_id                 int                                      not null comment '用户ID',
    portfolio_id            int                                      not null comment '组合产品ID',
    portfolio_name          varchar(200)                             not null comment '组合名称',
    subscription_amount     decimal(15, 2)                           not null comment '签约金额',
    actual_amount           decimal(15, 2)                           null comment '实际到账金额',
    fee_amount              decimal(15, 2) default 0.00              null comment '手续费',
    dividend_mode           varchar(20)    default 'reinvest'        null comment '分红方式',
    auto_invest_enabled     tinyint        default 0                 null comment '是否定投',
    auto_invest_period      varchar(20)                              null comment '定投周期',
    auto_invest_amount      decimal(15, 2)                           null comment '定投金额',
    user_risk_level         varchar(10)                              not null comment '用户风险等级',
    product_risk_level      varchar(10)                              not null comment '产品风险等级',
    risk_matched            tinyint                                  not null comment '风险是否匹配',
    risk_mismatch_confirmed tinyint        default 0                 null comment '不匹配已确认',
    agreements_signed       json                                     null comment '已签协议列表',
    all_agreements_signed   tinyint        default 0                 null comment '协议全部已签',
    signature_data          text                                     null comment '用户签名',
    signature_ip            varchar(50)                              null comment '签名IP',
    signed_at               datetime                                 null comment '签名时间',
    payment_method          varchar(50)                              null comment '支付方式',
    payment_status          varchar(20)    default 'unpaid'          null comment '支付状态',
    paid_at                 datetime                                 null comment '支付时间',
    payment_channel_no      varchar(100)                             null comment '支付渠道订单号',
    order_status            varchar(20)    default 'pending'         not null comment '订单状态',
    audit_status            varchar(20)    default 'pending'         null comment '审核状态',
    auditor_id              int                                      null comment '审核人ID',
    audit_remark            varchar(500)                             null comment '审核备注',
    audited_at              datetime                                 null comment '审核时间',
    submitted_at            datetime                                 null comment '提交时间',
    completed_at            datetime                                 null comment '完成时间',
    cancelled_at            datetime                                 null comment '取消时间',
    subscribe_channel       varchar(50)                              null comment '签约渠道',
    customer_remark         varchar(500)                             null comment '客户备注',
    created_at              datetime       default CURRENT_TIMESTAMP not null,
    updated_at              datetime       default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_order_no
        unique (order_no),
    constraint fk_subscription_order_auditor
        foreign key (auditor_id) references users (id)
            on delete set null,
    constraint fk_subscription_order_user
        foreign key (user_id) references users (id)
)
    comment '签约订单表' collate = utf8mb4_unicode_ci;

create index idx_order_status
    on subscription_order (order_status);

create index idx_portfolio_id
    on subscription_order (portfolio_id);

create index idx_user_id
    on subscription_order (user_id);

create index idx_user_status
    on subscription_order (user_id, order_status);

create table user_agreement_sign
(
    id                bigint unsigned auto_increment comment '主键ID'
        primary key,
    user_id           int                                not null comment '用户ID',
    agreement_id      int unsigned                       not null comment '协议模板ID',
    agreement_code    varchar(50)                        not null comment '协议编码',
    agreement_version varchar(20)                        not null comment '协议版本',
    sign_no           varchar(50)                        not null comment '签署编号',
    sign_status       tinyint  default 1                 not null comment '签署状态',
    sign_scenario     varchar(100)                       null comment '签署场景',
    related_order_no  varchar(50)                        null comment '关联订单号',
    ip_address        varchar(50)                        null comment '签署IP',
    device_info       varchar(200)                       null comment '设备信息',
    signed_at         datetime                           not null comment '签署时间',
    content_hash      varchar(64)                        not null comment '协议哈希',
    signature_data    text                               null comment '电子签名',
    created_at        datetime default CURRENT_TIMESTAMP not null,
    updated_at        datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_sign_no
        unique (sign_no),
    constraint fk_user_agreement_sign_template
        foreign key (agreement_id) references agreement_template (id),
    constraint fk_user_agreement_sign_user
        foreign key (user_id) references users (id)
            on delete cascade
)
    comment '协议签署记录表' collate = utf8mb4_unicode_ci;

create index idx_agreement_id
    on user_agreement_sign (agreement_id);

create index idx_related_order
    on user_agreement_sign (related_order_no);

create index idx_user_id
    on user_agreement_sign (user_id);

create index idx_email
    on users (email);

create index idx_role
    on users (role);

create index idx_username
    on users (username);

-- 1. 收益率因子（直接使用change_rate）
INSERT INTO factor_base (
    factor_name, factor_code, factor_formula, data_source,
    update_frequency, data_start_date, latest_data_date,
    data_desc, is_valid
) VALUES (
             '基金收益率因子',
             'FUND_RETURN',
             '{"type":"formula","version":"1.0","expression":"change_rate","dataFields":["change_rate"],"description":"收益率因子：直接使用change_rate字段"}',
             'fund_etf_spot_ths',
             '日度',
             '2020-01-01',
             CURDATE(),
             '基金收益率因子，直接使用fund_etf_spot_ths表的change_rate字段',
             1
         );

-- 2. 波动率因子（20日收益率标准差）
INSERT INTO factor_base (
    factor_name, factor_code, factor_formula, data_source,
    update_frequency, data_start_date, latest_data_date,
    data_desc, is_valid
) VALUES (
             '基金波动率因子',
             'FUND_VOLATILITY',
             '{"type":"formula","version":"1.0","expression":"STD(change_rate, 20)","dataFields":["change_rate"],"description":"波动率因子：20日收益率标准差"}',
             'fund_etf_spot_ths',
             '日度',
             '2020-01-01',
             CURDATE(),
             '基金波动率因子，计算20日收益率的标准差',
             1
         );

-- 3. 动量因子（20日收益率）
INSERT INTO factor_base (
    factor_name, factor_code, factor_formula, data_source,
    update_frequency, data_start_date, latest_data_date,
    data_desc, is_valid
) VALUES (
             '基金动量因子',
             'FUND_MOMENTUM',
             '{"type":"formula","version":"1.0","expression":"RETURN(unit_nav, 20)","dataFields":["unit_nav"],"description":"动量因子：20日收益率"}',
             'fund_etf_spot_ths',
             '日度',
             '2020-01-01',
             CURDATE(),
             '基金动量因子，计算20日收益率',
             1
         );

-- 4. 最大回撤因子
INSERT INTO factor_base (
    factor_name, factor_code, factor_formula, data_source,
    update_frequency, data_start_date, latest_data_date,
    data_desc, is_valid
) VALUES (
             '基金最大回撤因子',
             'FUND_MAX_DRAWDOWN',
             '{"type":"formula","version":"1.0","expression":"MAX_DRAWDOWN(unit_nav)","dataFields":["unit_nav"],"description":"最大回撤因子：计算历史最大回撤"}',
             'fund_etf_spot_ths',
             '日度',
             '2020-01-01',
             CURDATE(),
             '基金最大回撤因子，计算历史最大回撤',
             1
         );

-- 5. 夏普比率因子（简化版）
INSERT INTO factor_base (
    factor_name, factor_code, factor_formula, data_source,
    update_frequency, data_start_date, latest_data_date,
    data_desc, is_valid
) VALUES (
             '基金夏普比率因子',
             'FUND_SHARPE',
             '{"type":"formula","version":"1.0","expression":"MEAN(change_rate, 20) / STD(change_rate, 20)","dataFields":["change_rate"],"description":"夏普比率：收益率均值除以标准差"}',
             'fund_etf_spot_ths',
             '日度',
             '2020-01-01',
             CURDATE(),
             '基金夏普比率因子，计算收益率均值除以标准差',
             1
         );

-- 6. 均值回归因子
INSERT INTO factor_base (
    factor_name, factor_code, factor_formula, data_source,
    update_frequency, data_start_date, latest_data_date,
    data_desc, is_valid
) VALUES (
             '基金均值回归因子',
             'FUND_MEAN_REVERSION',
             '{"type":"formula","version":"1.0","expression":"MEAN_REVERSION(unit_nav, 20)","dataFields":["unit_nav"],"description":"均值回归因子：计算净值与均值的偏离度"}',
             'fund_etf_spot_ths',
             '日度',
             '2020-01-01',
             CURDATE(),
             '基金均值回归因子，计算净值与均值的偏离度',
             1
         );