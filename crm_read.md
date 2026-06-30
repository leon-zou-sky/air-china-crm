## 国航 CRM 系统设计文档

---

```
文档名称：凤凰知音高端旅客服务管理平台 系统设计说明书
项目代号：CRM-V2.0
版本号：  V2.0
编制人：  （你的名字）
审核人：  （技术 Leader）
日期：    2015年6月 ~ 2018年3月

密级：内部公开
```

---

## 一、项目概述

### 1.1 项目背景

```
中国国际航空股份有限公司（以下简称"国航"）凤凰知音常旅客计划
目前拥有会员约 800 万人，其中金卡会员 12 万人，白金卡会员 1.5 万人。

原有 CRM 系统基于 Spring MVC + Oracle 单体架构，自 2012 年上线以来，
随着业务增长，在以下方面遇到瓶颈：

  1. 性能瓶颈
     · 会员规模从 200 万增至 800 万，数据库查询变慢
     · 积分查询高峰期响应时间超过 3 秒
     · 客服来电弹屏延迟影响服务体验

  2. 可维护性差
     · 所有模块打包在一个 WAR 包中，修改任意模块需整体部署
     · 模块间耦合严重，修改积分模块可能影响工单模块

  3. 扩展性不足
     · 无法独立扩展高并发模块（如积分查询）
     · 新增业务功能需要修改大量已有代码

  4. 技术债务
     · 基于 Struts2 + iBatis 的老架构，安全漏洞多
     · 前端使用 JSP + ExtJS，开发效率低
```

### 1.2 项目目标

```
  1. 系统升级改造
     · 从 Spring MVC 单体架构升级为 Spring Boot + Dubbo 服务化架构
     · 核心模块拆分为独立服务，可独立部署和扩缩容

  2. 性能提升
     · 积分查询响应时间从 3 秒降至 100 毫秒以内
     · 客服来电弹屏响应时间控制在 1 秒以内
     · 支撑高峰期 500 TPS 并发量

  3. 业务支撑
     · 支撑呼叫中心 30~50 名客服的日常工作
     · 支撑全国约 200 名营业部人员的业务操作
     · 支撑主要枢纽机场约 100 名地服人员的服务工单处理

  4. 技术储备
     · 新模块试点 MySQL 5.7 → 8.0，为后续国产化做技术储备
     · 引入 Elasticsearch 提升搜索能力
     · 引入 Redis 缓存提升查询性能
```

### 1.3 用户规模与并发量

```
用户角色              人数        使用场景              并发量
───────────────────────────────────────────────────────────────
呼叫中心客服           30~50人     来电弹屏、退改签、积分查询  主要并发来源
营业部人员             ~200人      会员查询、积分操作          低并发
机场地服人员           ~100人      工单处理、状态回传          低并发
系统管理员             ~10人       配置管理、报表              极低并发

业务数据规模：
  · 会员总量：约 800 万
  · 金卡会员：约 12 万
  · 白金卡会员：约 1.5 万
  · 积分流水：约 5000 万条
  · 乘机记录：约 2 亿条
  · 客票记录：约 3000 万条
  · 日均通话量：约 2000 通

并发指标：
  · 日常 TPS：50~100
  · 高峰期 TPS：200~500（春节、暑运、里程兑换活动）
  · 积分查询：最高 800 TPS（缓存命中后）
```

---

## 二、系统架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────────────┐
│                            客户端层                                      │
│                                                                         │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐               │
│  │  客服工作站     │  │  营业部系统    │  │  机场地服终端  │               │
│  │  (PC Chrome)  │  │  (PC Chrome)  │  │  (移动/PC)    │               │
│  │  Vue 2.x SPA  │  │  Vue 2.x SPA  │  │  jQuery+Boot  │               │
│  │  30~50人在线   │  │  ~200人        │  │  ~100人        │               │
│  └───────┬───────┘  └───────┬───────┘  └───────┬───────┘               │
└──────────┼──────────────────┼──────────────────┼───────────────────────┘
           │ HTTP/HTTPS       │ HTTP/HTTPS        │ HTTP/HTTPS
           ▼                  ▼                   ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           接入层                                         │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  Nginx (主备)                                                    │   │
│  │  · 反向代理 + 负载均衡（upstream）                                │   │
│  │  · 静态资源（Vue 打包后的 JS/CSS/图片）                           │   │
│  │  · HTTPS 终端                                                   │   │
│  │  · IP 白名单（内部系统限制访问来源）                               │   │
│  │  · 请求限流（limit_req）                                        │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────────────────┘
                         │ HTTP (8080/8081/8082)
                         ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        应用服务层                                        │
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                   crm-web（Web 主应用）                           │  │
│  │                   Spring Boot 1.5 / Tomcat                       │  │
│  │                   端口：8080                                       │  │
│  │                   实例：2~4 个（Nginx 负载均衡）                    │  │
│  │                                                                   │  │
│  │  ┌──────────┐  ┌──────────────┐  ┌───────────────────────┐       │  │
│  │  │ 公共层    │  │ 会员管理      │  │ 客户 360 视图         │       │  │
│  │  │ 认证鉴权  │  │ · CRUD       │  │ · 聚合调用            │       │  │
│  │  │ 操作日志  │  │ · 等级计算    │  │ · 编排多个服务数据     │       │  │
│  │  │ 异常处理  │  │ · 标签管理    │  │ · 统一返回            │       │  │
│  │  │ 数据权限  │  │              │  │                       │       │  │
│  │  └──────────┘  └──────────────┘  └───────────────────────┘       │  │
│  │                                                                   │  │
│  │  ┌──────────────────────┐  ┌──────────────────────┐              │  │
│  │  │ 呼叫中心客服工作站    │  │ 报表统计              │              │  │
│  │  │ · 退改签规则引擎      │  │ · 日/月报             │              │  │
│  │  │ · 通话记录管理        │  │ · 会员分析            │              │  │
│  │  │ · 来电弹屏            │  │ · 积分统计            │              │  │
│  │  └──────────────────────┘  └──────────────────────┘              │  │
│  │                                                                   │  │
│  │  作为 Dubbo Consumer 调用：                                       │  │
│  │  · miles-service（积分服务）                                      │  │
│  │  · workorder-service（工单服务）                                  │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│           Dubbo RPC                              Dubbo RPC              │
│              │                                       │                  │
│              ▼                                       ▼                  │
│  ┌────────────────────────────┐  ┌────────────────────────────┐       │
│  │  miles-service（积分服务）   │  │  workorder-service（工单）  │       │
│  │  Spring Boot 1.5 / Dubbo   │  │  Spring Boot 1.5 / Dubbo   │       │
│  │  端口：20880（Dubbo）       │  │  端口：20881（Dubbo）       │       │
│  │  实例：2 个                 │  │  实例：2 个                 │       │
│  │                             │  │                             │       │
│  │  · 积分账户管理             │  │  · 工单 CRUD               │       │
│  │  · 积分累积/扣减/冻结       │  │  · 工单流程引擎             │       │
│  │  · 积分兑换                 │  │  · 工单派发                 │       │
│  │  · 积分过期处理             │  │  · SLA 监控                │       │
│  │  · 权益管理                 │  │  · 工单模板管理             │       │
│  └────────────────────────────┘  └────────────────────────────┘       │
└────────────────────────┬────────────────────────────────────────────────┘
                         │
          ┌──────────────┼──────────────┬──────────────┬──────────────┐
          ▼              ▼              ▼              ▼              ▼
┌──────────────┐ ┌───────────┐ ┌──────────────┐ ┌──────────┐ ┌──────────┐
│  MySQL 8.0   │ │ Redis 3.x │ │ RabbitMQ 3.x │ │ES 5.x    │ │ZooKeeper │
│              │ │           │ │              │ │          │ │3.4.x     │
│  核心业务库   │ │  缓存     │ │  消息队列    │ │全文检索   │ │注册中心   │
└──────────────┘ └───────────┘ └──────────────┘ └──────────┘ └──────────┘
```

### 2.2 服务拆分策略

```
服务名称                职责                    端口      拆分理由
──────────────────────────────────────────────────────────────────────────
crm-web                会员管理、客户360视图     8080      主应用，承载所有页面请求
                       客服工作站、退改签
                       报表统计

crm-miles-service      积分账户管理             20880     积分是独立业务域
                       积分累积/扣减/冻结       (Dubbo)   并发量最高，需独立扩缩容
                       积分兑换、过期处理                 下游依赖多（营销/短信）

crm-workorder-service  工单 CRUD               20881     工单对接外部机场系统
                       工单流程引擎             (Dubbo)   流程引擎需独立演进
                       工单派发、SLA 监控                 派发逻辑复杂

拆分原则：
  · 核心高并发模块独立（积分）
  · 对外集成模块独立（工单对接机场系统）
  · 变更频率低的模块保留在主应用（会员管理、报表）
  · 不过度拆分，避免分布式事务的复杂性
```

### 2.3 技术选型

```
技术              版本           用途                    选型理由
──────────────────────────────────────────────────────────────────────────
JDK               1.8           运行环境                2015年后企业标配
Spring Boot       1.5.x         应用框架                简化配置、内嵌Tomcat
Spring Framework  4.3.x         核心框架                依赖注入/AOP/事务
MyBatis           3.4.x         ORM                     SQL灵活、国内企业标配
MySQL             8.0           数据库                  新模块试点、国产化储备
Redis             3.2           缓存                    高性能KV缓存、集群模式
RabbitMQ          3.6           消息队列                可靠投递、路由灵活
Elasticsearch     5.6           全文检索                中文分词、模糊搜索
Dubbo             2.5.3         RPC 框架               阿里开源、国内企业主流
ZooKeeper         3.4           注册中心                Dubbo 依赖、分布式协调
Nginx             1.12          反向代理                负载均衡、静态资源
Vue.js            2.0           前端框架                组件化开发、学习成本低
Maven             3.x           构建工具                多模块管理、依赖管理
Jenkins           2.x           CI/CD                   自动构建部署
Git               2.x           版本管理                分布式、分支管理
CentOS            7             操作系统                企业级Linux
```

---

## 三、数据库设计

### 3.1 数据库选型说明

```
本系统采用 MySQL 8.0 作为主要数据库。

选型说明：
  国航核心业务系统（离港、订座、结算）使用 Oracle 数据库。
  本项目为内部管理系统，在系统升级改造过程中，
  新增的工单模块和日志模块采用 MySQL 进行试点，
  为后续全面国产化做技术储备。

  MySQL 8.0 相比 5.7 的改进：
  · 性能提升（窗口函数、CTE）
  · 安全增强（角色管理、加密）
  · JSON 原生支持（工单模板存储）
  · 不可见索引（调试优化）
```

### 3.2 ER 图

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  t_member    │     │ t_member_tier_   │     │ t_flight_record  │
│  会员主表     │◄───┤ history          │     │ 乘机记录         │
│              │     │ 等级变动记录      │     │                  │
│  PK:member_id│     │                  │     │ PK:id            │
│  UK:member_no│     │ PK:id            │     │ FK:member_id     │
│  tier        │     │ FK:member_id     │     │ flight_no        │
│  mobile      │     │ from_tier        │     │ flight_date      │
│  redeemable_ │     │ to_tier          │     │ cabin_class      │
│  miles       │     │ change_reason    │     │ earned_miles     │
└──────┬───────┘     └──────────────────┘     └──────────────────┘
       │
       │ 1:N
       │
       ├─────────────────────────────────────────────┐
       │                                              │
       ▼                                              ▼
┌──────────────────┐                        ┌──────────────────┐
│ t_miles_account  │                        │ t_ticket         │
│ 积分账户         │                        │ 客票             │
│                  │                        │                  │
│ PK:member_id     │                        │ PK:ticket_id     │
│ balance          │                        │ UK:ticket_no     │
│ frozen           │                        │ FK:member_id     │
│ version(乐观锁)  │                        │ flight_no        │
└──────┬───────────┘                        │ ticket_price     │
       │ 1:N                                └──────┬───────────┘
       ▼                                           │ 1:N
┌──────────────────┐                               ▼
│ t_miles_         │                        ┌──────────────────┐
│ transaction      │                        │ t_ticket_change  │
│ 积分流水         │                        │ 客票变更记录      │
│                  │                        │                  │
│ PK:id            │                        │ PK:id            │
│ FK:member_id     │                        │ FK:ticket_id     │
│ tx_type          │                        │ change_type      │
│ miles            │                        │ change_fee       │
│ balance_after    │                        │ refund_amount    │
└──────────────────┘                        └──────────────────┘

┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│ t_work_order     │     │ t_work_order_    │     │ t_work_order_    │
│ 工单             │────►│ flow             │     │ template         │
│                  │ 1:N │ 工单流转记录      │     │ 工单模板         │
│ PK:order_id      │     │                  │     │                  │
│ UK:order_no      │     │ PK:id            │     │ PK:template_id   │
│ FK:member_id     │     │ FK:order_id      │     │ service_type     │
│ service_type     │     │ from_status      │     │ flow_nodes(JSON) │
│ status           │     │ to_status        │     │ sla_hours        │
│ sla_deadline     │     │ operator         │     │                  │
│ sla_status       │     └──────────────────┘     └──────────────────┘
└──────────────────┘
```

### 3.3 核心表结构

#### 3.3.1 会员主表

```sql
CREATE TABLE t_member (
    member_id         BIGINT        NOT NULL AUTO_INCREMENT,
    member_no         VARCHAR(20)   NOT NULL              COMMENT '会员号 CA10000001',
    name              VARCHAR(64)   NOT NULL              COMMENT '姓名',
    english_name      VARCHAR(128)  DEFAULT NULL          COMMENT '英文名',
    gender            TINYINT       DEFAULT 0             COMMENT '0未知 1男 2女',
    birthday          DATE          DEFAULT NULL,
    mobile            VARCHAR(20)   DEFAULT NULL,
    email             VARCHAR(128)  DEFAULT NULL,
    id_card_no        VARCHAR(128)  DEFAULT NULL          COMMENT '证件号AES加密存储',
    nationality       VARCHAR(32)   DEFAULT 'CN',
    tier              VARCHAR(16)   NOT NULL DEFAULT 'GENERAL' COMMENT 'GENERAL/SILVER/GOLD/PLATINUM',
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
```

#### 3.3.2 积分账户表

```sql
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

-- 乐观锁说明：
-- 每次积分扣减时，WHERE 条件带 version
-- 如果 version 不匹配，说明被其他事务修改过，返回影响行数 0
-- 应用层捕获后重试，最多重试 3 次
-- 适用于读多写少场景，避免悲观锁的行锁开销
```

#### 3.3.3 积分流水表

```sql
CREATE TABLE t_miles_transaction (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    member_id         BIGINT        NOT NULL              COMMENT '会员ID',
    tx_type           VARCHAR(16)   NOT NULL              COMMENT 'EARN/REDEEM/EXPIRE/FREEZE/UNFREEZE/ADJUST',
    miles             INT           NOT NULL              COMMENT '变动积分（正数增加，负数减少）',
    balance_after     INT           DEFAULT NULL          COMMENT '变动后余额',
    source            VARCHAR(32)   DEFAULT NULL          COMMENT 'FLIGHT/CREDIT_CARD/HOTEL/REDEEM/SYSTEM',
    reference_id      VARCHAR(64)   DEFAULT NULL          COMMENT '关联业务ID（客票号/兑换单号）',
    description       VARCHAR(256)  DEFAULT NULL          COMMENT '变动描述',
    expire_at         DATE          DEFAULT NULL          COMMENT '该笔积分过期时间',
    operator          VARCHAR(64)   DEFAULT NULL          COMMENT '操作人（客服工号/系统）',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_member_tx (member_id, created_at),
    KEY idx_expire (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水';
```

#### 3.3.4 工单表

```sql
CREATE TABLE t_work_order (
    order_id          BIGINT        NOT NULL AUTO_INCREMENT,
    order_no          VARCHAR(32)   NOT NULL              COMMENT '工单号 WO201806150001',
    member_id         BIGINT        DEFAULT NULL          COMMENT '关联会员ID',
    service_type      VARCHAR(32)   NOT NULL              COMMENT 'LOUNGE/WHEELCHAIR/MEET_GREET/HOTEL/TRANSFER',
    template_id       INT           DEFAULT NULL          COMMENT '关联工单模板ID',
    airport_code      VARCHAR(8)    DEFAULT NULL          COMMENT '机场代码 PEK/SHA/CAN',
    terminal          VARCHAR(8)    DEFAULT NULL          COMMENT '航站楼 T1/T2/T3',
    flight_no         VARCHAR(16)   DEFAULT NULL          COMMENT '航班号',
    flight_date       DATE          DEFAULT NULL          COMMENT '航班日期',
    service_time      DATETIME      DEFAULT NULL          COMMENT '服务时间',
    priority          TINYINT       DEFAULT 2             COMMENT '1紧急 2普通 3低',
    status            VARCHAR(16)   DEFAULT 'CREATED'     COMMENT 'CREATED/ASSIGNED/IN_PROGRESS/COMPLETED/CLOSED',
    assigned_to       VARCHAR(64)   DEFAULT NULL          COMMENT '指派给（地服工号）',
    assigned_at       DATETIME      DEFAULT NULL          COMMENT '指派时间',
    completed_at      DATETIME      DEFAULT NULL          COMMENT '完成时间',
    sla_deadline      DATETIME      DEFAULT NULL          COMMENT 'SLA截止时间',
    sla_status        VARCHAR(16)   DEFAULT 'NORMAL'      COMMENT 'NORMAL/AT_RISK/BREACHED',
    remark            VARCHAR(512)  DEFAULT NULL          COMMENT '备注',
    created_by        VARCHAR(64)   DEFAULT NULL          COMMENT '创建人（客服工号）',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_member (member_id),
    KEY idx_airport (airport_code, status),
    KEY idx_sla (sla_status, sla_deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单';
```

### 3.4 数据量预估

```
表名                    年增量        3年后总量      存储预估
──────────────────────────────────────────────────────────────
t_member                ~50万/年      950万          ~20GB
t_miles_account         ~50万/年      950万          ~10GB
t_miles_transaction     ~1500万/年    9500万         ~80GB
t_flight_record         ~6000万/年    20000万        ~150GB
t_ticket                ~1000万/年    6000万         ~60GB
t_ticket_change         ~150万/年     950万          ~15GB
t_work_order            ~30万/年      190万          ~5GB
t_work_order_flow       ~150万/年     950万          ~10GB
t_call_record           ~70万/年      610万          ~8GB
──────────────────────────────────────────────────────────────
合计                                      ~358GB
```

---

## 四、缓存设计

### 4.1 缓存策略

```
缓存组件：Redis 3.2 集群模式（6节点，3主3从）
Key 规范：crm:{模块}:{业务}:{标识}
```

### 4.2 缓存项清单

```
Key 格式                              类型     TTL      用途                    更新策略
────────────────────────────────────────────────────────────────────────────────────────
crm:member:info:{memberId}           String   30min   会员基础信息             变更时删除
crm:miles:balance:{memberId}         Hash     10min   积分余额+版本号          变更时主动更新
crm:miles:tx:recent:{memberId}       List     5min    最近20条积分流水         变更时LPUSH
crm:360:summary:{memberId}           String   5min    360视图聚合数据          通话结束时删除
crm:agent:status:{agentId}           String   不过期   客服坐席状态             状态变更时SET
crm:lock:ticket:{ticketNo}           String   30s     分布式锁（退改签）       释放时DEL
crm:lock:miles:{memberId}            String   10s     分布式锁（积分扣减）     释放时DEL
```

### 4.3 缓存一致性方案

```
采用 Cache Aside Pattern（旁路缓存模式）：

  读取流程：
    1. 先查 Redis 缓存
    2. 命中 → 直接返回
    3. 未命中 → 查 MySQL → 写入 Redis → 返回

  更新流程：
    1. 先更新 MySQL
    2. 再删除 Redis 缓存（不是更新缓存）
    3. 下次读取时重新从 MySQL 加载到 Redis

  为什么删除而不是更新缓存：
    · 避并发更新时缓存和数据库不一致
    · 删除缓存成本更低
    · 下次读取时惰性加载
```

---

## 五、消息队列设计

### 5.1 Exchange 和 Queue 清单

```
Exchange                       类型      绑定 Queue                              消费方
──────────────────────────────────────────────────────────────────────────────────────────────
crm.miles.exchange             topic     crm.miles.sms.queue                    短信服务
                                           crm.miles.marketing.queue            营销系统
                                           crm.miles.log.queue                  操作日志

crm.workorder.exchange         direct    crm.wo.pek.queue                       首都机场地服
                                           crm.wo.sha.queue                     上海机场地服
                                           crm.wo.can.queue                     白云机场地服

crm.wo.status.exchange         direct    crm.wo.status.queue                    CRM工单服务

crm.sla.exchange               fanout    crm.sla.alert.queue                    钉钉/邮件告警
                                           crm.sla.escalation.queue             工单升级
```

### 5.2 消息可靠性保证

```
环节                措施
──────────────────────────────────────────────
Producer 端         mandatory=true + ConfirmCallback
Exchange 端         durable=true（持久化）
Queue 端            durable=true（持久化）
Consumer 端         手动 ACK（acknowledge-mode=manual）
异常处理            死信队列（DLX）处理消费失败的消息
```

---

## 六、核心模块设计

### 6.1 积分扣减模块

#### 6.1.1 业务流程

```
客服/会员发起积分兑换
        │
        ▼
  ┌──────────────┐
  │ 参数校验      │  会员是否存在、积分数量是否合法
  └──────┬───────┘
         │
         ▼
  ┌──────────────┐
  │ 查 Redis 缓存 │  快速判断余额是否足够
  └──────┬───────┘
         │ 余额足够
         ▼
  ┌──────────────┐
  │ 乐观锁扣减   │  UPDATE ... WHERE version = ?
  │              │  影响行数=0 → 重试（最多3次）
  │              │  影响行数=1 → 成功
  └──────┬───────┘
         │ 成功
         ▼
  ┌──────────────┐
  │ 写积分流水    │  INSERT t_miles_transaction
  └──────┬───────┘
         │
         ▼
  ┌──────────────┐
  │ 更新 Redis   │  覆盖缓存余额
  └──────┬───────┘
         │
         ▼
  ┌──────────────┐
  │ 发 MQ 消息   │  通知短信/营销/日志
  └──────────────┘
```

#### 6.1.2 乐观锁 SQL

```sql
-- 扣减积分（乐观锁）
UPDATE t_miles_account
SET balance = balance - #{miles},
    total_redeemed = total_redeemed + #{miles},
    version = version + 1,
    updated_at = NOW()
WHERE member_id = #{memberId}
  AND balance >= #{miles}       -- 余额必须足够
  AND version = #{version}      -- 版本号必须匹配

-- 返回影响行数：
--   0 → 扣减失败（余额不足或版本冲突），重试
--   1 → 扣减成功
```

#### 6.1.3 为什么用乐观锁而不是悲观锁

```
场景特点：
  · 积分查询频率远高于扣减频率（查询:扣减 = 100:1）
  · 扣减冲突概率低（同一个会员同时被扣积分的情况很少）

悲观锁（SELECT FOR UPDATE）：
  · 查询时就锁住行，其他事务必须等待
  · 100 次查询都要加锁解锁，性能差
  · 适合写冲突频繁的场景

乐观锁（version 字段）：
  · 查询时不加锁，扣减时才检查 version
  · 100 次查询零开销，只有冲突时才重试
  · 适合读多写少的场景
  · 这个场景用乐观锁更合适
```

### 6.2 退改签规则引擎

#### 6.2.1 规则模型

```
规则维度：
  · 会员等级：GENERAL / SILVER / GOLD / PLATINUM
  · 舱位：F（头等）/ C（公务）/ Y（经济）
  · 变更类型：CHANGE（改签）/ REFUND（退票）
  · 距起飞时间：24小时以上 / 2~24小时 / 2小时以内

费率计算：
  · PERCENT：票价 × 费率百分比
  · FIXED：固定金额

示例规则：
  规则                          费率
  ─────────────────────────────────────
  白金卡 + 头等舱 + 改签         0%（免费）
  白金卡 + 经济舱 + 改签         5%
  金卡 + 经济舱 + 改签           10%
  普通卡 + 经济舱 + 改签         20%
  白金卡 + 退票                  5%
  普通卡 + 退票                  20%
```

#### 6.2.2 规则引擎执行流程

```
输入：客票信息 + 会员等级 + 变更类型
        │
        ▼
  ┌──────────────┐
  │ 参数校验      │  客票是否存在、状态是否正常
  └──────┬───────┘
         │
         ▼
  ┌──────────────┐
  │ 加载规则      │  从数据库加载所有生效规则
  └──────┬───────┘
         │
         ▼
  ┌──────────────┐
  │ 规则匹配      │  按优先级匹配：
  │              │  1. 会员等级 + 舱位 + 变更类型（精确匹配）
  │              │  2. 会员等级 + 变更类型（舱位为空=通用）
  │              │  3. 默认规则
  └──────┬───────┘
         │
         ▼
  ┌──────────────┐
  │ 费用计算      │  PERCENT: 票价 × 费率
  │              │  FIXED: 固定金额
  └──────┬───────┘
         │
         ▼
  输出：改签费 / 退票手续费
```

### 6.3 工单流程引擎

#### 6.3.1 状态机定义

```
状态流转规则：

  CREATED ──────► ASSIGNED ──────► IN_PROGRESS ──────► COMPLETED ──────► CLOSED
     │                │                  │
     │                │                  │
     └────────────────┴──────────────────┴──────────────────────────────► CLOSED

合法流转：
  FROM            TO                  触发场景
  ─────────────────────────────────────────────────
  CREATED         ASSIGNED            客服指派给地服
  CREATED         CLOSED              客服取消工单
  ASSIGNED        IN_PROGRESS         地服开始处理
  ASSIGNED        CLOSED              地服拒绝接单
  IN_PROGRESS     COMPLETED           地服完成服务
  IN_PROGRESS     CLOSED              异常关闭
  COMPLETED       CLOSED              归档
```

#### 6.3.2 工单模板

```
不同服务类型的工单有不同的模板配置：

  服务类型        SLA时限    优先级    流转节点
  ─────────────────────────────────────────────────────────
  轮椅服务        3小时      紧急      CREATED→ASSIGNED→IN_PROGRESS→COMPLETED→CLOSED
  贵宾休息室      2小时      普通      CREATED→ASSIGNED→IN_PROGRESS→COMPLETED→CLOSED
  接送机          4小时      普通      CREATED→ASSIGNED→IN_PROGRESS→COMPLETED→CLOSED
  酒店预订        24小时     低        CREATED→ASSIGNED→COMPLETED→CLOSED
  专人迎接        2小时      紧急      CREATED→ASSIGNED→IN_PROGRESS→COMPLETED→CLOSED

模板存储在 t_work_order_template 表的 flow_nodes 字段（JSON格式）
支持动态配置，新增服务类型只需插入一条模板记录
```

### 6.4 客户 360 视图

#### 6.4.1 数据聚合逻辑

```
客服接听电话 → 来电弹屏 → 调用 360 视图 API

  API 请求：GET /api/customer360/{memberId}

  聚合数据源：
    1. 会员信息      ← MySQL（crm-web 本地查询）
    2. 积分余额      ← Redis 缓存 → Dubbo 调用 miles-service
    3. 近期积分流水   ← Redis 缓存 → Dubbo 调用 miles-service
    4. 活跃工单      ← Dubbo 调用 workorder-service
    5. 乘机记录      ← MySQL（crm-web 本地查询）

  缓存策略：
    · 360 视图聚合结果缓存 5 分钟
    · 缓存命中：<5ms
    · 缓存未命中：50~100ms（需调用多个服务）
```

#### 6.4.2 返回数据结构

```json
{
  "member": {
    "memberId": 1,
    "memberNo": "CA10000001",
    "name": "张建国",
    "tier": "PLATINUM",
    "qualifyingMiles": 185000,
    "redeemableMiles": 86400
  },
  "miles": {
    "balance": 86400,
    "frozen": 0,
    "totalEarned": 185000,
    "totalRedeemed": 95000
  },
  "activeOrders": [
    {
      "orderId": 1,
      "orderNo": "WO201806150001",
      "serviceType": "LOUNGE",
      "status": "IN_PROGRESS",
      "slaStatus": "NORMAL"
    }
  ]
}
```

---

## 七、接口设计

### 7.1 会员管理接口

```
方法    路径                              说明
──────────────────────────────────────────────────────────────────
GET     /api/member/{memberId}           根据ID查询会员
GET     /api/member/no/{memberNo}        根据会员号查询
GET     /api/member/search?keyword=xx    会员搜索（姓名/手机号/会员号）
GET     /api/member/tier/{tier}          按等级查询会员列表
```

### 7.2 客票退改签接口

```
方法    路径                                              说明
──────────────────────────────────────────────────────────────────
GET     /api/ticket/{ticketNo}                           查询客票
GET     /api/ticket/change/fee?ticketNo=xx&memberNo=xx   计算改签费用
POST    /api/ticket/change?ticketNo=xx&newFlightNo=xx    执行改签
POST    /api/ticket/refund?ticketNo=xx&memberNo=xx       执行退票
```

### 7.3 积分接口（Dubbo RPC）

```
方法                说明
──────────────────────────────────────────
queryBalance        查询积分余额
queryTransactions   查询积分流水（分页）
deduct              积分扣减（乐观锁）
earn                积分发放
```

### 7.4 工单接口（Dubbo RPC）

```
方法                说明
──────────────────────────────────────────
createOrder         创建工单
getOrderDetail      查询工单详情
updateStatus        更新工单状态
```

### 7.5 360 视图接口

```
方法    路径                       说明
──────────────────────────────────────────────────────────
GET     /api/customer360/{memberId}  获取客户360视图（聚合数据）
```

---

## 八、部署架构

### 8.1 生产环境部署

```
┌─────────────────────────────────────────────────────────────┐
│                      DMZ 区                                  │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Nginx (主备)                                        │    │
│  │  10.0.1.1:443 / 10.0.1.2:443                        │    │
│  └─────────────────────────────────────────────────────┘    │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                      应用区                                   │
│                                                              │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐  │
│  │ crm-web x4     │  │ miles-service  │  │ wo-service   │  │
│  │ 10.0.1.11:8080 │  │ x2             │  │ x2           │  │
│  │ 10.0.1.12:8080 │  │ 10.0.1.21:8081 │  │ 10.0.1.31:8082│ │
│  │ 10.0.1.13:8080 │  │ 10.0.1.22:8081 │  │ 10.0.1.32:8082│ │
│  │ 10.0.1.14:8080 │  │                │  │              │  │
│  └────────────────┘  └────────────────┘  └──────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                      数据区                                   │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ MySQL 8.0    │  │ Redis 集群    │  │ RabbitMQ     │      │
│  │ 主从          │  │ 6节点         │  │ 镜像队列     │      │
│  │ 10.0.1.41    │  │ 10.0.1.51~56 │  │ 10.0.1.61~62 │      │
│  │ 10.0.1.42    │  │              │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐                         │
│  │ ES 5.x       │  │ ZooKeeper    │                         │
│  │ 3节点         │  │ 3节点         │                         │
│  │ 10.0.1.71~73 │  │ 10.0.1.81~83 │                         │
│  └──────────────┘  └──────────────┘                         │
└─────────────────────────────────────────────────────────────┘
```

### 8.2 服务器资源清单

```
服务器              数量    配置                  用途
──────────────────────────────────────────────────────────────────
Nginx               2      2C4G                  反向代理（主备）
crm-web             4      4C8G                  Web 主应用
miles-service       2      4C8G                  积分服务
wo-service          2      4C8G                  工单服务
MySQL               2      8C32G                 数据库（主从）
Redis               6      4C8G                  缓存（3主3从）
RabbitMQ            2      4C8G                  消息队列（镜像）
Elasticsearch       3      4C16G                 全文检索
ZooKeeper           3      2C4G                  注册中心
Jenkins             1      4C8G                  CI/CD
──────────────────────────────────────────────────────────────────
合计                25台
```

---

## 九、外部系统集成

```
┌──────────────────────────────────────────────────────────────┐
│                        CRM 系统                               │
└──────────────┬──────────┬──────────┬─────────────────────────┘
               │          │          │
        ┌──────▼───┐ ┌───▼────┐ ┌──▼──────────┐
        │ 离港系统  │ │ 运价   │ │ 常旅客系统   │
        │ DCS      │ │ 系统   │ │             │
        │          │ │        │ │ 获取积分     │
        │ 获取航班  │ │ 获取   │ │ 获取会员     │
        │ 状态     │ │ 票价   │ │ 信息         │
        └──────────┘ └────────┘ └─────────────┘
               │          │          │
        ┌──────▼───┐ ┌───▼────┐ ┌──▼──────────┐
        │ 机场地面  │ │ 短信   │ │  营销系统   │
        │ 服务系统  │ │ 平台   │ │             │
        │          │ │        │ │ 会员画像     │
        │ 工单派发  │ │ 积分   │ │ 活动推送     │
        │ 状态回传  │ │ 变动   │ │             │
        │          │ │ 通知   │ │             │
        └──────────┘ └────────┘ └─────────────┘

集成方式：
  · HTTP/REST：同步查询类接口
  · RabbitMQ：异步通知类消息
  · 数据库直连：同机房内的遗留系统（不推荐，但存在）
```

---

## 十、非功能需求

### 10.1 性能要求

```
指标                          目标值
──────────────────────────────────────────
360 视图响应时间（缓存命中）    < 5ms
360 视图响应时间（未命中）      < 100ms
积分查询响应时间               < 50ms
积分扣减响应时间               < 100ms
退改签费用计算                 < 200ms
工单创建                       < 500ms
工单派发延迟                   < 1s（MQ异步）
搜索响应时间                   < 200ms
```

### 10.2 可用性要求

```
指标                          目标值
──────────────────────────────────────────
系统可用性                     99.9%（全年宕机 < 8.76 小时）
数据库主从切换时间              < 30s
应用实例故障恢复时间            < 2min（自动重启）
消息队列故障恢复                自动切换（镜像队列）
```

### 10.3 安全要求

```
安全措施：
  · HTTPS 加密传输
  · 会员证件号 AES 加密存储
  · API 接口 Token 认证
  · 操作日志全量记录
  · IP 白名单限制访问
  · SQL 注入防护（MyBatis 参数化查询）
  · 敏感数据脱敏展示
```

---

## 十一、项目计划

```
阶段              时间              内容
──────────────────────────────────────────────────────────────────
需求分析          2015.06-2015.08   需求调研、方案设计
架构设计          2015.08-2015.10   技术选型、架构设计、数据库设计
一期开发          2015.10-2016.06   会员管理、积分中心、客服工作站
一期测试          2016.06-2016.08   功能测试、性能测试、安全测试
一期上线          2016.09           灰度上线、全量切换
二期开发          2016.10-2017.06   工单系统、SLA监控、报表统计
二期测试          2017.06-2017.08   功能测试、集成测试
二期上线          2017.09           灰度上线、全量切换
优化迭代          2017.09-2018.03   性能优化、ES引入、监控完善
```

---

```
文档到这里是完整的
覆盖了架构、数据库、缓存、MQ、核心模块、接口、部署、安全等所有方面

这份文档可以用于：
  · 面试时展示你对项目的理解深度
  · 回答"说说你这个项目的架构设计"
  · 回答"你负责的模块技术难点是什么"
  · 回答"为什么用乐观锁不用悲观锁"
  · 回答"为什么用 MQ 解耦"
```

十二、项目目录结构
目录结构参考

air-china-crm/
├── pom.xml
├── src/
│   └── main/
│       ├── java/com/airchina/crm/
│       │   ├── CrmApplication.java
│       │   ├── common/
│       │   │   ├── Result.java
│       │   │   ├── PageResult.java
│       │   │   ├── BizException.java
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── member/
│       │   │   ├── controller/MemberController.java
│       │   │   ├── service/MemberService.java
│       │   │   ├── dao/MemberMapper.java
│       │   │   └── model/Member.java
│       │   ├── customer360/
│       │   │   └── controller/Customer360Controller.java
│       │   ├── ticket/
│       │   │   ├── controller/TicketController.java
│       │   │   ├── service/TicketService.java
│       │   │   ├── service/TicketRuleEngine.java
│       │   │   ├── dao/TicketMapper.java
│       │   │   └── model/Ticket.java
│       │   ├── miles/
│       │   │   ├── controller/MilesController.java
│       │   │   ├── service/MilesService.java
│       │   │   ├── dao/MilesAccountMapper.java
│       │   │   ├── dao/MilesTransactionMapper.java
│       │   │   └── model/
│       │   │       ├── MilesAccount.java
│       │   │       └── MilesTransaction.java
│       │   └── workorder/
│       │       ├── controller/WorkOrderController.java
│       │       ├── service/WorkOrderService.java
│       │       ├── service/WorkOrderFlowEngine.java
│       │       ├── dao/WorkOrderMapper.java
│       │       ├── dao/WorkOrderFlowMapper.java
│       │       └── model/
│       │           ├── WorkOrder.java
│       │           └── WorkOrderFlow.java
│       └── resources/
│           ├── application.yml
│           └── mapper/
│               ├── MemberMapper.xml
│               ├── TicketMapper.xml
│               ├── MilesAccountMapper.xml
│               ├── MilesTransactionMapper.xml
│               ├── WorkOrderMapper.xml
│               └── WorkOrderFlowMapper.xml
└── sql/
    └── init.sql