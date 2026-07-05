# 国航CRM项目 - 简历包装 & 面试指南

> 适用级别：高级/资深 Java 开发工程师

---

## 一、简历项目经验

### 项目名称：国航凤凰知音高端旅客服务管理平台（CRM）

**项目背景**：
国航凤凰知音常旅客计划拥有会员约800万人，其中金卡会员12万人，白金卡会员1.5万人。原系统基于Spring MVC + Oracle单体架构，自2012年上线以来，面临性能瓶颈（积分查询高峰期响应>3秒）、模块耦合严重（修改积分模块影响工单模块）、扩展性不足（无法独立扩展高并发模块）等问题。

**项目职责**：
- 负责核心模块设计与开发：会员管理、积分系统、工单系统、退改签规则引擎
- 设计并实现客服知识库搜索引擎（Elasticsearch + IK中文分词），支撑客服人员快速检索
- 负责消息中间件架构设计（RabbitMQ），实现积分变动、工单派发、SLA告警等异步通知
- 主导缓存架构设计（Redis Cache Aside模式），优化查询性能
- 参与数据库设计与优化，负责索引设计、慢查询优化

**技术栈**：
Spring Boot 2.7、MyBatis-Plus、MySQL 8.0、Redis、RabbitMQ、Elasticsearch 7.17、Docker、Swagger

**项目成果**：
- 积分查询响应时间从3秒降至50ms（缓存命中），提升60倍
- 工单处理效率提升40%（状态机+模板匹配）
- 客服知识检索效率提升80%（ES全文搜索替代MySQL模糊查询）
- 系统可用性达到99.9%，支撑高峰期500 TPS并发

---

## 二、技术深度分析

### 2.1 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                            客户端层                               │
│  客服工作站(PC)    营业部系统(PC)    机场地服终端(移动/PC)        │
└────────────────────────┬────────────────────────────────────────┘
                         │ HTTP/HTTPS
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                           Nginx                                   │
│              反向代理 + 负载均衡 + HTTPS终端                      │
└────────────────────────┬────────────────────────────────────────┘
                         │ HTTP (8080)
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Spring Boot 应用                            │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │ 会员管理 │ │ 积分系统 │ │ 工单系统 │ │ 知识库   │           │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
└────────────────────────┬────────────────────────────────────────┘
          ┌──────────────┼──────────────┬──────────────┐
          ▼              ▼              ▼              ▼
   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
   │ MySQL    │   │ Redis    │   │ RabbitMQ │   │ ES       │
   │ 主从     │   │ 集群     │   │ 镜像队列 │   │ 集群     │
   └──────────┘   └──────────┘   └──────────┘   └──────────┘
```

### 2.2 业务逻辑

```
客户来电 → 来电弹屏(Customer360) → 查询会员信息
    │
    ├─→ 退改签请求 → 规则引擎计算费用 → 执行退改签
    │
    ├─→ 积分兑换 → 乐观锁扣减 → 写流水 → MQ通知
    │
    ├─→ 服务请求 → 创建工单 → 状态机流转 → MQ派发到机场
    │
    └─→ 咨询问题 → 知识库搜索(ES) → 返回答案
```

### 2.3 业务痛点与解决方案

| 痛点 | 问题描述 | 解决方案 | 技术实现 |
|------|---------|---------|---------|
| **性能瓶颈** | 积分查询高峰期响应>3秒 | Redis缓存 + 乐观锁 | Cache Aside + version CAS |
| **模块耦合** | 修改积分影响工单模块 | 服务解耦 + MQ异步 | RabbitMQ Topic/Direct/Fanout |
| **扩展性差** | 无法独立扩展高并发模块 | 接口隔离 + 预留Dubbo | Service接口+Impl分离 |
| **搜索弱** | MySQL模糊查询慢 | ES全文检索 | IK分词 + 相关性排序 |
| **状态混乱** | 工单状态流转不规范 | 状态机模式 | 合法状态定义 + 流转引擎 |
| **规则复杂** | 退改签规则多变 | 规则引擎 | 优先级匹配（精确→模糊→默认） |

---

## 三、面试题

### 3.1 Java 基础与设计模式

#### Q1：为什么积分扣减用乐观锁而不是悲观锁？

**标准答案**：

```
场景特点：
- 积分查询:扣减 = 100:1（读多写少）
- 同一会员同时扣减概率低（写冲突少）

悲观锁（SELECT FOR UPDATE）：
- 查询时锁行，其他事务等待
- 100次查询都要加锁解锁，性能差
- 适合写冲突频繁场景

乐观锁（version字段CAS）：
- 查询不加锁，扣减时才检查version
- 100次查询零开销，冲突时才重试
- 适合读多写少场景

实现：
UPDATE t_miles_account 
SET balance = balance - #{miles}, version = version + 1
WHERE member_id = #{memberId} AND version = #{version}

影响行数=0 → 版本冲突 → 重试（最多3次）
```

**追问**：
- 乐观锁的ABA问题怎么解决？（版本号单调递增）
- 重试3次还是失败怎么办？（抛异常，返回用户重试）
- 分布式环境下乐观锁有什么问题？（数据库压力大，可用Redis分布式锁）

---

#### Q2：状态机模式在工单系统中的应用？

**标准答案**：

```
状态定义：
CREATED → ASSIGNED → IN_PROGRESS → COMPLETED → CLOSED

合法流转：
FROM            TO                  触发场景
──────────────────────────────────────────────────
CREATED         ASSIGNED            客服指派
CREATED         CLOSED              客服取消
ASSIGNED        IN_PROGRESS         地服开始处理
ASSIGNED        CLOSED              地服拒绝
IN_PROGRESS     COMPLETED           服务完成
IN_PROGRESS     CLOSED              异常关闭
COMPLETED       CLOSED              归档

实现：
public class WorkOrderFlowEngine {
    // 定义合法流转
    private static Map<String, Set<String>> TRANSITIONS = Map.of(
        "CREATED", Set.of("ASSIGNED", "CLOSED"),
        "ASSIGNED", Set.of("IN_PROGRESS", "CLOSED"),
        "IN_PROGRESS", Set.of("COMPLETED", "CLOSED"),
        "COMPLETED", Set.of("CLOSED")
    );
    
    public void transit(String fromStatus, String toStatus) {
        if (!TRANSITIONS.get(fromStatus).contains(toStatus)) {
            throw new BizException("非法状态流转：" + fromStatus + " → " + toStatus);
        }
    }
}
```

**追问**：
- 如果要新增一个状态怎么办？（修改TRANSITIONS映射，不影响其他代码）
- 状态机和工作流引擎（如Activiti）的区别？（状态机简单轻量，适合固定流程；工作流引擎适合复杂审批流）
- 如何保证状态流转的事务性？（状态更新和流转记录在同一个事务）

---

#### Q3：规则引擎的设计与实现？

**标准答案**：

```
规则维度：
- 会员等级：GENERAL/SILVER/GOLD/PLATINUM
- 舱位：F（头等）/C（公务）/Y（经济）
- 变更类型：CHANGE/REFUND
- 距起飞时间：>24h / 2-24h / <2h

匹配策略（优先级从高到低）：
1. 精确匹配：会员等级 + 舱位 + 变更类型
2. 模糊匹配：会员等级 + 变更类型（舱位为空=通用）
3. 默认规则：兜底规则

实现：
public class TicketRuleEngine {
    public Rule match(String tier, String cabin, String changeType) {
        // 1. 精确匹配
        Rule rule = rules.stream()
            .filter(r -> r.matchExact(tier, cabin, changeType))
            .findFirst()
            .orElse(null);
        
        // 2. 模糊匹配（舱位为空）
        if (rule == null) {
            rule = rules.stream()
                .filter(r -> r.matchFuzzy(tier, changeType))
                .findFirst()
                .orElse(defaultRule);
        }
        
        return rule;
    }
}
```

**追问**：
- 规则引擎和策略模式的区别？（规则引擎关注规则匹配，策略模式关注算法替换）
- 如果规则非常多（1000+），如何优化匹配性能？（规则索引、缓存、预编译）
- 如何实现规则的热更新？（数据库存储 + 定时刷新 + 版本控制）

---

#### Q4：门面模式在Customer360中的应用？

**标准答案**：

```
问题：
客服来电弹屏需要调用多个服务：
- 会员服务 → 查询会员信息
- 积分服务 → 查询积分余额
- 工单服务 → 查询活跃工单

如果客服系统直接调用，会：
- 代码耦合多个服务
- 需要处理多个服务的异常
- 需要聚合多个返回结果

门面模式：
public class Customer360ServiceImpl implements Customer360Service {
    
    @Autowired
    private MemberService memberService;
    
    @Reference  // Dubbo
    private MilesService milesService;
    
    @Reference
    private WorkOrderService workOrderService;
    
    @Override
    public Customer360VO getCustomer360(Long memberId) {
        // 1. 查询会员信息（本地）
        MemberVO member = memberService.getMemberVOById(memberId);
        
        // 2. 查询积分（Dubbo调用）
        MilesBalanceVO miles = milesService.queryBalance(memberId);
        
        // 3. 查询工单（Dubbo调用）
        List<WorkOrder> orders = workOrderService.getActiveOrders(memberId);
        
        // 4. 聚合返回
        return Customer360VO.builder()
            .member(member)
            .miles(miles)
            .activeOrders(orders)
            .build();
    }
}
```

**追问**：
- 门面模式和中介者模式的区别？（门面简化接口，中介者协调交互）
- 如果某个服务调用失败怎么办？（容错降级：返回默认值或缓存数据）
- 如何优化聚合查询的性能？（并行调用CompletableFuture、缓存聚合结果）

---

### 3.2 数据库与缓存

#### Q5：MySQL索引设计原则？

**标准答案**：

```
本项目索引设计：

1. 会员表 t_member
   - PRIMARY KEY (member_id)
   - UNIQUE KEY uk_member_no (member_no)  -- 会员号唯一
   - KEY idx_mobile (mobile)              -- 手机号查询
   - KEY idx_name (name)                  -- 姓名查询
   - KEY idx_tier (tier)                  -- 等级筛选

2. 积分流水表 t_miles_transaction
   - KEY idx_member_tx (member_id, created_at)  -- 联合索引
   - KEY idx_expire (expire_at)                 -- 过期查询

3. 工单表 t_work_order
   - KEY idx_airport (airport_code, status)     -- 机场+状态
   - KEY idx_sla (sla_status, sla_deadline)     -- SLA监控

索引设计原则：
1. 最左前缀原则：联合索引(a,b,c)可以匹配(a)、(a,b)、(a,b,c)
2. 区分度高的列放前面：member_id > status
3. 避免索引失效：不在索引列上使用函数、类型转换、OR
4. 覆盖索引减少回表：SELECT的字段都在索引中
```

**追问**：
- 什么是索引下推（ICP）？（MySQL 5.6+，在索引层过滤，减少回表）
- 什么是覆盖索引？（查询字段都在索引中，无需回表）
- 如何分析SQL是否走索引？（EXPLAIN，看type、key、rows、Extra）

---

#### Q6：Redis缓存一致性如何保证？

**标准答案**：

```
采用 Cache Aside Pattern（旁路缓存模式）：

读取流程：
1. 先查Redis缓存
2. 命中 → 直接返回
3. 未命中 → 查MySQL → 写入Redis → 返回

更新流程：
1. 先更新MySQL
2. 再删除Redis（不是更新）
3. 下次读取时重新加载

为什么删除而不是更新缓存：
- 避免并发更新时缓存和数据库不一致
- 删除缓存成本更低
- 下次读取时惰性加载

代码实现：
public MemberVO getMemberVOById(Long memberId) {
    String cacheKey = "crm:member:info:" + memberId;
    
    // 1. 先查缓存
    try {
        MemberVO cached = redisCacheHelper.get(cacheKey, MemberVO.class);
        if (cached != null) {
            log.debug("缓存命中：memberId={}", memberId);
            return cached;
        }
    } catch (Exception e) {
        log.warn("Redis读取失败，降级查库：{}", e.getMessage());
    }
    
    // 2. 查库
    MemberVO member = memberMapper.selectById(memberId);
    
    // 3. 写缓存
    try {
        redisCacheHelper.set(cacheKey, member, 30, TimeUnit.MINUTES);
    } catch (Exception e) {
        log.warn("Redis写入失败：{}", e.getMessage());
    }
    
    return member;
}
```

**追问**：
- 缓存穿透、缓存击穿、缓存雪崩的区别和解决方案？
  - 穿透：查询不存在的数据 → 布隆过滤器、缓存空值
  - 击穿：热点key过期 → 互斥锁、永不过期
  - 雪崩：大量key同时过期 → 随机TTL、多级缓存
- 延迟双删策略是什么？（更新DB→删缓存→延迟→再删缓存）
- Redis和MySQL数据不一致怎么办？（最终一致性：MQ重试、定时对账）

---

#### Q7：Redis容错降级策略？

**标准答案**：

```
问题：
Redis不可用时，不能影响主业务

策略：
所有Redis操作try-catch，异常时降级查库

实现：
public <T> T get(String key, Class<T> clazz) {
    try {
        String json = redisTemplate.opsForValue().get(key);
        return JSON.parseObject(json, clazz);
    } catch (Exception e) {
        log.warn("Redis读取失败：{}", e.getMessage());
        return null;  // 降级：返回null，走数据库
    }
}

public void set(String key, Object value, long timeout, TimeUnit unit) {
    try {
        redisTemplate.opsForValue().set(key, JSON.toJSONString(value), timeout, unit);
    } catch (Exception e) {
        log.warn("Redis写入失败：{}", e.getMessage());
        // 降级：忽略，下次读取时重新加载
    }
}

public void delete(String key) {
    try {
        redisTemplate.delete(key);
    } catch (Exception e) {
        log.warn("Redis删除失败：{}", e.getMessage());
    }
}

监控：
- Redis连接池监控（活跃连接、空闲连接）
- 缓存命中率监控（命中/未命中）
- 降次数告警
```

**追问**：
- 如何监控Redis健康状态？（INFO命令、连接池指标、慢查询日志）
- 缓存降级后如何恢复？（自动重连、手动预热）
- 本地缓存（Caffeine）作为二级缓存？（L1本地 + L2 Redis，减少网络开销）

---

### 3.3 消息队列

#### Q8：RabbitMQ消息可靠性如何保证？

**标准答案**：

```
消息丢失的三个环节：
1. Producer → Exchange
2. Exchange → Queue
3. Queue → Consumer

解决方案：

1. Producer端（发送确认）
   spring.rabbitmq.publisher-confirm-type=correlated
   template.setConfirmCallback((correlationData, ack, cause) -> {
       if (!ack) log.error("消息发送失败: " + cause);
   });

2. Exchange端（持久化）
   ExchangeBuilder.topicExchange(EXCHANGE).durable(true)

3. Queue端（持久化）
   QueueBuilder.durable(QUEUE)

4. Consumer端（手动ACK）
   spring.rabbitmq.listener.simple.acknowledge-mode=manual
   
   @RabbitListener(queues = "xxx")
   public void handle(Message msg, Channel channel) {
       try {
           // 处理消息
           processMessage(msg);
           channel.basicAck(tag, false);
       } catch (Exception e) {
           log.error("消息处理失败", e);
           channel.basicNack(tag, false, true);  // 重新入队
       }
   }

5. 死信队列（消费失败处理）
   配置DLX，消费失败N次后进入死信队列，人工处理
```

**追问**：
- 什么是死信队列？什么场景使用？（消费失败、消息过期、队列满）
- 如何保证消息的幂等性？（消息ID去重、数据库唯一约束、Redis setnx）
- 如何处理消息积压？（增加消费者、临时队列、丢弃过期消息）

---

#### Q9：Exchange类型的区别和使用场景？

**标准答案**：

```
1. Direct Exchange（直连）
   - 路由键完全匹配
   - 场景：工单派发（按机场代码路由）
   
   Exchange: crm.workorder.exchange
   Queue: crm.wo.pek.queue ← binding key "PEK"
   Queue: crm.wo.sha.queue ← binding key "SHA"
   Queue: crm.wo.can.queue ← binding key "CAN"

2. Topic Exchange（主题）
   - 路由键模糊匹配（*一个词，#多个词）
   - 场景：积分通知（按类型路由）
   
   Exchange: crm.miles.exchange
   Queue: crm.miles.sms.queue ← "miles.sms.#"
   Queue: crm.miles.marketing.queue ← "miles.marketing.#"
   Queue: crm.miles.log.queue ← "miles.log.#"

3. Fanout Exchange（扇出）
   - 广播到所有绑定队列
   - 场景：SLA告警（通知多个系统）
   
   Exchange: crm.sla.exchange
   Queue: crm.sla.alert.queue
   Queue: crm.sla.escalation.queue

4. Headers Exchange（头匹配）
   - 根据消息头匹配
   - 场景：较少使用
```

**追问**：
- Topic和Direct的区别？（Direct精确匹配，Topic模糊匹配）
- 如何实现消息的顺序消费？（单消费者、消息哈希到同一队列）
- 如何实现消息的延迟消费？（TTL + 死信队列、延迟插件）

---

### 3.4 Elasticsearch

#### Q10：ES倒排索引原理？

**标准答案**：

```
正排索引（MySQL）：
文档ID → 文档内容
1 → "退改签收费标准"
2 → "行李托运规定"

搜索"退改签"：全表扫描，LIKE '%退改签%'

倒排索引（ES）：
词条 → 文档ID列表
"退" → [1]
"改" → [1]
"签" → [1]
"退改签" → [1]
"行李" → [2]
"托运" → [2]
"规定" → [1, 2]

搜索"退改签"：
1. 分词："退改签" → ["退", "改", "签", "退改签"]
2. 查倒排索引：取文档ID并集
3. 返回文档1

IK分词器：
- ik_max_word：最细粒度分词（建索引时使用）
  "退改签收费标准" → "退改签", "退", "改", "签", "收费", "标准"
- ik_smart：智能分词（搜索时使用）
  "退改签收费标准" → "退改签", "收费", "标准"
```

**追问**：
- TF-IDF算法是什么？（词频-逆文档频率，衡量词条重要性）
- 什么是词频、文档频率？（词频=词条在文档出现次数，文档频率=包含词条的文档数）
- 如何优化ES查询性能？（合理设置分片数、使用filter代替query、避免深分页）

---

#### Q11：ES相关性排序原理？

**标准答案**：

```
相关性评分（_score）计算：

1. TF（词频 - Term Frequency）
   词条在文档中出现的次数越多，评分越高
   "退改签"出现2次 vs 出现1次 → 前者评分高

2. IDF（逆文档频率 - Inverse Document Frequency）
   词条在越少的文档中出现，评分越高
   "退改签"只在1篇文档出现 vs 在100篇出现 → 前者评分高

3. 字段权重（Boost）
   title^3 > keywords^2 > content^1

4. 文档长度
   短文档匹配相同词条，评分更高

实现：
{
  "query": {
    "multi_match": {
      "query": "退改签",
      "fields": ["title^3", "keywords^2", "content"]
    }
  },
  "highlight": {
    "fields": {
      "title": {},
      "content": {}
    }
  }
}

优化：
- boost参数调整权重
- function_score自定义评分
- script_score脚本评分
```

**追问**：
- 什么是BM25算法？（ES默认评分算法，改进TF-IDF）
- 如何自定义评分规则？（function_score、script_score）
- 如何优化搜索结果的相关性？（调整权重、添加同义词、优化数据）

---

#### Q12：ES与MySQL的数据同步方案？

**标准答案**：

```
方案一：同步双写
- 写MySQL成功后，立即写ES
- 优点：实时性强
- 缺点：代码耦合，分布式事务

方案二：异步双写（MQ）
- 写MySQL成功后，发MQ消息
- Consumer消费消息，写ES
- 优点：解耦，可靠性高
- 缺点：有延迟（秒级）

方案三：Canal监听Binlog
- Canal监听MySQL Binlog
- 解析变更事件，同步到ES
- 优点：代码无侵入，实时性好
- 缺点：引入额外组件，运维成本

方案四：定时任务
- 定时扫描MySQL变更数据（按更新时间）
- 批量同步到ES
- 优点：简单可靠
- 缺点：延迟大（分钟级）

本项目方案：
- 知识库数据更新频率低（月级）
- 采用脚本批量导入（Python + ES API）
- 运营人员整理Excel → 脚本导入 → ES
```

**追问**：
- Canal的原理是什么？（伪装MySQL从库，接收Binlog）
- 如何保证ES和MySQL的最终一致性？（重试、对账、补偿）
- 如果ES写入失败怎么办？（MQ重试、死信队列、人工处理）

---

### 3.5 系统设计

#### Q13：如何设计一个高并发的积分系统？

**标准答案**：

```
挑战：
- 查询QPS：800+（高峰期）
- 扣减QPS：100+
- 数据一致性要求高

架构设计：

1. 缓存层（Redis）
   - 积分余额缓存（10min TTL）
   - 查询走缓存，<50ms
   - Key: miles:balance:{memberId}

2. 数据层（MySQL）
   - 乐观锁扣减（version CAS）
   - 失败重试3次
   - 流水表记录所有变动

3. 消息层（RabbitMQ）
   - 扣减成功后发MQ消息
   - 异步通知短信、营销、日志

4. 监控层
   - 扣减成功率监控
   - 缓存命中率监控
   - 余额一致性校验

流程：
查询：Redis → 命中返回 → 未命中查MySQL → 写Redis
扣减：查Redis余额 → 乐观锁扣MySQL → 删Redis → 发MQ

优化：
- 热点账户拆分（多行记录，随机选择）
- 本地缓存（Caffeine）二级缓存
- 异步写流水（MQ）
- 批量扣减（减少DB交互）
```

**追问**：
- 什么是热点账户问题？如何解决？（单行记录竞争激烈 → 拆分多行）
- 分布式事务如何保证？（最终一致性：MQ + 补偿 + 对账）
- 如何实现积分过期自动扣减？（定时任务扫描 + MQ通知）

---

#### Q14：如何设计客服知识库搜索引擎？

**标准答案**：

```
需求：
- 1000~5000条知识文档
- 支持中文分词
- 搜索结果高亮
- 按相关性排序
- 支持分类筛选

技术选型：
- Elasticsearch 7.17 + IK分词器
- 为什么不用MySQL？LIKE '%xx%'全表扫描，无分词
- 为什么不用MongoDB？全文检索能力弱

索引设计：
{
  "mappings": {
    "properties": {
      "title": {"type": "text", "analyzer": "ik_max_word"},
      "content": {"type": "text", "analyzer": "ik_smart"},
      "category": {"type": "keyword"},
      "tags": {"type": "keyword"},
      "keywords": {"type": "text", "analyzer": "ik_max_word"},
      "priority": {"type": "integer"},
      "viewCount": {"type": "integer"},
      "createTime": {"type": "date"}
    }
  }
}

搜索策略：
- 多字段搜索：title^3 + keywords^2 + content
- 高亮显示：pre_tags/post_tags
- 分类筛选：term query
- 排序：_score + priority + viewCount

数据导入：
- Python脚本读取Excel
- 批量写入ES（epoch_millis时间格式）
- 支持增量更新（按标题去重）
```

**追问**：
- ES的分片策略？（单分片适合小数据量，多分片适合大数据量）
- 如何优化ES的写入性能？（批量API、调整刷新间隔、关闭副本）
- 如何实现搜索建议（自动补全）？（Completion Suggester）

---

#### Q15：如何设计工单的SLA监控？

**标准答案**：

```
需求：
- 监控工单SLA状态
- 即将超时告警（≤30分钟）
- 已超时告警
- 自动升级

设计：

1. SLA状态定义
   - NORMAL：剩余>30分钟
   - AT_RISK：剩余≤30分钟
   - BREACHED：已超时

2. 定时扫描
   @Scheduled(fixedRate = 5 * 60 * 1000)  // 每5分钟
   public void scanSlaStatus() {
       // 查询活跃工单
       List<WorkOrder> orders = getActiveOrders();
       
       for (WorkOrder order : orders) {
           SlaStatus newStatus = calculateStatus(order.getSlaDeadline());
           
           if (newStatus != order.getSlaStatus()) {
               // 更新状态
               updateSlaStatus(order, newStatus);
               
               // 发送告警
               sendAlert(order, newStatus);
           }
       }
   }

3. 告警通知
   - MQ消息 → 钉钉/邮件
   - fanout广播，多个消费者

4. 自动升级
   - BREACHED工单自动提升优先级
   - 通知客服主管
```

**追问**：
- 定时任务的实现方式？（@Scheduled、Quartz、XXL-Job）
- 如何避免重复扫描？（分布式锁、任务状态标记）
- SLA时间如何计算？（创建时间 + SLA时限，排除非工作时间）

---

### 3.6 部署与运维

#### Q16：Docker部署架构？

**标准答案**：

```
服务列表：
┌─────────────────────────────────────────────────────┐
│  服务名称          端口        用途                   │
├─────────────────────────────────────────────────────┤
│  MySQL            3307        业务数据库             │
│  Redis            6379        缓存                  │
│  RabbitMQ         5672/15672  消息队列               │
│  Elasticsearch    9200        搜索引擎              │
│  Kibana           5601        ES可视化              │
│  CRM应用          8080        后端API               │
└─────────────────────────────────────────────────────┘

Docker命令：
# MySQL
docker run -d --name mysql -p 3307:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 mysql:8.0

# Redis
docker run -d --name redis -p 6379:6379 redis:7-alpine

# RabbitMQ
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# Elasticsearch
docker run -d --name elasticsearch \
  -p 9200:9200 -e "discovery.type=single-node" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  elasticsearch:7.17.18

# Kibana
docker run -d --name kibana \
  -p 5601:5601 \
  -e "ELASTICSEARCH_HOSTS=http://host.docker.internal:9200" \
  kibana:7.17.18
```

**追问**：
- Docker和虚拟机的区别？（容器共享内核，更轻量）
- 什么是Docker Compose？如何使用？（YAML定义多容器编排）
- 如何实现容器的健康检查？（HEALTHCHECK指令）

---

#### Q17：生产环境部署方案？

**标准答案**：

```
架构设计：
┌─────────────────────────────────────────────────────┐
│                      DMZ区                           │
│  Nginx (主备) → 反向代理 + 负载均衡                  │
│  IP白名单 + 限流 + HTTPS终端                         │
└────────────────────────┬────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────┐
│                      应用区                           │
│  crm-web × 4 (4C8G)                                 │
│  miles-service × 2 (4C8G)                           │
│  workorder-service × 2 (4C8G)                       │
└────────────────────────┬────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────┐
│                      数据区                           │
│  MySQL主从 (8C32G)                                   │
│  Redis集群 3主3从 (4C8G)                             │
│  RabbitMQ镜像队列 (4C8G)                             │
│  ES集群 3节点 (4C16G)                                │
└─────────────────────────────────────────────────────┘

高可用设计：
- 应用层：Nginx负载均衡，多实例部署
- 数据层：MySQL主从复制，Redis哨兵/集群
- 消息层：RabbitMQ镜像队列
- 搜索层：ES集群，分片+副本

监控：
- Prometheus + Grafana监控指标
- ELK收集日志
- 告警：钉钉/邮件
```

**追问**：
- Nginx的负载均衡策略？（轮询、权重、ip_hash、least_conn）
- MySQL主从复制原理？（Binlog → RelayLog → 重放）
- 如何实现灰度发布？（Nginx权重、Cookie标记、Header标记）

---

### 3.7 场景题

#### Q18：线上积分扣减失败率高，如何排查？

**标准答案**：

```
排查步骤：

1. 查看日志
   - 搜索"乐观锁冲突"、"版本冲突"
   - 统计重试次数和失败率
   - 查看是否有异常堆栈

2. 分析原因
   - 高并发场景：同一会员被多次扣减
   - 缓存问题：Redis和MySQL余额不一致
   - 网络问题：扣减成功但返回超时
   - 代码问题：事务未提交就删除缓存

3. 解决方案
   - 短期：增加重试次数（3→5）
   - 中期：热点账户拆分（多行记录）
   - 长期：引入分布式锁（Redisson）

4. 监控告警
   - 扣减失败率 > 1% 告警
   - 重试次数 > 3 告警
   - 积分余额不一致告警（定时对账）

5. 复盘优化
   - 热点账户识别
   - 限流策略调整
   - 缓存预热
```

---

#### Q19：知识库搜索结果不准确，如何优化？

**标准答案**：

```
问题分析：
1. 分词不准确 → 调整IK词典
2. 权重不合理 → 调整boost参数
3. 数据质量差 → 优化数据内容
4. 查询方式不对 → 优化查询DSL

优化方案：

1. 词典优化
   - 添加自定义词典：国航、凤凰知音、白金卡、金卡
   - 添加同义词词典：退票=退签，改票=改签，行李=箱子
   - 热更新词典（无需重启ES）

2. 权重调整
   - title^3 → title^5（标题更重要）
   - 添加exact_match（完全匹配加分）
   - 使用function_score自定义评分

3. 数据优化
   - 补充keywords字段（人工标注）
   - 优化content结构（分段、标题）
   - 添加搜索热门度字段

4. 搜索优化
   - 添加拼音搜索（pinyin分词器）
   - 添加搜索建议（completion suggester）
   - 记录搜索日志，分析热门搜索

5. A/B测试
   - 对比不同算法的点击率
   - 持续优化排序规则
```

---

#### Q20：系统扛不住高并发，如何优化？

**标准答案**：

```
分层优化：

1. 接入层
   - Nginx缓存静态资源
   - 限流（limit_req、令牌桶）
   - IP白名单
   - CDN加速

2. 应用层
   - 多实例部署（4→8）
   - JVM调优（堆大小、GC策略）
   - 线程池调优（核心线程、最大线程、队列）
   - 异步处理（CompletableFuture）

3. 缓存层
   - Redis集群（3主3从）
   - 本地缓存（Caffeine）二级缓存
   - 缓存预热
   - 热点数据永不过期

4. 数据层
   - MySQL读写分离
   - 分库分表（按会员ID）
   - SQL优化（索引、慢查询）
   - 连接池调优

5. 消息层
   - 异步处理（MQ）
   - 削峰填谷
   - 消息批量消费

6. 搜索层
   - ES集群扩容
   - 分片策略优化
   - 查询缓存

监控指标：
- QPS/TPS
- 响应时间（P50、P99）
- 错误率
- 系统负载（CPU、内存、磁盘、网络）
```

---

## 四、面试技巧

### 4.1 回答框架（STAR法则）

```
S（Situation）：项目背景
T（Task）：你的职责
A（Action）：你的行动
R（Result）：项目成果

示例：
S：国航CRM系统，800万会员，原系统性能瓶颈
T：负责积分系统设计，需要支持800 TPS
A：采用Redis缓存 + 乐观锁 + MQ异步通知
R：查询响应从3秒降至50ms，扣减成功率99.9%
```

### 4.2 技术深度展示

```
不要只说"用了什么"，要说"为什么用"和"怎么用的"

❌ "我用了Redis缓存"
✅ "我采用了Cache Aside模式，因为读多写少场景；所有Redis操作try-catch做容错降级；缓存TTL根据业务特点设置（会员30min、积分10min）"
```

### 4.3 问题解决能力

```
展示思路：
1. 问题是什么？（现象）
2. 原因是什么？（分析）
3. 怎么解决？（方案）
4. 效果如何？（结果）

示例：
问题：积分查询高峰期响应>3秒
原因：MySQL全表扫描 + 无缓存
方案：Redis缓存 + 索引优化
效果：响应时间降至50ms
```

### 4.4 常见追问应对

```
1. "为什么不用xxx？"
   → 对比分析，说明选型理由

2. "如果xxx怎么办？"
   → 展示容错思维，说明降级方案

3. "如何优化xxx？"
   → 分层优化，从架构到代码

4. "有什么缺点？"
   → 诚实回答，说明权衡取舍
```

---

## 五、技术栈速查表

| 技术 | 版本 | 用途 | 核心知识点 |
|------|------|------|-----------|
| Spring Boot | 2.7.18 | 应用框架 | 自动配置、Starter、Actuator |
| MyBatis-Plus | 3.5.3.1 | ORM | 代码生成、分页、乐观锁 |
| MySQL | 8.0 | 数据库 | 索引、事务、锁、慢查询 |
| Redis | 单机 | 缓存 | 数据结构、过期策略、持久化 |
| RabbitMQ | 3.x | 消息队列 | Exchange、Queue、ACK |
| Elasticsearch | 7.17 | 搜索引擎 | 倒排索引、分词、相关性排序 |
| Docker | - | 容器化 | 镜像、容器、网络、存储 |

---

## 六、项目数据速查

| 指标 | 数值 | 说明 |
|------|------|------|
| 会员规模 | 800万 | 金卡12万，白金卡1.5万 |
| 积分查询QPS | 800+ | 缓存命中后 |
| 积分扣减QPS | 100+ | 乐观锁+重试 |
| 知识库数据 | 27条 | 7大分类 |
| 工单状态 | 5个 | 状态机管理 |
| API接口 | 26个 | Swagger文档 |
| 缓存TTL | 5-30min | 按业务设置 |
| SLA告警 | 5分钟 | 定时扫描 |

---

文档生成时间：2026-07-04
