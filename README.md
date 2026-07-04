# 凤凰知音高端旅客服务管理平台 (CRM)

> 国航凤凰知音常旅客 CRM 系统 - Java 后端服务

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 11 | OpenJDK |
| Spring Boot | 2.7.18 | 应用框架 |
| MyBatis-Plus | 3.5.3.1 | ORM |
| MySQL | 8.0 | 数据库（Docker 3307） |
| Redis | 单机 | 缓存（Cache Aside） |
| RabbitMQ | 3.x | 消息队列（待接入） |

## 项目结构

```
src/main/java/com/airchina/crm/
├── common/              # 通用层
│   ├── config/          # Redis、MyBatis-Plus、Web 配置
│   ├── enums/           # 会员等级、工单状态、积分类型等枚举
│   ├── exception/       # 全局异常处理
│   ├── result/          # 统一返回值 Result<T>
│   └── util/            # 会员号/工单号生成器
├── member/              # 会员管理模块
├── miles/               # 积分模块（乐观锁）
├── workorder/           # 工单模块（状态机）
├── ticket/              # 退改签规则引擎
├── customer360/         # 客户360视图（门面模式）
└── CrmApplication.java  # 启动类
```

## 已完成 ✅

| # | 模块 | 功能 | 技术亮点 |
|---|------|------|---------|
| 1 | 会员管理 | CRUD、搜索、等级查询 | 手机号/证件号脱敏、Redis 缓存（30min） |
| 2 | 积分系统 | 余额查询、扣减、发放、流水 | **乐观锁** + 重试3次 + Redis 缓存（10min） |
| 3 | 工单系统 | 创建、指派、流转、SLA | **状态机**（5个状态合法流转）+ 模板匹配 |
| 4 | 退改签规则引擎 | 按会员等级/舱位/时间算费 | **规则优先级匹配**（精确→模糊→默认） |
| 5 | 客户360视图 | 聚合会员+积分+工单 | **门面模式** + Redis 缓存（5min） |
| 6 | Redis 缓存 | Cache Aside 模式 | 读写分离、容错降级 |
| 7 | SLA 超时监控 | 定时扫描、状态更新、告警 | @Scheduled 每5分钟扫描、日志告警 |

## 未完成 ❌

| # | 功能 | 复杂度 | 说明 |
|---|------|--------|------|
| 1 | **RabbitMQ 消息通知** | 中 | 积分变动→短信/营销、工单派发→机场、SLA→钉钉 |
| 2 | Elasticsearch 搜索 | 高 | 会员全文检索、中文分词 |
| 3 | Docker Compose | 低 | 一键启动 MySQL + Redis + 应用 |
| 4 | Swagger 接口文档 | 低 | 在线调试所有接口 |
| 5 | 操作日志 | 低 | AOP 记录接口调用日志 |
| 6 | 积分过期处理 | 中 | 定时扫描过期积分，自动扣减 |
| 7 | 报表统计 | 中 | 日/月报、会员分析、积分统计 |

## 快速启动

```bash
# 1. 启动 MySQL（Docker）
docker run -d --name mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=123456 mysql:8.0

# 2. 建库建表
mysql -h localhost -P 3307 -u root -p123456 < sql/init.sql

# 3. 启动 Redis
brew services start redis  # 或 redis-server --daemonize yes

# 4. 启动应用
mvn spring-boot:run

# 5. 测试接口
curl http://localhost:8080/crm/api/member/1
curl http://localhost:8080/crm/api/miles/balance/1
curl http://localhost:8080/crm/api/customer360/1
```

## API 接口

### 会员管理
```
GET  /api/member/{memberId}           # 按ID查询
GET  /api/member/no/{memberNo}        # 按会员号查询
GET  /api/member/search?keyword=xx    # 搜索
GET  /api/member/tier/{tier}          # 按等级查询
```

### 积分系统
```
GET  /api/miles/balance/{memberId}    # 查余额
GET  /api/miles/transactions/{memberId}  # 查流水
POST /api/miles/deduct                # 扣减（乐观锁）
POST /api/miles/earn                  # 发放
```

### 工单系统
```
POST /api/workorder                   # 创建
GET  /api/workorder/{orderId}         # 详情
POST /api/workorder/{id}/assign       # 指派
POST /api/workorder/{id}/start        # 开始处理
POST /api/workorder/{id}/complete     # 完成
POST /api/workorder/{id}/close        # 关闭
GET  /api/workorder/{id}/flows        # 流转记录
GET  /api/workorder/sla/abnormal      # SLA异常工单
```

### 退改签
```
GET  /api/ticket/change/fee           # 计算改签费
GET  /api/ticket/refund/fee           # 计算退票费
POST /api/ticket/change               # 执行改签
POST /api/ticket/refund               # 执行退票
```

### 客户360视图
```
GET  /api/customer360/{memberId}      # 聚合数据
```

## 设计模式

| 模式 | 应用 |
|------|------|
| 分层架构 | Controller → Service → Mapper |
| 接口隔离 | Service 接口 + Impl（为 Dubbo 预留） |
| 状态机 | WorkOrderStatus + WorkOrderFlowEngine |
| 规则引擎 | TicketRuleEngine（优先级匹配） |
| 乐观锁 | MilesAccountMapper.deductMiles() |
| 门面模式 | Customer360ServiceImpl |
| Cache Aside | RedisCacheHelper + 容错降级 |
| DTO/VO 分离 | entity / dto / vo 三层 |

## 设计文档

详见 [crm_read.md](crm_read.md)

## 开发记忆

详见 [docs/memory/](docs/memory/) — 记录了关键设计决策和开发过程
