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
| RabbitMQ | 3.x | 消息队列（Docker 5672/15672） |
| Elasticsearch | 7.17 | 搜索引擎（Docker 9200）+ IK中文分词 |
| Kibana | 7.17 | ES 可视化工具（Docker 5601） |

## 项目结构

```
air-china-crm/
├── src/main/java/com/airchina/crm/
│   ├── common/              # 通用层
│   │   ├── config/          # Redis、MyBatis-Plus、Web、Swagger 配置
│   │   ├── enums/           # 会员等级、工单状态、积分类型等枚举
│   │   ├── exception/       # 全局异常处理
│   │   ├── result/          # 统一返回值 Result<T>
│   │   ├── mq/              # RabbitMQ 配置、生产者、消费者
│   │   └── util/            # 会员号/工单号生成器
│   ├── member/              # 会员管理模块
│   ├── miles/               # 积分模块（乐观锁）
│   ├── workorder/           # 工单模块（状态机）
│   ├── ticket/              # 退改签规则引擎
│   ├── customer360/         # 客户360视图（门面模式）
│   ├── knowledge/           # 客服知识库（ES搜索引擎）
│   └── CrmApplication.java  # 启动类
├── scripts/                 # 脚本工具
│   ├── create_full_knowledge.py  # 生成知识库示例数据
│   ├── import_knowledge.py       # Excel 导入 ES
│   ├── create_template.py        # 创建 Excel 模板
│   └── es-query.sh              # ES 数据查看
├── data/                    # 数据文件
│   └── knowledge.xlsx       # Excel 模板
└── sql/
    └── init.sql             # 数据库初始化
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
| 8 | RabbitMQ 消息通知 | 积分变动/工单派发/SLA告警 | Topic/Direct/Fanout Exchange、手动ACK、生产者确认 |
| 9 | Swagger 接口文档 | 在线调试所有接口 | OpenAPI 3.0、Swagger UI、接口分组 |
| 10 | 客服知识库 | 全文搜索、分类筛选、高亮显示 | **Elasticsearch + IK中文分词**、相关性排序 |

## 未完成 ❌

| # | 功能 | 复杂度 | 说明 |
|---|------|--------|------|
| 1 | Docker Compose | 低 | 一键启动 MySQL + Redis + RabbitMQ + ES + 应用 |
| 2 | 操作日志 | 低 | AOP 记录接口调用日志 |
| 3 | 积分过期处理 | 中 | 定时扫描过期积分，自动扣减 |
| 4 | 报表统计 | 中 | 日/月报、会员分析、积分统计 |

## 快速启动

```bash
# 1. 启动 MySQL（Docker）
docker run -d --name mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=123456 mysql:8.0

# 2. 建库建表
mysql -h localhost -P 3307 -u root -p123456 < sql/init.sql

# 3. 启动 Redis
docker run -d --name redis-crm -p 6379:6379 redis:7-alpine

# 4. 启动 RabbitMQ（Docker）
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management

# 5. 启动 Elasticsearch（Docker）
docker run -d --name elasticsearch \
  -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -e "xpack.security.enabled=false" \
  elasticsearch:7.17.18

# 安装 IK 分词器
docker exec elasticsearch bash -c "
cd /usr/share/elasticsearch/plugins && \
mkdir -p analysis-ik && cd analysis-ik && \
curl -L -o ik.zip https://release.infinilabs.com/analysis-ik/stable/elasticsearch-analysis-ik-7.17.18.zip && \
unzip ik.zip && rm ik.zip"
docker restart elasticsearch

# 6. 启动应用
mvn spring-boot:run

# 7. 测试接口
curl http://localhost:8080/crm/api/member/1
curl http://localhost:8080/crm/api/miles/balance/1
curl http://localhost:8080/crm/api/customer360/1
curl "http://localhost:8080/crm/api/knowledge/search?keyword=退改签"

# 9. 启动 Kibana（可选，ES 可视化）
docker run -d --name kibana \
  -p 5601:5601 \
  -e "ELASTICSEARCH_HOSTS=http://host.docker.internal:9200" \
  kibana:7.17.18

# 10. 初始化知识库数据
python3 scripts/create_full_knowledge.py

# 11. 访问管理界面
# Swagger UI:    http://localhost:8080/crm/swagger-ui/index.html
# RabbitMQ:      http://localhost:15672 (guest/guest)
# Elasticsearch: http://localhost:9200
# Kibana:        http://localhost:5601
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

### 客服知识库
```
GET  /api/knowledge/search?keyword=xx # 全文搜索（支持中文分词）
GET  /api/knowledge/search?category=FAQ  # 分类筛选
GET  /api/knowledge/{id}              # 知识详情（自动+1浏览量）
GET  /api/knowledge/category/{type}   # 按分类查询
GET  /api/knowledge/hot?limit=10      # 热门知识
GET  /api/knowledge/categories        # 分类列表
POST /api/knowledge                   # 创建知识（管理员）
PUT  /api/knowledge/{id}              # 更新知识（管理员）
DELETE /api/knowledge/{id}            # 删除知识（管理员）
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
| 消息队列 | RabbitMQ（积分/工单/SLA通知） |
| 全文检索 | Elasticsearch + IK中文分词（客服知识库） |

## RabbitMQ 消息设计

### Exchange 和 Queue

| Exchange | 类型 | Queue | 用途 |
|----------|------|-------|------|
| crm.miles.exchange | topic | crm.miles.sms.queue | 积分变动短信通知 |
| | | crm.miles.marketing.queue | 积分变动营销通知 |
| | | crm.miles.log.queue | 积分操作日志 |
| crm.workorder.exchange | direct | crm.wo.pek.queue | 首都机场工单派发 |
| | | crm.wo.sha.queue | 上海机场工单派发 |
| | | crm.wo.can.queue | 白云机场工单派发 |
| crm.wo.status.exchange | direct | crm.wo.status.queue | 工单状态变更 |
| crm.sla.exchange | fanout | crm.sla.alert.queue | SLA告警通知 |
| | | crm.sla.escalation.queue | SLA工单升级 |

### 消息触发场景

| 场景 | Exchange | 说明 |
|------|----------|------|
| 积分发放/扣减 | crm.miles.exchange | 通知短信、营销、日志 |
| 工单创建 | crm.workorder.exchange | 按机场代码路由派发 |
| 工单状态变更 | crm.wo.status.exchange | 通知状态更新 |
| SLA即将超时/已超时 | crm.sla.exchange | fanout广播所有告警队列 |

## Elasticsearch 客服知识库

### 索引设计

```
索引名：crm_knowledge
分词器：IK（ik_max_word 建索引，ik_smart 搜索）
```

| 字段 | 类型 | 分词器 | 说明 |
|------|------|--------|------|
| title | text | ik_max_word | 标题（权重3倍） |
| content | text | ik_smart | 内容（权重1倍） |
| keywords | text | ik_max_word | 关键词（权重2倍） |
| category | keyword | - | 分类（7大类） |
| tags | keyword | - | 标签列表 |
| priority | integer | - | 优先级排序 |
| viewCount | integer | - | 浏览量排序 |

### 知识分类（27条示例数据）

| 分类代码 | 名称 | 数量 | 内容示例 |
|----------|------|------|----------|
| PLATFORM | 平台信息 | 4条 | APP下载、官网联系方式、营业部地址、值机方式 |
| TICKET | 票规政策 | 3条 | 退改签收费、行李托运、购票渠道 |
| FAQ | 常见问题 | 5条 | 里程查询、密码重置、手机号修改、航班延误、儿童票 |
| SERVICE | 服务流程 | 4条 | 轮椅服务、贵宾休息室、接送机、特殊餐食 |
| BENEFITS | 权益说明 | 4条 | 白金卡权益、金卡权益、里程兑换、里程累积 |
| SCRIPT | 话术模板 | 4条 | 开场白、投诉处理、挽留话术、结束语 |
| SYSTEM | 系统操作 | 3条 | 工单处理、里程补登、等级升降级 |

### 搜索特性

| 特性 | 说明 |
|------|------|
| 中文分词 | "退改签" → "退" "改" "签" "退改签" |
| 高亮显示 | 搜索结果关键词 `<em>` 标红 |
| 相关性排序 | 标题×3 + 关键词×2 + 内容×1 |
| 分类筛选 | 支持按7大类筛选 |
| 热门排序 | 按浏览量降序 |

### 数据导入

#### 方式一：Python 脚本（推荐）

```bash
# 安装依赖
pip3 install openpyxl elasticsearch

# 生成完整示例数据（27条）
python3 scripts/create_full_knowledge.py

# 或从 Excel 导入
python3 scripts/import_knowledge.py data/knowledge.xlsx

# 创建 Excel 模板
python3 scripts/create_template.py
```

#### 方式二：Shell 脚本

```bash
# 查看 ES 数据
./scripts/es-query.sh stats    # 统计信息
./scripts/es-query.sh list     # 列出所有
./scripts/es-query.sh search "退改签"  # 搜索
./scripts/es-query.sh category TICKET  # 按分类
```

#### 方式三：API 导入

```bash
curl -X POST "http://localhost:8080/crm/api/knowledge/import" \
  -F "file=@knowledge.xlsx"
```

### Kibana 可视化

访问 http://localhost:5601

1. 创建 Index Pattern: `crm_knowledge*`
2. 选择时间字段: `createTime`
3. 在 Discover 查看和搜索数据
4. 可创建图表、仪表盘等可视化

---

## 设计文档

详见 [crm_read.md](crm_read.md)

## 开发记忆

详见 [docs/memory/](docs/memory/) — 记录了关键设计决策和开发过程
