# 面试准备（二）：语言语法 + 算法题

> 目标岗位：阅安科技 H-开发工程师
> 语言优先级：Python > Go > Java > C++
> 算法难度：LeetCode Easy-Medium，每题 10-15 分钟

---

## 一、Python 语法（高频）

### 1.1 数据类型与内存

#### Q1：Python 的可变类型和不可变类型？

```python
# 不可变类型（值不能修改，修改会创建新对象）
int, float, str, tuple, frozenset

# 可变类型（值可以修改，id 不变）
list, dict, set

# 验证
a = [1, 2, 3]
print(id(a))  # 140234567890
a.append(4)
print(id(a))  # 140234567890（同一个对象）

b = "hello"
print(id(b))  # 140234567890
b += " world"
print(id(b))  # 140234567891（新对象）
```

**面试官追问**：函数默认参数用可变类型会有什么问题？

```python
# 经典坑
def append_to(element, target=[]):
    target.append(element)
    return target

append_to(1)  # [1]
append_to(2)  # [1, 2]（不是 [2]！）

# 原因：默认参数在函数定义时创建，每次调用共享同一个 list
# 正确写法
def append_to(element, target=None):
    if target is None:
        target = []
    target.append(element)
    return target
```

---

#### Q2：深拷贝和浅拷贝？

```python
import copy

# 浅拷贝：只拷贝外层对象，内层对象共享引用
a = [[1, 2], [3, 4]]
b = copy.copy(a)
b[0].append(99)
print(a)  # [[1, 2, 99], [3, 4]]（内层被修改！）

# 深拷贝：递归拷贝所有层，完全独立
a = [[1, 2], [3, 4]]
b = copy.deepcopy(a)
b[0].append(99)
print(a)  # [[1, 2], [3, 4]]（不受影响）
```

---

### 1.2 GIL 与多线程

#### Q3：GIL 是什么？为什么有 GIL？

```python
# GIL（Global Interpreter Lock）全局解释器锁
# CPython 中同一时刻只有一个线程执行 Python 字节码

# 为什么有 GIL：
# 1. 简化 CPython 内存管理（引用计数）
# 2. 避免多线程并发修改引用计数导致内存泄漏
# 3. 历史原因，移除 GIL 会破坏 C 扩展兼容性

# GIL 的影响：
# - CPU 密集型：多线程无法利用多核（反而更慢，因为线程切换开销）
# - I/O 密集型：不受影响（I/O 等待时释放 GIL）

# 解决方案：
# - CPU 密集型：multiprocessing 多进程
# - I/O 密集型：asyncio 异步 / threading 多线程
# - Python 3.13+：实验性移除 GIL（--disable-gil 编译）
```

---

#### Q4：multiprocessing vs threading vs asyncio？

```python
# threading：多线程，I/O 密集型
import threading

def download(url):
    # I/O 操作
    pass

threads = [threading.Thread(target=download, args=(url,)) for url in urls]
for t in threads:
    t.start()
for t in threads:
    t.join()

# multiprocessing：多进程，CPU 密集型
from multiprocessing import Pool

def compute(n):
    # CPU 密集计算
    return sum(i*i for i in range(n))

with Pool(4) as p:
    results = p.map(compute, [10**6, 10**6, 10**6, 10**6])

# asyncio：协程，I/O 密集型（高并发）
import asyncio
import aiohttp

async def fetch(url, semaphore):
    async with semaphore:
        async with aiohttp.ClientSession() as session:
            async with session.get(url) as resp:
                return await resp.text()

async def main():
    semaphore = asyncio.Semaphore(100)
    tasks = [fetch(url, semaphore) for url in urls]
    results = await asyncio.gather(*tasks)

asyncio.run(main())

# 选型：
# I/O 密集 + 高并发（万级）→ asyncio
# I/O 密集 + 低并发 → threading
# CPU 密集 → multiprocessing
```

---

### 1.3 装饰器

#### Q5：装饰器是什么？写一个带参数的装饰器？

```python
# 基础装饰器
import time
from functools import wraps

def timer(func):
    @wraps(func)  # 保留原函数的 __name__, __doc__
    def wrapper(*args, **kwargs):
        start = time.time()
        result = func(*args, **kwargs)
        print(f"{func.__name__} took {time.time() - start:.3f}s")
        return result
    return wrapper

@timer
def heavy_computation():
    time.sleep(1)
    return "done"

# 带参数的装饰器
def retry(max_retries=3, delay=1):
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            for i in range(max_retries):
                try:
                    return func(*args, **kwargs)
                except Exception as e:
                    if i == max_retries - 1:
                        raise
                    print(f"Retry {i+1}/{max_retries}: {e}")
                    time.sleep(delay)
        return wrapper
    return decorator

@retry(max_retries=3, delay=2)
def unreliable_api():
    # 可能失败的 API 调用
    pass
```

---

### 1.4 生成器与迭代器

#### Q6：生成器和迭代器区别？

```python
# 迭代器：实现了 __iter__() 和 __next__() 的对象
class Counter:
    def __init__(self, max):
        self.max = max
        self.current = 0
    
    def __iter__(self):
        return self
    
    def __next__(self):
        if self.current >= self.max:
            raise StopIteration
        self.current += 1
        return self.current

# 生成器：用 yield 的函数，自动实现迭代器协议
def counter(max):
    current = 0
    while current < max:
        current += 1
        yield current

# 生成器表达式（惰性求值）
gen = (x**2 for x in range(1000000))  # 不立即计算
next(gen)  # 0
next(gen)  # 1

# 适用场景：
# - 数据量大，不需要一次性加载到内存
# - 流式处理，逐个处理数据
# - 无限序列（斐波那契数列）
```

---

### 1.5 元类与描述符

#### Q7：元类是什么？什么时候用？

```python
# 元类：创建类的类
# type 是所有类的元类

# 普通方式
class MyClass:
    x = 1

# 等价于
MyClass = type('MyClass', (), {'x': 1})

# 自定义元类
class SingletonMeta(type):
    _instances = {}
    
    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            cls._instances[cls] = super().__call__(*args, **kwargs)
        return cls._instances[cls]

class Database(metaclass=SingletonMeta):
    def __init__(self):
        self.connection = "connected"

db1 = Database()
db2 = Database()
print(db1 is db2)  # True（单例）

# 什么时候用元类：
# - ORM 框架（Django Model、SQLAlchemy）
# - 单例模式
# - API 验证（自动检查方法签名）
# - 一般业务代码很少用
```

---

### 1.6 Python 3 新特性

#### Q8：f-string、walrus operator、match-case？

```python
# f-string（Python 3.6+）
name = "Alice"
age = 30
print(f"{name} is {age} years old")
print(f"{2 + 2 = }")  # 2 + 2 = 4（调试神器）

# walrus operator :=（Python 3.8+）
# 在表达式中赋值
data = [1, 2, 3, 4, 5]
if (n := len(data)) > 3:
    print(f"List has {n} elements")

# match-case（Python 3.10+）
def handle_response(status):
    match status:
        case 200:
            return "OK"
        case 404:
            return "Not Found"
        case 500:
            return "Server Error"
        case _:
            return "Unknown"

# Type Hints（Python 3.5+）
def greet(name: str) -> str:
    return f"Hello, {name}"

from typing import List, Dict, Optional

def process(items: List[int]) -> Dict[str, int]:
    return {"sum": sum(items), "count": len(items)}
```

---

## 二、Go 语法（高频）

### 2.1 基础

#### Q9：Go 的 slice 和 array 区别？

```go
// 数组：固定长度，值类型
var arr [5]int = [5]int{1, 2, 3, 4, 5}
arr2 := arr  // 拷贝，修改 arr2 不影响 arr

// 切片：动态长度，引用类型
s := []int{1, 2, 3}
s = append(s, 4)  // 自动扩容

// 切片底层：指针 + 长度 + 容量
type slice struct {
    array unsafe.Pointer  // 指向底层数组
    len   int             // 长度
    cap   int             // 容量
}

// 切片扩容策略：
// 容量 < 1024：翻倍
// 容量 >= 1024：增长 25%

// 切片陷阱
s1 := []int{1, 2, 3, 4, 5}
s2 := s1[1:3]  // s2 和 s1 共享底层数组
s2[0] = 99
fmt.Println(s1)  // [1, 99, 3, 4, 5]（s1 被修改！）

// 安全做法：copy
s2 = make([]int, 2)
copy(s2, s1[1:3])
```

---

#### Q10：goroutine 和 channel？

```go
// goroutine：轻量级协程
go func() {
    fmt.Println("Hello from goroutine")
}()

// channel：goroutine 间通信
ch := make(chan int)       // 无缓冲（同步）
ch := make(chan int, 100)  // 有缓冲（异步）

// 发送和接收
ch <- 42      // 发送
val := <-ch   // 接收

// select：多路复用
select {
case msg := <-ch1:
    fmt.Println("Received from ch1:", msg)
case msg := <-ch2:
    fmt.Println("Received from ch2:", msg)
case <-time.After(5 * time.Second):
    fmt.Println("Timeout")
}

// 关闭 channel
close(ch)
val, ok := <-ch  // ok=false 表示 channel 已关闭

// range 遍历 channel
for val := range ch {
    fmt.Println(val)
}
```

---

#### Q11：Go 的 context 怎么用？

```go
// context：控制 goroutine 生命周期
// 传递取消信号、超时、截止时间

// 1. WithCancel：手动取消
ctx, cancel := context.WithCancel(context.Background())
go func(ctx context.Context) {
    for {
        select {
        case <-ctx.Done():
            fmt.Println("Cancelled:", ctx.Err())
            return
        default:
            // do work
        }
    }
}(ctx)
cancel()  // 取消

// 2. WithTimeout：超时自动取消
ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
defer cancel()
result, err := fetchWithContext(ctx, url)

// 3. WithValue：传递值（谨慎使用）
ctx = context.WithValue(ctx, "request_id", "12345")
requestID := ctx.Value("request_id").(string)

// 最佳实践：
// - context 作为函数第一个参数
// - 不要把 context 存在 struct 里
// - 只传递请求级别的值（request_id、trace_id）
```

---

#### Q12：Go 的 interface？

```go
// interface：隐式实现（鸭子类型）
type Writer interface {
    Write(data []byte) (int, error)
}

type FileWriter struct{}
func (fw FileWriter) Write(data []byte) (int, error) {
    // 写文件
    return len(data), nil
}

// FileWriter 自动实现 Writer 接口，不需要显式声明
var w Writer = FileWriter{}
w.Write([]byte("hello"))

// 空接口：任何类型都实现
func printAnything(v interface{}) {
    fmt.Println(v)
}

// 类型断言
func process(v interface{}) {
    switch val := v.(type) {
    case int:
        fmt.Println("int:", val)
    case string:
        fmt.Println("string:", val)
    default:
        fmt.Println("unknown:", val)
    }
}

// 最佳实践：
// - 接口越小越好（1-3 个方法）
// - 在消费者端定义接口，不在生产者端
// - 用 interface{} 接收任意类型（Go 1.18+ 用泛型）
```

---

### 2.2 并发

#### Q13：sync 包常用工具？

```go
import "sync"

// 1. Mutex：互斥锁
var mu sync.Mutex
var counter int

func increment() {
    mu.Lock()
    defer mu.Unlock()
    counter++
}

// 2. RWMutex：读写锁
var rwMu sync.RWMutex

func read() int {
    rwMu.RLock()         // 读锁（共享）
    defer rwMu.RUnlock()
    return counter
}

func write(val int) {
    rwMu.Lock()          // 写锁（独占）
    defer rwMu.Unlock()
    counter = val
}

// 3. WaitGroup：等待一组 goroutine
var wg sync.WaitGroup

for i := 0; i < 10; i++ {
    wg.Add(1)
    go func(id int) {
        defer wg.Done()
        fmt.Println("Worker", id)
    }(i)
}
wg.Wait()

// 4. Once：只执行一次
var once sync.Once

func init() {
    once.Do(func() {
        // 只执行一次（单例初始化）
    })
}

// 5. Pool：对象池（复用对象，减少 GC）
var pool = sync.Pool{
    New: func() interface{} {
        return make([]byte, 1024)
    },
}

buf := pool.Get().([]byte)
defer pool.Put(buf)
```

---

## 三、Java 语法（基础）

### 3.1 集合

#### Q14：HashMap 底层原理？

```java
// JDK 1.8：数组 + 链表 + 红黑树

// 结构
Node[] table  // 数组
├── [0] → null
├── [1] → Node → Node → Node（链表）
├── [2] → TreeNode（红黑树，链表长度>8时转换）
├── [3] → null
└── ...

// put 流程
1. 计算 key 的 hash：hash = key.hashCode() ^ (key.hashCode() >>> 16)
2. 计算数组下标：index = hash & (table.length - 1)
3. 如果 table[index] 为空，直接插入
4. 如果 table[index] 不为空：
   - 如果是链表，遍历链表，key 相同则覆盖，否则尾插
   - 如果是红黑树，按红黑树插入
5. 链表长度 > 8 且数组长度 >= 64，转红黑树
6. 容量 > 阈值（容量 * 负载因子），扩容（2倍）

// 为什么用红黑树不用 AVL 树：
// 红黑树插入/删除旋转次数少（最多3次）
// AVL 树更严格平衡，查询快但插入/删除慢
// HashMap 插入/删除频繁，红黑树更合适
```

---

#### Q15：ConcurrentHashMap 怎么保证线程安全？

```java
// JDK 1.7：分段锁（Segment）
// 每个 Segment 是一个小 HashMap，独立加锁
// 并发度 = Segment 数量（默认16）

// JDK 1.8：CAS + synchronized
// 1. 计算 hash，找到数组下标
// 2. 如果该位置为空，CAS 插入（无锁）
// 3. 如果该位置不为空，synchronized 锁住该节点
// 4. 链表操作在锁内完成

// CAS（Compare And Swap）：
// 比较并交换，CPU 原子指令
// 如果当前值 == 预期值，则更新为新值
// 无锁，性能比 synchronized 高

// 为什么 JDK 1.8 放弃分段锁：
// 分段锁粒度太粗（锁整个 Segment）
// CAS + synchronized 粒度更细（锁单个节点）
// 并发性能更好
```

---

### 3.2 并发

#### Q16：synchronized vs ReentrantLock？

```java
// synchronized：JVM 内置锁
// - 自动获取/释放
// - 不可中断
// - 非公平锁
// - 可重入

// ReentrantLock：JUC 包
// - 手动 lock()/unlock()
// - 可中断 lockInterruptibly()
// - 可选公平/非公平
// - 可重入
// - 支持 Condition（精确唤醒）

// 选型：
// 简单同步 → synchronized（JVM 优化，性能好）
// 需要中断/超时/公平/条件等待 → ReentrantLock

// 示例：ReentrantLock + Condition
class BoundedBuffer {
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private final Object[] items = new Object[100];
    int putptr, takeptr, count;

    public void put(Object x) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length)
                notFull.await();  // 满了等待
            items[putptr] = x;
            if (++putptr == items.length) putptr = 0;
            ++count;
            notEmpty.signal();  // 通知可以取
        } finally {
            lock.unlock();
        }
    }
}
```

---

## 四、C++ 语法（基础）

### 4.1 智能指针

#### Q17：C++ 智能指针有哪些？

```cpp
#include <memory>

// 1. unique_ptr：独占所有权，不可拷贝
std::unique_ptr<int> p1 = std::make_unique<int>(42);
// std::unique_ptr<int> p2 = p1;  // 编译错误！
std::unique_ptr<int> p2 = std::move(p1);  // 转移所有权

// 2. shared_ptr：共享所有权，引用计数
std::shared_ptr<int> p1 = std::make_shared<int>(42);
std::shared_ptr<int> p2 = p1;  // 引用计数 +1
// p1 和 p2 都销毁时，内存释放

// 3. weak_ptr：弱引用，不影响引用计数
std::weak_ptr<int> wp = p1;
if (auto sp = wp.lock()) {  // 尝试提升为 shared_ptr
    std::cout << *sp << std::endl;
}

// 你的项目：shared_mutex 读写分离
// shared_ptr 管理缓存数据
std::shared_mutex cache_mutex;
std::unordered_map<uint64_t, std::shared_ptr<Data>> cache;

// 读操作（共享锁）
std::shared_lock<std::shared_mutex> read_lock(cache_mutex);
auto it = cache.find(key);

// 写操作（独占锁）
std::unique_lock<std::shared_mutex> write_lock(cache_mutex);
cache[key] = data;
```

---

### 4.2 移动语义

#### Q18：std::move 是什么？

```cpp
// std::move：将左值转换为右值引用
// 触发移动构造函数/移动赋值运算符
// 避免深拷贝，转移资源所有权

class Buffer {
    int* data;
    size_t size;
public:
    // 移动构造函数
    Buffer(Buffer&& other) noexcept 
        : data(other.data), size(other.size) {
        other.data = nullptr;  // 转移所有权
        other.size = 0;
    }
};

Buffer b1(100);
Buffer b2 = std::move(b1);  // 移动，b1 不再有效

// 你的项目：数据点编码
// 移动语义避免大数据拷贝
std::vector<DataPoint> points = loadPoints();
processPoints(std::move(points));  // 转移 vector 所有权
```

---

## 五、算法题（Python 实现）

### 5.1 哈希表

#### 题目 1：两数之和（LeetCode 1）

```
给定 nums = [2, 7, 11, 15]，target = 9
返回 [0, 1]（因为 nums[0] + nums[1] = 9）

思路：哈希表存储已遍历的值
时间 O(n)，空间 O(n)
```

```python
def twoSum(nums, target):
    seen = {}  # val -> index
    for i, num in enumerate(nums):
        complement = target - num
        if complement in seen:
            return [seen[complement], i]
        seen[num] = i
    return []
```

```go
// Go 实现
func twoSum(nums []int, target int) []int {
    seen := make(map[int]int)
    for i, num := range nums {
        if j, ok := seen[target-num]; ok {
            return []int{j, i}
        }
        seen[num] = i
    }
    return nil
}
```

---

#### 题目 2：有效的括号（LeetCode 20）

```
给定 "()" "([])" "(]" "{[]}"
判断括号是否有效

思路：栈
遇到左括号入栈，遇到右括号匹配栈顶
```

```python
def isValid(s):
    stack = []
    mapping = {')': '(', ']': '[', '}': '{'}
    
    for char in s:
        if char in mapping:
            # 右括号，检查栈顶
            if not stack or stack[-1] != mapping[char]:
                return False
            stack.pop()
        else:
            # 左括号，入栈
            stack.append(char)
    
    return len(stack) == 0
```

```go
// Go 实现
func isValid(s string) bool {
    stack := []rune{}
    mapping := map[rune]rune{')': '(', ']': '[', '}': '{'}
    
    for _, char := range s {
        if match, ok := mapping[char]; ok {
            if len(stack) == 0 || stack[len(stack)-1] != match {
                return false
            }
            stack = stack[:len(stack)-1]
        } else {
            stack = append(stack, char)
        }
    }
    
    return len(stack) == 0
}
```

---

### 5.2 链表

#### 题目 3：合并两个有序链表（LeetCode 21）

```
输入：1->2->4, 1->3->4
输出：1->1->2->3->4->4

思路：双指针，比较两个链表的当前节点
```

```python
class ListNode:
    def __init__(self, val=0, next=None):
        self.val = val
        self.next = next

def mergeTwoLists(l1, l2):
    dummy = ListNode(0)
    curr = dummy
    
    while l1 and l2:
        if l1.val <= l2.val:
            curr.next = l1
            l1 = l1.next
        else:
            curr.next = l2
            l2 = l2.next
        curr = curr.next
    
    curr.next = l1 or l2
    return dummy.next
```

```go
// Go 实现
type ListNode struct {
    Val  int
    Next *ListNode
}

func mergeTwoLists(l1, l2 *ListNode) *ListNode {
    dummy := &ListNode{}
    curr := dummy
    
    for l1 != nil && l2 != nil {
        if l1.Val <= l2.Val {
            curr.Next = l1
            l1 = l1.Next
        } else {
            curr.Next = l2
            l2 = l2.Next
        }
        curr = curr.Next
    }
    
    if l1 != nil {
        curr.Next = l1
    } else {
        curr.Next = l2
    }
    
    return dummy.Next
}
```

---

#### 题目 4：反转链表（LeetCode 206）

```
输入：1->2->3->4->5
输出：5->4->3->2->1

思路：三指针（prev, curr, next）
```

```python
def reverseList(head):
    prev = None
    curr = head
    
    while curr:
        next_tmp = curr.next  # 保存下一个
        curr.next = prev      # 反转指针
        prev = curr           # prev 前进
        curr = next_tmp       # curr 前进
    
    return prev
```

```go
func reverseList(head *ListNode) *ListNode {
    var prev *ListNode
    curr := head
    
    for curr != nil {
        next := curr.Next
        curr.Next = prev
        prev = curr
        curr = next
    }
    
    return prev
}
```

---

### 5.3 栈与队列

#### 题目 5：用栈实现队列（LeetCode 232）

```
思路：两个栈
入队：push 到 stack1
出队：如果 stack2 为空，把 stack1 全部倒入 stack2，pop stack2
```

```python
class MyQueue:
    def __init__(self):
        self.stack1 = []  # 入队栈
        self.stack2 = []  # 出队栈
    
    def push(self, x):
        self.stack1.append(x)
    
    def pop(self):
        self._transfer()
        return self.stack2.pop()
    
    def peek(self):
        self._transfer()
        return self.stack2[-1]
    
    def empty(self):
        return not self.stack1 and not self.stack2
    
    def _transfer(self):
        if not self.stack2:
            while self.stack1:
                self.stack2.append(self.stack1.pop())
```

---

### 5.4 哈希

#### 题目 6：LRU 缓存（LeetCode 146）

```
设计 LRU 缓存，支持 get 和 put，O(1) 时间复杂度

思路：双向链表 + 哈希表
- 哈希表：key -> node（快速查找）
- 双向链表：维护访问顺序（最近访问在头部）
```

```python
class Node:
    def __init__(self, key=0, val=0):
        self.key = key
        self.val = val
        self.prev = None
        self.next = None

class LRUCache:
    def __init__(self, capacity):
        self.capacity = capacity
        self.cache = {}  # key -> Node
        
        # 哨兵节点，简化边界处理
        self.head = Node()
        self.tail = Node()
        self.head.next = self.tail
        self.tail.prev = self.head
    
    def _remove(self, node):
        """从链表中移除节点"""
        node.prev.next = node.next
        node.next.prev = node.prev
    
    def _add_to_head(self, node):
        """在头部插入节点"""
        node.next = self.head.next
        node.prev = self.head
        self.head.next.prev = node
        self.head.next = node
    
    def get(self, key):
        if key not in self.cache:
            return -1
        
        node = self.cache[key]
        self._remove(node)
        self._add_to_head(node)
        return node.val
    
    def put(self, key, value):
        if key in self.cache:
            node = self.cache[key]
            node.val = value
            self._remove(node)
            self._add_to_head(node)
        else:
            node = Node(key, value)
            self.cache[key] = node
            self._add_to_head(node)
            
            if len(self.cache) > self.capacity:
                # 移除尾部节点
                lru = self.tail.prev
                self._remove(lru)
                del self.cache[lru.key]
```

```go
// Go 实现（用 container/list）
type LRUCache struct {
    capacity int
    cache    map[int]*list.Element
    list     *list.List
}

type entry struct {
    key, val int
}

func Constructor(capacity int) LRUCache {
    return LRUCache{
        capacity: capacity,
        cache:    make(map[int]*list.Element),
        list:     list.New(),
    }
}

func (c *LRUCache) Get(key int) int {
    if e, ok := c.cache[key]; ok {
        c.list.MoveToFront(e)
        return e.Value.(*entry).val
    }
    return -1
}

func (c *LRUCache) Put(key, value int) {
    if e, ok := c.cache[key]; ok {
        e.Value.(*entry).val = value
        c.list.MoveToFront(e)
        return
    }
    
    if c.list.Len() == c.capacity {
        e := c.list.Back()
        c.list.Remove(e)
        delete(c.cache, e.Value.(*entry).key)
    }
    
    e := c.list.PushFront(&entry{key, value})
    c.cache[key] = e
}
```

---

### 5.5 双指针

#### 题目 7：三数之和（LeetCode 15）

```
给定 nums = [-1, 0, 1, 2, -1, -4]
返回 [[-1, -1, 2], [-1, 0, 1]]

思路：排序 + 双指针
时间 O(n²)，空间 O(1)
```

```python
def threeSum(nums):
    nums.sort()
    result = []
    
    for i in range(len(nums) - 2):
        # 跳过重复
        if i > 0 and nums[i] == nums[i-1]:
            continue
        
        left, right = i + 1, len(nums) - 1
        
        while left < right:
            total = nums[i] + nums[left] + nums[right]
            
            if total < 0:
                left += 1
            elif total > 0:
                right -= 1
            else:
                result.append([nums[i], nums[left], nums[right]])
                # 跳过重复
                while left < right and nums[left] == nums[left+1]:
                    left += 1
                while left < right and nums[right] == nums[right-1]:
                    right -= 1
                left += 1
                right -= 1
    
    return result
```

---

### 5.6 滑动窗口

#### 题目 8：无重复字符的最长子串（LeetCode 3）

```
给定 "abcabcbb"
输出 3（"abc"）

思路：滑动窗口 + 哈希表记录字符位置
```

```python
def lengthOfLongestSubstring(s):
    char_index = {}  # char -> last seen index
    max_len = 0
    left = 0
    
    for right, char in enumerate(s):
        if char in char_index and char_index[char] >= left:
            left = char_index[char] + 1
        
        char_index[char] = right
        max_len = max(max_len, right - left + 1)
    
    return max_len
```

```go
func lengthOfLongestSubstring(s string) int {
    charIndex := make(map[byte]int)
    maxLen, left := 0, 0
    
    for right := 0; right < len(s); right++ {
        if idx, ok := charIndex[s[right]]; ok && idx >= left {
            left = idx + 1
        }
        charIndex[s[right]] = right
        if curLen := right - left + 1; curLen > maxLen {
            maxLen = curLen
        }
    }
    
    return maxLen
}
```

---

### 5.7 二叉树

#### 题目 9：二叉树的层序遍历（LeetCode 102）

```
    3
   / \
  9  20
    /  \
   15   7

输出 [[3], [9, 20], [15, 7]]

思路：BFS（队列）
```

```python
from collections import deque

def levelOrder(root):
    if not root:
        return []
    
    result = []
    queue = deque([root])
    
    while queue:
        level_size = len(queue)
        level = []
        
        for _ in range(level_size):
            node = queue.popleft()
            level.append(node.val)
            
            if node.left:
                queue.append(node.left)
            if node.right:
                queue.append(node.right)
        
        result.append(level)
    
    return result
```

---

### 5.8 动态规划

#### 题目 10：爬楼梯（LeetCode 70）

```
每次爬 1 或 2 个台阶，n 个台阶有多少种爬法

思路：斐波那契数列
dp[i] = dp[i-1] + dp[i-2]
```

```python
def climbStairs(n):
    if n <= 2:
        return n
    
    dp = [0] * (n + 1)
    dp[1] = 1
    dp[2] = 2
    
    for i in range(3, n + 1):
        dp[i] = dp[i-1] + dp[i-2]
    
    return dp[n]

# 优化：只用两个变量
def climbStairs(n):
    if n <= 2:
        return n
    
    a, b = 1, 2
    for _ in range(3, n + 1):
        a, b = b, a + b
    
    return b
```

---

#### 题目 11：最长递增子序列（LeetCode 300）

```
给定 [10, 9, 2, 5, 3, 7, 101, 18]
输出 4（[2, 3, 7, 101]）

思路：动态规划
dp[i] = 以 nums[i] 结尾的最长递增子序列长度
```

```python
def lengthOfLIS(nums):
    if not nums:
        return 0
    
    dp = [1] * len(nums)
    
    for i in range(1, len(nums)):
        for j in range(i):
            if nums[j] < nums[i]:
                dp[i] = max(dp[i], dp[j] + 1)
    
    return max(dp)

# 优化：贪心 + 二分查找（O(n log n)）
import bisect

def lengthOfLIS(nums):
    tails = []
    
    for num in nums:
        pos = bisect.bisect_left(tails, num)
        if pos == len(tails):
            tails.append(num)
        else:
            tails[pos] = num
    
    return len(tails)
```

---

## 六、算法题清单（刷题优先级）

### 必刷（面试高频）

| # | 题目 | 难度 | 类型 |
|---|------|------|------|
| 1 | 两数之和 | Easy | 哈希表 |
| 2 | 有效的括号 | Easy | 栈 |
| 3 | 合并两个有序链表 | Easy | 链表 |
| 4 | 反转链表 | Easy | 链表 |
| 5 | 爬楼梯 | Easy | 动态规划 |
| 6 | 无重复字符的最长子串 | Medium | 滑动窗口 |
| 7 | 三数之和 | Medium | 双指针 |
| 8 | LRU 缓存 | Medium | 设计 |
| 9 | 二叉树层序遍历 | Medium | BFS |
| 10 | 最长递增子序列 | Medium | 动态规划 |

### 建议刷（进阶）

| # | 题目 | 难度 | 类型 |
|---|------|------|------|
| 11 | 最大子数组和 | Medium | 动态规划 |
| 12 | 合并区间 | Medium | 排序 |
| 13 | 搜索旋转排序数组 | Medium | 二分 |
| 14 | 最小栈 | Easy | 栈 |
| 15 | 盛最多水的容器 | Medium | 双指针 |

---

## 面试做题技巧

| 步骤 | 时间 | 说明 |
|------|------|------|
| 1. 理解题意 | 1分钟 | 确认输入输出、边界条件 |
| 2. 说思路 | 2分钟 | 先说暴力解法，再说优化 |
| 3. 写代码 | 5分钟 | 边写边讲，命名规范 |
| 4. 测试 | 2分钟 | 用示例走一遍 |
| 5. 分析复杂度 | 1分钟 | 时间 O(?)，空间 O(?) |

**注意**：
- 不会做先说思路，面试官会给提示
- 代码写完一定要测试（手动走一遍）
- 复杂度分析是加分项
