-- ============================================================
-- 凤凰知音高端旅客服务管理平台 - 建库建表脚本
-- MySQL 8.0
-- ============================================================

CREATE DATABASE IF NOT EXISTS air_china_crm DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE air_china_crm;

-- ------------------------------------------------------------
-- 1. 会员主表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_member;
CREATE TABLE t_member (
    member_id         BIGINT        NOT NULL AUTO_INCREMENT,
    member_no         VARCHAR(20)   NOT NULL              COMMENT '会员号 CA10000001',
    name              VARCHAR(64)   NOT NULL              COMMENT '姓名',
    english_name      VARCHAR(128)  DEFAULT NULL          COMMENT '英文名',
    gender            TINYINT       DEFAULT 0             COMMENT '0未知 1男 2女',
    birthday          DATE          DEFAULT NULL          COMMENT '生日',
    mobile            VARCHAR(20)   DEFAULT NULL          COMMENT '手机号',
    email             VARCHAR(128)  DEFAULT NULL          COMMENT '邮箱',
    id_card_no        VARCHAR(128)  DEFAULT NULL          COMMENT '证件号(AES加密)',
    nationality       VARCHAR(32)   DEFAULT 'CN'          COMMENT '国籍',
    tier              VARCHAR(16)   NOT NULL DEFAULT 'GENERAL' COMMENT '等级:GENERAL/SILVER/GOLD/PLATINUM',
    tier_achieved_at  DATETIME      DEFAULT NULL          COMMENT '当前等级获得时间',
    tier_expiry_date  DATE          DEFAULT NULL          COMMENT '等级有效期',
    qualifying_miles  INT           DEFAULT 0             COMMENT '当年定级里程',
    qualifying_segs   INT           DEFAULT 0             COMMENT '当年定级航段',
    total_miles       INT           DEFAULT 0             COMMENT '累积总里程',
    redeemable_miles  INT           DEFAULT 0             COMMENT '可兑换里程',
    lifetime_miles    BIGINT        DEFAULT 0             COMMENT '终身累积里程',
    status            TINYINT       DEFAULT 1             COMMENT '1正常 2冻结 3注销',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (member_id),
    UNIQUE KEY uk_member_no (member_no),
    KEY idx_tier (tier),
    KEY idx_mobile (mobile),
    KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员主表';

-- ------------------------------------------------------------
-- 2. 积分账户表（乐观锁）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_miles_account;
CREATE TABLE t_miles_account (
    member_id         BIGINT        NOT NULL              COMMENT '会员ID',
    balance           INT           DEFAULT 0             COMMENT '当前可用积分',
    frozen            INT           DEFAULT 0             COMMENT '冻结积分',
    total_earned      INT           DEFAULT 0             COMMENT '累积获得积分',
    total_redeemed    INT           DEFAULT 0             COMMENT '累积兑换积分',
    total_expired     INT           DEFAULT 0             COMMENT '累积过期积分',
    version           INT           DEFAULT 0             COMMENT '乐观锁版本号',
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分账户';

-- ------------------------------------------------------------
-- 3. 积分流水表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_miles_transaction;
CREATE TABLE t_miles_transaction (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    member_id         BIGINT        NOT NULL              COMMENT '会员ID',
    tx_type           VARCHAR(16)   NOT NULL              COMMENT 'EARN/REDEEM/EXPIRE/FREEZE/UNFREEZE/ADJUST',
    miles             INT           NOT NULL              COMMENT '变动积分(正数增加,负数减少)',
    balance_after     INT           DEFAULT NULL          COMMENT '变动后余额',
    source            VARCHAR(32)   DEFAULT NULL          COMMENT '来源:FLIGHT/CREDIT_CARD/HOTEL/REDEEM/SYSTEM',
    reference_id      VARCHAR(64)   DEFAULT NULL          COMMENT '关联业务ID',
    description       VARCHAR(256)  DEFAULT NULL          COMMENT '变动描述',
    expire_at         DATE          DEFAULT NULL          COMMENT '该笔积分过期时间',
    operator          VARCHAR(64)   DEFAULT NULL          COMMENT '操作人',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_member_tx (member_id, created_at),
    KEY idx_expire (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水';

-- ------------------------------------------------------------
-- 4. 客票表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_ticket;
CREATE TABLE t_ticket (
    ticket_id         BIGINT        NOT NULL AUTO_INCREMENT,
    ticket_no         VARCHAR(20)   NOT NULL              COMMENT '客票号',
    member_id         BIGINT        DEFAULT NULL          COMMENT '关联会员ID',
    passenger_name    VARCHAR(64)   NOT NULL              COMMENT '旅客姓名',
    flight_no         VARCHAR(16)   NOT NULL              COMMENT '航班号',
    flight_date       DATE          NOT NULL              COMMENT '航班日期',
    departure         VARCHAR(8)    NOT NULL              COMMENT '出发城市三字码',
    arrival           VARCHAR(8)    NOT NULL              COMMENT '到达城市三字码',
    cabin_class       VARCHAR(8)    NOT NULL              COMMENT '舱位:F/C/Y',
    ticket_price      DECIMAL(10,2) NOT NULL              COMMENT '票价',
    status            VARCHAR(16)   DEFAULT 'VALID'       COMMENT 'VALID/USED/CHANGED/REFUNDED',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (ticket_id),
    UNIQUE KEY uk_ticket_no (ticket_no),
    KEY idx_member (member_id),
    KEY idx_flight (flight_no, flight_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客票';

-- ------------------------------------------------------------
-- 5. 客票变更记录表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_ticket_change;
CREATE TABLE t_ticket_change (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    ticket_id         BIGINT        NOT NULL              COMMENT '客票ID',
    change_type       VARCHAR(16)   NOT NULL              COMMENT 'CHANGE/REFUND',
    original_flight   VARCHAR(16)   DEFAULT NULL          COMMENT '原航班号',
    new_flight        VARCHAR(16)   DEFAULT NULL          COMMENT '新航班号',
    change_fee        DECIMAL(10,2) DEFAULT 0             COMMENT '改签费',
    refund_amount     DECIMAL(10,2) DEFAULT 0             COMMENT '退票金额',
    rule_id           INT           DEFAULT NULL          COMMENT '匹配的规则ID',
    operator          VARCHAR(64)   DEFAULT NULL          COMMENT '操作人',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_ticket (ticket_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客票变更记录';

-- ------------------------------------------------------------
-- 6. 退改签规则表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_ticket_change_rule;
CREATE TABLE t_ticket_change_rule (
    rule_id           INT           NOT NULL AUTO_INCREMENT,
    rule_name         VARCHAR(64)   NOT NULL              COMMENT '规则名称',
    member_tier       VARCHAR(16)   DEFAULT NULL          COMMENT '适用等级(NULL=全部)',
    cabin_class       VARCHAR(8)    DEFAULT NULL          COMMENT '适用舱位(NULL=全部)',
    change_type       VARCHAR(16)   NOT NULL              COMMENT 'CHANGE/REFUND',
    hours_before      INT           DEFAULT NULL          COMMENT '距起飞小时数(NULL=不限)',
    fee_type          VARCHAR(16)   NOT NULL DEFAULT 'PERCENT'  COMMENT 'PERCENT/FIXED',
    fee_value         DECIMAL(10,2) NOT NULL DEFAULT 0    COMMENT '费率(百分比)或固定金额',
    priority          INT           DEFAULT 0             COMMENT '优先级(越大越优先)',
    status            TINYINT       DEFAULT 1             COMMENT '1启用 0停用',
    remark            VARCHAR(256)  DEFAULT NULL,
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (rule_id),
    KEY idx_match (change_type, member_tier, cabin_class, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退改签规则';

-- ------------------------------------------------------------
-- 7. 工单表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_work_order;
CREATE TABLE t_work_order (
    order_id          BIGINT        NOT NULL AUTO_INCREMENT,
    order_no          VARCHAR(32)   NOT NULL              COMMENT '工单号 WO202606300001',
    member_id         BIGINT        DEFAULT NULL          COMMENT '关联会员ID',
    service_type      VARCHAR(32)   NOT NULL              COMMENT 'LOUNGE/WHEELCHAIR/MEET_GREET/HOTEL/TRANSFER',
    template_id       INT           DEFAULT NULL          COMMENT '关联工单模板ID',
    airport_code      VARCHAR(8)    DEFAULT NULL          COMMENT '机场代码',
    terminal          VARCHAR(8)    DEFAULT NULL          COMMENT '航站楼',
    flight_no         VARCHAR(16)   DEFAULT NULL          COMMENT '航班号',
    flight_date       DATE          DEFAULT NULL          COMMENT '航班日期',
    service_time      DATETIME      DEFAULT NULL          COMMENT '服务时间',
    priority          TINYINT       DEFAULT 2             COMMENT '1紧急 2普通 3低',
    status            VARCHAR(16)   DEFAULT 'CREATED'     COMMENT 'CREATED/ASSIGNED/IN_PROGRESS/COMPLETED/CLOSED',
    assigned_to       VARCHAR(64)   DEFAULT NULL          COMMENT '指派给(地服工号)',
    assigned_at       DATETIME      DEFAULT NULL          COMMENT '指派时间',
    completed_at      DATETIME      DEFAULT NULL          COMMENT '完成时间',
    sla_deadline      DATETIME      DEFAULT NULL          COMMENT 'SLA截止时间',
    sla_status        VARCHAR(16)   DEFAULT 'NORMAL'      COMMENT 'NORMAL/AT_RISK/BREACHED',
    remark            VARCHAR(512)  DEFAULT NULL,
    created_by        VARCHAR(64)   DEFAULT NULL          COMMENT '创建人(客服工号)',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_member (member_id),
    KEY idx_airport (airport_code, status),
    KEY idx_sla (sla_status, sla_deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单';

-- ------------------------------------------------------------
-- 8. 工单流转记录表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_work_order_flow;
CREATE TABLE t_work_order_flow (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    order_id          BIGINT        NOT NULL              COMMENT '工单ID',
    from_status       VARCHAR(16)   DEFAULT NULL          COMMENT '原状态',
    to_status         VARCHAR(16)   NOT NULL              COMMENT '目标状态',
    operator          VARCHAR(64)   NOT NULL              COMMENT '操作人',
    remark            VARCHAR(256)  DEFAULT NULL          COMMENT '备注',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单流转记录';

-- ------------------------------------------------------------
-- 9. 工单模板表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_work_order_template;
CREATE TABLE t_work_order_template (
    template_id       INT           NOT NULL AUTO_INCREMENT,
    service_type      VARCHAR(32)   NOT NULL              COMMENT '服务类型',
    template_name     VARCHAR(64)   NOT NULL              COMMENT '模板名称',
    flow_nodes        JSON          NOT NULL              COMMENT '流转节点(JSON数组)',
    sla_hours         INT           NOT NULL DEFAULT 4    COMMENT 'SLA时限(小时)',
    default_priority  TINYINT       DEFAULT 2             COMMENT '默认优先级',
    status            TINYINT       DEFAULT 1             COMMENT '1启用 0停用',
    remark            VARCHAR(256)  DEFAULT NULL,
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (template_id),
    UNIQUE KEY uk_service_type (service_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单模板';

-- ------------------------------------------------------------
-- 10. 乘机记录表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_flight_record;
CREATE TABLE t_flight_record (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    member_id         BIGINT        NOT NULL              COMMENT '会员ID',
    flight_no         VARCHAR(16)   NOT NULL              COMMENT '航班号',
    flight_date       DATE          NOT NULL              COMMENT '航班日期',
    departure         VARCHAR(8)    NOT NULL              COMMENT '出发城市',
    arrival           VARCHAR(8)    NOT NULL              COMMENT '到达城市',
    cabin_class       VARCHAR(8)    NOT NULL              COMMENT '舱位',
    earned_miles      INT           DEFAULT 0             COMMENT '累积里程',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_member (member_id),
    KEY idx_flight (flight_no, flight_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='乘机记录';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 工单模板
INSERT INTO t_work_order_template (service_type, template_name, flow_nodes, sla_hours, default_priority) VALUES
('WHEELCHAIR',  '轮椅服务',   '["CREATED","ASSIGNED","IN_PROGRESS","COMPLETED","CLOSED"]', 3, 1),
('LOUNGE',      '贵宾休息室', '["CREATED","ASSIGNED","IN_PROGRESS","COMPLETED","CLOSED"]', 2, 2),
('TRANSFER',    '接送机',     '["CREATED","ASSIGNED","IN_PROGRESS","COMPLETED","CLOSED"]', 4, 2),
('HOTEL',       '酒店预订',   '["CREATED","ASSIGNED","COMPLETED","CLOSED"]',               24, 3),
('MEET_GREET',  '专人迎接',   '["CREATED","ASSIGNED","IN_PROGRESS","COMPLETED","CLOSED"]', 2, 1);

-- 退改签规则
INSERT INTO t_ticket_change_rule (rule_name, member_tier, cabin_class, change_type, hours_before, fee_type, fee_value, priority) VALUES
-- 改签规则
('白金卡-头等舱-改签',   'PLATINUM', 'F', 'CHANGE', NULL,   'PERCENT', 0,    100),
('白金卡-公务舱-改签',   'PLATINUM', 'C', 'CHANGE', NULL,   'PERCENT', 3,    99),
('白金卡-经济舱-改签',   'PLATINUM', 'Y', 'CHANGE', NULL,   'PERCENT', 5,    98),
('金卡-头等舱-改签',     'GOLD',     'F', 'CHANGE', NULL,   'PERCENT', 5,    90),
('金卡-经济舱-改签',     'GOLD',     'Y', 'CHANGE', NULL,   'PERCENT', 10,   88),
('普通卡-经济舱-改签',   'GENERAL',  'Y', 'CHANGE', NULL,   'PERCENT', 20,   50),
('银卡-经济舱-改签',     'SILVER',   'Y', 'CHANGE', NULL,   'PERCENT', 15,   60),
-- 退票规则
('白金卡-退票',          'PLATINUM', NULL, 'REFUND', NULL,   'PERCENT', 5,    90),
('金卡-退票',            'GOLD',     NULL, 'REFUND', NULL,   'PERCENT', 10,   80),
('银卡-退票',            'SILVER',   NULL, 'REFUND', NULL,   'PERCENT', 15,   70),
('普通卡-退票',          'GENERAL',  NULL, 'REFUND', NULL,   'PERCENT', 20,   50),
-- 默认兜底
('默认改签',             NULL,       NULL, 'CHANGE', NULL,   'PERCENT', 20,   0),
('默认退票',             NULL,       NULL, 'REFUND', NULL,   'PERCENT', 20,   0);

-- 测试会员数据
INSERT INTO t_member (member_no, name, english_name, gender, birthday, mobile, email, tier, qualifying_miles, redeemable_miles, total_miles, lifetime_miles, status) VALUES
('CA10000001', '张建国', 'ZHANG JIANGUO', 1, '1975-03-15', '13800001111', 'zjg@example.com', 'PLATINUM', 185000, 86400, 280000, 580000, 1),
('CA10000002', '李明',   'LI MING',       1, '1982-08-20', '13800002222', 'lm@example.com',  'GOLD',     95000, 42000, 150000, 320000, 1),
('CA10000003', '王芳',   'WANG FANG',     2, '1990-11-05', '13800003333', 'wf@example.com',  'SILVER',   45000, 18000, 60000,  120000, 1),
('CA10000004', '赵强',   'ZHAO QIANG',    1, '1988-06-12', '13800004444', 'zq@example.com',  'GENERAL',  12000, 8000,  20000,  35000,  1),
('CA10000005', '陈丽华', 'CHEN LIHUA',    2, '1979-01-28', '13800005555', 'clh@example.com', 'PLATINUM', 210000, 120000, 350000, 720000, 1);

-- 测试积分账户
INSERT INTO t_miles_account (member_id, balance, frozen, total_earned, total_redeemed, total_expired, version) VALUES
(1, 86400,  0, 280000, 180000, 13600, 0),
(2, 42000,  0, 150000, 95000,  13000, 0),
(3, 18000,  0, 60000,  35000,  7000,  0),
(4, 8000,   0, 20000,  10000,  2000,  0),
(5, 120000, 0, 350000, 200000, 30000, 0);

-- 测试客票
INSERT INTO t_ticket (ticket_no, member_id, passenger_name, flight_no, flight_date, departure, arrival, cabin_class, ticket_price, status) VALUES
('999-1234567890', 1, '张建国', 'CA1301', '2026-07-15', 'PEK', 'SHA', 'C', 2800.00, 'VALID'),
('999-1234567891', 2, '李明',   'CA1502', '2026-07-20', 'PEK', 'CAN', 'Y', 1280.00, 'VALID'),
('999-1234567892', 1, '张建国', 'CA1831', '2026-08-01', 'SHA', 'PEK', 'F', 5600.00, 'VALID');

-- 测试乘机记录
INSERT INTO t_flight_record (member_id, flight_no, flight_date, departure, arrival, cabin_class, earned_miles) VALUES
(1, 'CA1301', '2026-06-01', 'PEK', 'SHA', 'C', 2800),
(1, 'CA1302', '2026-06-10', 'SHA', 'PEK', 'C', 2800),
(2, 'CA1501', '2026-06-05', 'PEK', 'CAN', 'Y', 1280),
(1, 'CA1801', '2026-05-15', 'PEK', 'CTU', 'F', 3200);
