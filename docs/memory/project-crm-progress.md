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
- 单体架构，Service 接口+实现分离（为后面 Dubbo 预留）
- 开发顺序：会员 → 积分 → 工单 → 退改签 → 360视图

## 已完成 ✅
1. 项目脚手架 + 通用层（Result、异常、枚举、配置）
2. 建表 SQL（10张表 + 测试数据）：`sql/init.sql`
3. 会员管理：CRUD、搜索、脱敏、Redis 缓存（30min TTL）
4. 积分系统：乐观锁扣减（重试3次）、流水、Redis 缓存（10min TTL）
5. 工单系统：状态机（CREATED→ASSIGNED→IN_PROGRESS→COMPLETED→CLOSED）、模板匹配、SLA、流转记录
6. 退改签规则引擎：优先级匹配（会员等级×舱位×变更类型）、PERCENT/FIXED 费率
7. 客户360视图：门面模式聚合会员+积分+工单，Redis 缓存（5min TTL）
8. SLA 超时监控：@Scheduled 每5分钟扫描，NORMAL→AT_RISK(≤30min)→BREACHED，日志告警
9. RabbitMQ 消息通知：积分变动/工单派发/SLA告警，Topic/Direct/Fanout Exchange
10. Swagger 接口文档：OpenAPI 3.0，Swagger UI 在线调试
11. 客服知识库：Elasticsearch + IK中文分词，27条示例数据，支持全文搜索、高亮、分类筛选
12. Kibana 可视化：ES 数据可视化工具，支持 Discover、Visualize、Dashboard

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
- RabbitMQ：Topic（积分通知）、Direct（工单派发）、Fanout（SLA告警）
- ES 搜索：IK 分词器，标题权重3倍、关键词2倍、内容1倍
- 知识库设计：7大分类，27条示例数据（PLATFORM/TICKET/FAQ/SERVICE/BENEFITS/SCRIPT/SYSTEM）
- 数据导入：Python脚本导入Excel → ES，支持批量导入和增量更新
- Kibana 可视化：Index Pattern = crm_knowledge*，时间字段 = createTime

## 用户环境
- macOS, JDK 11 (OpenJDK 11.0.31)
- GitHub 账号: leon-zou-sky (skyleozou@gmail.com)
- SSH config 用 github-sky 别名区分两个 GitHub 账号
- 项目经历背景：2016~2018年 Java 企业级项目
