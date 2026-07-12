---
name: project-crm-progress
description: 国航CRM项目开发进度、技术选型、关键决策记录
metadata: 
  node_type: memory
  type: project
  originSessionId: 60a5e59e-6eb2-46b8-8131-d3500cee7f20
---

## 项目：凤凰知音高端旅客服务管理平台 (air-china-crm)

GitHub: https://github.com/leon-zou-sky/air-china-crm

## 技术栈
- Spring Boot 2.7.18 + JDK 11 + MyBatis-Plus 3.5
- MySQL 8.0 (Docker port 3307, password: 123456)
- Redis 单机 (localhost:6379)
- RabbitMQ 3.x (Docker port 5672/15672)
- Elasticsearch 7.17 + IK分词
- 单体架构，Service 接口+实现分离（为后面 Dubbo 预留）

## 已完成 ✅
1. 项目脚手架 + 通用层（Result、异常、枚举、配置）
2. 建表 SQL（10张表 + 测试数据）：`sql/init.sql`
3. 会员管理：CRUD、搜索、脱敏、Redis 缓存（30min TTL）
4. 积分系统：乐观锁扣减（重试3次）、流水、Redis 缓存（10min TTL）
5. 工单系统：状态机（CREATED→ASSIGNED→IN_PROGRESS→COMPLETED→CLOSED）、模板匹配、SLA、流转记录
6. 退改签规则引擎：优先级匹配（会员等级×舱位×变更类型）、PERCENT/FIXED 费率
7. 客户360视图：门面模式聚合会员+积分+工单，Redis 缓存（5min TTL）
8. SLA 超时监控：@Scheduled 每5分钟扫描，NORMAL→AT_RISK(≤30min)→BREACHED，日志告警
9. RabbitMQ 消息通知（积分变动/工单派发/SLA告警）
10. Swagger 接口文档
11. Elasticsearch 客服知识库

## 简历包装方案（2个项目）

### 项目一：国航凤凰知音会员积分系统（2023.06-2024.03，约10个月）
- 会员管理、积分系统（乐观锁）、退改签规则引擎、客户360视图
- 技术亮点：乐观锁+重试、Cache Aside缓存、规则引擎优先级匹配
- 成果：积分查询3s→50ms，支撑500TPS

### 项目二：国航机场工单服务系统（2024.04-2024.12，约9个月）
- 工单管理、状态机引擎、SLA监控、模板配置化、MQ派发
- 技术亮点：状态机模式、SLA分级告警、模板驱动配置化
- 成果：工单效率提升40%，SLA超时率15%→3%

## 未完成 ❌
- Docker Compose 一键部署
- 操作日志（AOP）
- 积分过期处理
- 报表统计

## 关键设计决策
- Cache Aside 模式：读缓存→未命中查DB→写缓存；写DB→删缓存
- Redis 容错：所有 Redis 操作 try-catch，不可用时降级查库
- 工单号生成：WO + 日期 + 随机4位（避免重启冲突）
- SLA 告警：当前日志打印，预留 MQ 扩展点（alert() 方法）
- 简历拆分：CRM 和工单拆成2个项目，更贴合实际（不同团队维护）

## 用户环境
- macOS, JDK 11 (OpenJDK 11.0.31)
- GitHub 账号: leon-zou-sky (skyleozou@gmail.com)
- SSH config 用 github-sky 别名区分两个 GitHub 账号
- 项目经历背景：2016~2018年 Java 企业级项目
