# 面试题详细解答

> 针对高级/资深Java开发，深入讲解核心概念

---

## 一、MySQL 索引相关

### Q1：什么是索引下推（ICP - Index Condition Pushdown）？

**一句话理解**：把原本在Server层过滤的数据，下推到存储引擎层过滤，减少回表次数。

---

#### 没有ICP之前（MySQL 5.6之前）

```
假设有一张表：
CREATE TABLE user (
    id INT PRIMARY KEY,
    name VARCHAR(50),
    age INT,
    INDEX idx_name_age (name, age)
);

执行查询：
SELECT * FROM user WHERE name LIKE '张%' AND age = 25;

执行流程：
1. 存储引擎层：通过索引 idx_name_age 找到所有 name LIKE '张%' 的记录
2. 存储引擎层：对每条记录回表（根据主键ID去聚簇索引查完整数据）
3. Server层：拿到完整数据后，再过滤 age = 25

问题：
如果 name LIKE '张%' 匹配1000条，但 age = 25 只有10条
→ 回表1000次，但最终只返回10条
→ 浪费了990次回表操作
```

#### 有了ICP之后（MySQL 5.6+）

```
执行流程：
1. 存储引擎层：通过索引 idx_name_age 找到所有 name LIKE '张%' 的记录
2. 存储引擎层：在索引层直接过滤 age = 25（不用回表！）
3. 存储引擎层：只对满足条件的10条记录回表
4. Server层：直接返回结果

优化：
回表次数从1000次降到10次，性能提升100倍
```

#### 如何查看是否使用了ICP？

```sql
EXPLAIN SELECT * FROM user WHERE name LIKE '张%' AND age = 25;

-- Extra列会显示：Using index condition
-- 说明使用了ICP
```

#### ICP的使用条件

```
1. 只适用于范围查询（>、<、BETWEEN、LIKE）
2. 只适用于联合索引
3. 只适用于SELECT *（需要回表的场景）
4. 不适用于覆盖索引（已经不需要回表了）
```

---

### Q2：什么是覆盖索引（Covering Index）？

**一句话理解**：查询需要的所有字段都在索引中，不需要回表。

---

#### 普通查询（需要回表）

```
表结构：
CREATE TABLE user (
    id INT PRIMARY KEY,
    name VARCHAR(50),
    age INT,
    email VARCHAR(100),
    INDEX idx_name (name)
);

查询：
SELECT name, age, email FROM user WHERE name = '张三';

执行流程：
1. 在idx_name索引中找到name='张三'的记录，拿到主键ID
2. 根据主键ID回表，到聚簇索引中查完整数据
3. 返回name、age、email

问题：需要回表，性能较差
```

#### 覆盖索引（无需回表）

```
优化：创建联合索引
ALTER TABLE user ADD INDEX idx_name_age_email (name, age, email);

查询：
SELECT name, age, email FROM user WHERE name = '张三';

执行流程：
1. 在idx_name_age_email索引中找到name='张三'的记录
2. 索引中已经包含了name、age、email，直接返回
3. 不需要回表！

EXPLAIN结果：
Extra: Using index  -- 说明使用了覆盖索引
```

#### 覆盖索引的优势

```
1. 减少回表：性能提升明显
2. 减少IO：索引比聚簇索引小得多
3. 减少随机IO：顺序读索引 vs 随机读数据页

实际应用：
-- 用户表：手机号查姓名（高频）
CREATE INDEX idx_mobile_name ON user(mobile, name);

-- 积分流水：查最近10条的类型和积分
CREATE INDEX idx_member_tx_type_miles ON miles_transaction(member_id, tx_type, miles);
```

---

### Q3：什么是联合索引的最左前缀原则？

**一句话理解**：联合索引(a,b,c)只能从最左边开始匹配，不能跳过中间字段。

---

#### 联合索引结构

```
CREATE INDEX idx_a_b_c ON table(a, b, c);

索引结构（B+树）：
先按a排序 → a相同按b排序 → b相同按c排序

a=1, b=1, c=1
a=1, b=1, c=2
a=1, b=2, c=1
a=2, b=1, c=1
a=2, b=1, c=2
```

#### 哪些查询能用到索引？

```sql
-- ✅ 能用到索引
WHERE a = 1                    -- 匹配a
WHERE a = 1 AND b = 2          -- 匹配a,b
WHERE a = 1 AND b = 2 AND c = 3  -- 匹配a,b,c
WHERE a = 1 AND b > 2          -- 匹配a，b走范围
WHERE a = 1 ORDER BY b         -- 匹配a，b用于排序

-- ❌ 不能用到索引
WHERE b = 2                    -- 跳过了a
WHERE c = 3                    -- 跳过了a,b
WHERE b = 2 AND c = 3          -- 跳过了a
WHERE a = 1 OR b = 2           -- OR可能导致索引失效

-- ⚠️ 部分用到索引
WHERE a = 1 AND c = 3          -- 只用到a，c无法用索引（跳过了b）
WHERE a = 1 AND b LIKE 'x%'   -- 用到a,b前缀
WHERE a = 1 AND b > 2 AND c = 3  -- 用到a,b，c无法用索引（b是范围）
```

#### 为什么跳过中间字段就不能用？

```
索引结构：先按a排序 → a相同按b排序 → b相同按c排序

如果只查c=3：
- 索引是按a,b,c排序的
- c=3的记录分散在不同的a,b组合中
- 无法利用索引的有序性，只能全表扫描

类比：
字典按拼音排序（声母→韵母→声调）
如果要查"第3声的字"，必须翻遍整本字典
因为"第3声"不是排序的第一维度
```

---

### Q4：MySQL事务隔离级别有哪些？

**一句话理解**：隔离级别越高，数据越安全，但性能越差。

---

#### 四种隔离级别

```
级别              脏读    不可重复读    幻读    说明
─────────────────────────────────────────────────────
READ UNCOMMITTED  ✅有    ✅有         ✅有    最低，几乎不用
READ COMMITTED    ❌无    ✅有         ✅有    Oracle默认
REPEATABLE READ   ❌无    ❌无         ⚠️部分  MySQL默认
SERIALIZABLE      ❌无    ❌无         ❌无    最高，性能最差
```

#### 什么是脏读？

```
事务A：更新了age=25，但还没提交
事务B：读取到age=25（脏数据）
事务A：回滚了，age还是20
事务B：拿着age=25去做业务，出错了！

解决：READ COMMITTED
```

#### 什么是不可重复读？

```
事务A：第一次读age=20
事务B：更新age=25，提交了
事务A：第二次读age=25（同一个事务内，两次读结果不同）

解决：REPEATABLE READ
```

#### 什么是幻读？

```
事务A：第一次查age>20，返回10条
事务B：插入一条age=25，提交了
事务A：第二次查age>20，返回11条（多了一条幻影）

解决：SERIALIZABLE（性能差）或 MVCC+间隙锁（MySQL方案）
```

#### MySQL如何实现REPEATABLE READ？

```
1. MVCC（多版本并发控制）
   - 每条记录有多个版本
   - 事务开始时生成一个ReadView
   - 只能看到ReadView之前提交的数据

2. 间隙锁（Next-Key Lock）
   - 锁住记录之间的间隙
   - 阻止其他事务插入数据
   - 解决幻读问题
```

---

### Q5：MySQL的锁机制？

**一句话理解**：不同粒度、不同类型的锁，解决并发问题。

---

#### 锁的粒度

```
1. 表锁：锁住整张表
   - 开销小，加锁快
   - 粒度大，并发低
   - 适合：DDL操作

2. 行锁：锁住某一行
   - 开销大，加锁慢
   - 粒度小，并发高
   - 适合：OLTP业务

3. 页锁：锁住某一页（BDB引擎）
   - 介于表锁和行锁之间
```

#### 行锁的类型

```
1. 记录锁（Record Lock）
   - 锁住某一条记录
   - SELECT * FROM user WHERE id = 1 FOR UPDATE;

2. 间隙锁（Gap Lock）
   - 锁住记录之间的间隙
   - 防止其他事务插入数据
   - 解决幻读问题

3. 临键锁（Next-Key Lock）
   - 记录锁 + 间隙锁
   - 锁住记录及其前面的间隙
   - InnoDB默认使用

示例：
表中有id = 1, 5, 10 的记录

SELECT * FROM user WHERE id = 7 FOR UPDATE;
-- 间隙锁：锁住(5, 10)这个区间
-- 其他事务无法插入id = 6, 7, 8, 9
```

#### 乐观锁 vs 悲观锁

```
悲观锁：
- 假设会冲突，先加锁再操作
- SELECT ... FOR UPDATE
- 适合写多读少场景

乐观锁：
- 假设不会冲突，提交时检查版本
- UPDATE ... WHERE version = ?
- 适合读多写少场景

本项目选择：
积分系统（读多写少）→ 乐观锁
工单系统（写操作多）→ 悲观锁
```

---

## 二、Redis 相关

### Q6：Redis有哪些数据结构？各自的应用场景？

**一句话理解**：不同数据结构解决不同问题，选对了事半功倍。

---

#### 五种基础数据结构

```
1. String（字符串）
   - 最基础的数据结构
   - 场景：缓存、计数器、分布式锁
   
   SET user:1:name "张三"
   GET user:1:name
   INCR article:1:views  -- 阅读数+1
   SETNX lock:order:123 1  -- 分布式锁

2. Hash（哈希）
   - 字段值对，适合存储对象
   - 场景：用户信息、商品详情
   
   HSET user:1 name "张三" age 25
   HGET user:1 name
   HGETALL user:1

3. List（列表）
   - 双向链表，支持左右操作
   - 场景：消息队列、最新列表
   
   LPUSH timeline:1 "msg1"  -- 左插入
   RPUSH timeline:1 "msg2"  -- 右插入
   LRANGE timeline:1 0 9   -- 获取最新10条
   LPOP timeline:1          -- 左弹出

4. Set（集合）
   - 无序、不重复
   - 场景：标签、共同好友、去重
   
   SADD user:1:tags "java" "redis" "mysql"
   SMEMBERS user:1:tags
   SINTER user:1:tags user:2:tags  -- 交集（共同标签）
   SUNION user:1:tags user:2:tags  -- 并集

5. ZSet（有序集合）
   - 有序、不重复、带分数
   - 场景：排行榜、延迟队列
   
   ZADD rank:2024 100 "user1"  -- 添加，分数100
   ZADD rank:2024 200 "user2"
   ZREVRANGE rank:2024 0 9  -- 前10名（按分数降序）
   ZINCRBY rank:2024 10 "user1"  -- 分数+10
```

#### 高级数据结构

```
1. Bitmap（位图）
   - 场景：用户签到、在线状态
   
   SETBIT sign:user:1:202401 1 1  -- 1月2日签到
   GETBIT sign:user:1:202401 1   -- 查询1月2日是否签到
   BITCOUNT sign:user:1:202401  -- 1月签到天数

2. HyperLogLog（基数统计）
   - 场景：UV统计（去重计数）
   
   PFADD uv:20240101 "user1" "user2" "user1"
   PFCOUNT uv:20240101  -- 结果：2（去重后）

3. Geo（地理位置）
   - 场景：附近的人、门店
   
   GEOADD user:loc 116.40 39.90 "user1"
   GEORADIUS user:loc 116.40 39.90 5 km  -- 5公里内的用户
```

---

### Q7：Redis的过期策略和内存淘汰策略？

**一句话理解**：过期策略决定key什么时候删除，淘汰策略决定内存满了删谁。

---

#### 过期策略

```
1. 惰性删除
   - 访问key时才检查是否过期
   - 优点：CPU友好
   - 缺点：内存浪费（过期但没访问的key一直占内存）

2. 定期删除
   - 每隔一段时间随机检查一批key
   - 删除其中过期的key
   - 优点：平衡CPU和内存
   - 缺点：可能漏掉一些过期key

Redis实际使用：惰性删除 + 定期删除（两者结合）
```

#### 内存淘汰策略（maxmemory-policy）

```
8种策略：

1. noeviction（默认）
   - 不淘汰，内存满了直接报错
   - 适合：数据不能丢的场景

2. allkeys-lru
   - 所有key中，淘汰最近最少使用的
   - 适合：缓存场景（推荐）

3. volatile-lru
   - 设置了过期时间的key中，淘汰最近最少使用的
   - 适合：缓存+持久化混合场景

4. allkeys-random
   - 所有key中，随机淘汰
   - 适合：各key访问频率相近

5. volatile-random
   - 设置了过期时间的key中，随机淘汰

6. allkeys-lfu
   - 所有key中，淘汰访问频率最低的
   - 适合：热点数据场景

7. volatile-lfu
   - 设置了过期时间的key中，淘汰访问频率最低的

8. volatile-ttl
   - 设置了过期时间的key中，淘汰TTL最短的

本项目配置：
maxmemory-policy allkeys-lru  -- 缓存场景，推荐LRU
```

---

### Q8：Redis持久化方式？RDB vs AOF？

**一句话理解**：RDB是全量快照，AOF是增量日志。

---

#### RDB（Redis Database）

```
原理：
- 把内存数据写入磁盘，生成一个快照文件（dump.rdb）
- fork子进程，利用COW（写时复制）

触发方式：
- 自动：配置规则（save 900 1, save 300 10, save 60 10000）
- 手动：SAVE（阻塞）或 BGSAVE（后台）

优点：
- 文件小，恢复快
- 适合备份和灾难恢复

缺点：
- 可能丢失最后一次快照后的数据
- fork时内存翻倍（COW）
```

#### AOF（Append Only File）

```
原理：
- 把每条写命令追加到日志文件（appendonly.aof）
- 恢复时重放所有命令

同步策略：
- always：每条命令都同步（最安全，最慢）
- everysec：每秒同步一次（推荐，最多丢1秒数据）
- no：由操作系统决定（最快，可能丢数据）

优点：
- 数据安全性高（最多丢1秒）
- 文件可读，可手动修复

缺点：
- 文件大（需要定期重写）
- 恢复慢（重放所有命令）

重写机制：
- AOF文件太大时，自动触发重写
- 只保留最终数据的写命令
```

#### 如何选择？

```
推荐：RDB + AOF 同时开启

1. RDB用于备份和灾难恢复
2. AOF用于数据恢复（最多丢1秒）

Redis 4.0+混合持久化：
- 重写时，AOF文件前半段是RDB格式
- 后半段是增量AOF命令
- 兼顾恢复速度和数据安全
```

---

## 三、消息队列相关

### Q9：如何保证消息的幂等性？

**一句话理解**：同一条消息消费多次，结果和消费一次一样。

---

#### 为什么需要幂等性？

```
场景：
1. Producer发送消息，网络超时，重试发送
2. Consumer消费成功，但ACK丢失，MQ重新投递
3. Consumer重启，重新消费未ACK的消息

问题：
- 积分扣减消息消费两次 → 扣了两次积分
- 工单派发消息消费两次 → 派发了两次

解决：幂等性设计
```

#### 幂等性方案

```
方案一：消息ID去重
- 每条消息带唯一ID
- 消费前检查ID是否已消费
- 用Redis SETNX实现

public void consume(Message msg) {
    String msgId = msg.getId();
    
    // 检查是否已消费
    if (redisTemplate.opsForValue().setIfAbsent("msg:" + msgId, "1", 24, TimeUnit.HOURS)) {
        // 未消费，处理消息
        processMessage(msg);
    } else {
        log.info("消息已消费，跳过：{}", msgId);
    }
}

方案二：数据库唯一约束
- 利用数据库唯一索引
- 重复插入会报错

INSERT INTO miles_transaction (tx_id, member_id, miles) 
VALUES ('tx123', 1, 100);
-- 重复插入会报DuplicateKeyException

方案三：状态机
- 利用状态流转的幂等性
- 已完成的工单不能再完成

UPDATE work_order 
SET status = 'COMPLETED' 
WHERE order_id = 1 AND status = 'IN_PROGRESS';
-- 如果已经是COMPLETED，影响行数为0

方案四：Token机制
- 业务方先申请Token
- 携带Token请求，服务端校验并删除Token
- 重复请求Token已不存在
```

---

### Q10：如何处理消息积压？

**一句话理解**：消息生产速度 > 消费速度，导致队列堆积。

---

#### 消息积压的原因

```
1. Consumer处理太慢
   - 业务逻辑复杂
   - 数据库慢查询
   - 外部服务超时

2. Consumer挂了
   - 服务宕机
   - 内存溢出
   - 死循环

3. 突发流量
   - 秒杀活动
   - 营销推送
```

#### 解决方案

```
紧急处理：
1. 增加Consumer数量
   - 水平扩展，增加消费者实例
   - 注意：不能超过队列数

2. 临时队列
   - 新建临时队列
   - 把消息转发到临时队列
   - 用更多Consumer消费临时队列

3. 跳过过期消息
   - 如果消息有时效性
   - 直接丢弃过期消息

长期优化：
1. 优化Consumer处理速度
   - 异步处理非关键逻辑
   - 批量消费
   - 数据库优化

2. 预防措施
   - 监控队列长度
   - 设置告警阈值
   - 限流削峰

3. 架构优化
   - 读写分离
   - 分库分表
   - 缓存热点数据
```

---

## 四、Elasticsearch 相关

### Q11：ES的分片（Shard）是什么？如何设置？

**一句话理解**：分片是把索引数据拆分成多份，分布在不同节点上。

---

#### 为什么需要分片？

```
问题：
- 单节点存储容量有限（比如1TB）
- 单节点查询性能有限
- 单节点故障会丢失数据

解决：分片
- 把数据拆分成多个分片
- 每个分片是一个独立的Lucene索引
- 分片分布在不同节点上
```

#### 主分片和副本分片

```
主分片（Primary Shard）：
- 存储实际数据
- 数量在创建索引时确定，不能修改
- 写入操作只能在主分片上执行

副本分片（Replica Shard）：
- 主分片的复制
- 数量可以动态修改
- 用于查询和故障转移

示例：
PUT /my_index
{
  "settings": {
    "number_of_shards": 3,      -- 3个主分片
    "number_of_replicas": 2     -- 每个主分片2个副本
  }
}

总共：3个主分片 + 6个副本分片 = 9个分片
```

#### 分片数量如何设置？

```
经验公式：
- 单个分片大小：10GB~50GB
- 分片数量 = 数据量 / 单分片大小

示例：
- 预计数据量：100GB
- 分片数量：100GB / 30GB ≈ 3~5个分片

本项目：
- 知识库数据量：27条，很小
- 分片数量：1个主分片，1个副本
- 足够了，不需要多分片

注意：
- 分片数量创建后不能修改（除非reindex）
- 分片太多：浪费资源，查询变慢
- 分片太少：无法充分利用集群
```

---

### Q12：ES的查询和过滤（Query vs Filter）区别？

**一句话理解**：Query计算相关性评分，Filter只做过滤不计算评分。

---

#### Query（查询）

```
特点：
- 计算相关性评分（_score）
- 结果按评分排序
- 不会被缓存

示例：
{
  "query": {
    "match": {
      "title": "退改签"
    }
  }
}

评分计算：
- TF-IDF算法
- 词条出现频率
- 词条在多少文档中出现

适合场景：
- 全文搜索
- 需要相关性排序
```

#### Filter（过滤）

```
特点：
- 不计算评分
- 结果不排序
- 会被缓存（bitset）
- 性能更好

示例：
{
  "query": {
    "bool": {
      "filter": [
        { "term": { "status": 1 }},
        { "term": { "category": "TICKET" }}
      ]
    }
  }
}

适合场景：
- 精确匹配
- 范围查询
- 不需要评分的场景
```

#### 组合使用

```
{
  "query": {
    "bool": {
      "must": [
        { "match": { "title": "退改签" }}  -- Query：计算评分
      ],
      "filter": [
        { "term": { "category": "TICKET" }},  -- Filter：不计算评分
        { "range": { "createTime": { "gte": "2024-01-01" }}}
      ]
    }
  }
}

优点：
- 先过滤（快速缩小范围）
- 再查询（计算评分）
- 性能最优
```

---

## 五、JVM 相关

### Q13：JVM内存结构？

**一句话理解**：堆存对象，栈存方法，方法区存类信息。

---

#### JVM内存分区

```
┌─────────────────────────────────────────────────────────────┐
│                          JVM内存                              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    堆（Heap）                         │   │
│  │  ┌──────────────┐  ┌──────────────────────────────┐ │   │
│  │  │  新生代       │  │  老年代                       │ │   │
│  │  │ ┌────┬────┐  │  │                              │ │   │
│  │  │ │Eden│S0  │S1│  │                              │ │   │
│  │  │ └────┴────┘  │  │                              │ │   │
│  │  └──────────────┘  └──────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  虚拟机栈     │  │  本地方法栈   │  │  程序计数器   │      │
│  │  (线程私有)   │  │  (线程私有)   │  │  (线程私有)   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                方法区（Method Area）                   │   │
│  │  类信息、常量、静态变量、JIT编译后的代码               │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

#### 堆内存详解

```
新生代（Young Generation）：
- Eden区：新对象分配在这里
- S0（Survivor 0）：GC后存活的对象
- S1（Survivor 1）：GC后存活的对象
- 比例：Eden:S0:S1 = 8:1:1

老年代（Old Generation）：
- 存活时间长的对象
- 从新生代晋升过来的

GC流程：
1. 新对象分配在Eden
2. Eden满了 → Minor GC
3. 存活对象移到S0或S1
4. 年龄+1，达到阈值（默认15）→ 晋升老年代
5. 老年代满了 → Major GC / Full GC
```

#### 常用JVM参数

```
-Xms512m          -- 初始堆大小
-Xmx1024m         -- 最大堆大小
-Xmn256m          -- 新生代大小
-XX:SurvivorRatio=8  -- Eden:S0:S1 = 8:1:1
-XX:MaxTenuringThreshold=15  -- 晋升老年代的年龄阈值
-XX:+UseG1GC      -- 使用G1垃圾收集器
-XX:MaxGCPauseMillis=200  -- G1目标停顿时间

本项目配置：
-Xms512m -Xmx512m  -- 测试环境
-Xms2g -Xmx2g      -- 生产环境
```

---

### Q14：常见的垃圾收集器？

**一句话理解**：不同收集器适合不同场景，没有最好的，只有最合适的。

---

#### 垃圾收集器对比

```
收集器        算法        区域        特点                适合场景
─────────────────────────────────────────────────────────────────────────
Serial        复制/标记整理  新生代/老年代  单线程，简单         客户端、小内存
ParNew        复制         新生代       多线程版本Serial     配合CMS
Parallel      复制/标记整理  新生代/老年代  多线程，吞吐量优先   后台计算
CMS           标记清除      老年代       低停顿              Web应用
G1            分区+复制     全堆         平衡停顿和吞吐量     大内存（推荐）
ZGC           染色指针      全堆         超低停顿（<10ms）    超大内存
```

#### CMS收集器

```
阶段：
1. 初始标记（STW）：标记GC Roots直接关联的对象
2. 并发标记：从GC Roots开始遍历整个对象图
3. 重新标记（STW）：修正并发标记期间变动的对象
4. 并发清除：清除不可达对象

优点：
- 并发收集，低停顿

缺点：
- CPU敏感（占用CPU资源）
- 浮动垃圾（并发清除期间产生的垃圾）
- 内存碎片（标记清除算法）
```

#### G1收集器（推荐）

```
特点：
- 把堆划分为多个Region（1MB~32MB）
- 每个Region可以是Eden、Survivor、Old、Humongous
- 优先回收垃圾最多的Region（Garbage First）
- 可预测的停顿时间

阶段：
1. 初始标记（STW）
2. 并发标记
3. 最终标记（STW）
4. 筛选回收（STW）：选择回收价值高的Region

配置：
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200  -- 目标停顿时间
-XX:G1HeapRegionSize=16m  -- Region大小
```

---

## 六、Spring 相关

### Q15：Spring Bean的生命周期？

**一句话理解**：创建 → 初始化 → 使用 → 销毁

---

#### 完整生命周期

```
1. 实例化（Instantiation）
   - 通过构造函数创建对象
   - 此时还未注入依赖

2. 属性赋值（Populate）
   - 注入依赖（@Autowired）
   - 设置属性值

3. 初始化（Initialization）
   - 调用Aware接口方法（BeanNameAware、BeanFactoryAware等）
   - 调用BeanPostProcessor.postProcessBeforeInitialization
   - 调用@PostConstruct注解方法
   - 调用InitializingBean.afterPropertiesSet
   - 调用自定义init-method
   - 调用BeanPostProcessor.postProcessAfterInitialization

4. 使用
   - Bean准备就绪，可以被注入使用

5. 销毁（Destruction）
   - 调用@PreDestroy注解方法
   - 调用DisposableBean.destroy
   - 调用自定义destroy-method
```

#### 代码示例

```java
@Component
public class MyBean implements InitializingBean, DisposableBean {
    
    @Autowired
    private SomeService someService;
    
    @PostConstruct
    public void postConstruct() {
        System.out.println("1. @PostConstruct");
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("2. InitializingBean.afterPropertiesSet");
    }
    
    public void customInit() {
        System.out.println("3. 自定义init-method");
    }
    
    @Override
    public void destroy() throws Exception {
        System.out.println("1. DisposableBean.destroy");
    }
    
    @PreDestroy
    public void preDestroy() {
        System.out.println("2. @PreDestroy");
    }
}
```

---

### Q16：Spring循环依赖如何解决？

**一句话理解**：三级缓存，提前暴露未完全初始化的Bean。

---

#### 什么是循环依赖？

```
@Service
public class A {
    @Autowired
    private B b;
}

@Service
public class B {
    @Autowired
    private A a;
}

问题：A依赖B，B依赖A，形成循环
```

#### 三级缓存

```
一级缓存（singletonObjects）：
- 完全初始化好的Bean
- 可以直接使用

二级缓存（earlySingletonObjects）：
- 提前暴露的Bean（已实例化，未初始化）
- 用于解决循环依赖

三级缓存（singletonFactories）：
- Bean的工厂（ObjectFactory）
- 用于生成代理对象
```

#### 解决流程

```
1. 创建A
   - 实例化A（调用构造函数）
   - 把A的工厂放入三级缓存
   - 填充属性，发现依赖B

2. 创建B
   - 实例化B
   - 把B的工厂放入三级缓存
   - 填充属性，发现依赖A
   - 从三级缓存获取A的工厂，生成A的早期引用
   - 把A的早期引用放入二级缓存
   - B初始化完成，放入一级缓存

3. 完成A
   - 把B注入A
   - A初始化完成，放入一级缓存
```

#### 哪些情况无法解决？

```
1. 构造函数注入
   - 无法提前暴露，必须等构造完成
   
2. Prototype作用域
   - 每次都创建新实例，无法缓存
   
3. @Async + 循环依赖
   - 代理对象和原始对象不一致
```

---

文档生成时间：2026-07-04
