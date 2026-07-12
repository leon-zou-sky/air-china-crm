# LeetCode 高频算法题 - Java/Python/Go 多语言解答

> 结合中大厂面试，分析考点和解题切入点

---

## 一、链表专题（重点补充）

### 1. 反转链表（LeetCode 206）

**考点**：链表基础操作、指针修改

**解题切入点**：
- 画图理解指针变化
- 三指针：prev、curr、next
- 递归理解：先反转后面，再处理当前

**Java**：
```java
public ListNode reverseList(ListNode head) {
    ListNode prev = null;
    ListNode curr = head;
    while (curr != null) {
        ListNode next = curr.next;
        curr.next = prev;
        prev = curr;
        curr = next;
    }
    return prev;
}
```

**Python**：
```python
def reverseList(self, head: ListNode) -> ListNode:
    prev, curr = None, head
    while curr:
        nxt = curr.next
        curr.next = prev
        prev = curr
        curr = nxt
    return prev
```

**Go**：
```go
func reverseList(head *ListNode) *ListNode {
    var prev *ListNode
    curr := head
    for curr != nil {
        nxt := curr.Next
        curr.Next = prev
        prev = curr
        curr = nxt
    }
    return prev
}
```

**追问**：
- 反转前N个节点？→ 记录第N+1个节点作为后继
- 反转m到n？→ 找到前后节点，反转中间部分
- K个一组反转？→ 分组处理，递归

---

### 2. 环形链表（LeetCode 141/142）

**考点**：快慢指针、数学推导

**解题切入点**：
- 快指针走2步，慢指针走1步
- 有环一定会相遇
- 入环点：数学公式推导 a = c

**LeetCode 141 - 判断是否有环**：

**Java**：
```java
public boolean hasCycle(ListNode head) {
    ListNode slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) return true;
    }
    return false;
}
```

**Python**：
```python
def hasCycle(self, head: ListNode) -> bool:
    slow = fast = head
    while fast and fast.next:
        slow = slow.next
        fast = fast.next.next
        if slow == fast:
            return True
    return False
```

**Go**：
```go
func hasCycle(head *ListNode) bool {
    slow, fast := head, head
    for fast != nil && fast.Next != nil {
        slow = slow.Next
        fast = fast.Next.Next
        if slow == fast {
            return true
        }
    }
    return false
}
```

**LeetCode 142 - 返回入环节点**：

**Java**：
```java
public ListNode detectCycle(ListNode head) {
    ListNode slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) {
            slow = head;
            while (slow != fast) {
                slow = slow.next;
                fast = fast.next;
            }
            return slow;
        }
    }
    return null;
}
```

**Python**：
```python
def detectCycle(self, head: ListNode) -> ListNode:
    slow = fast = head
    while fast and fast.next:
        slow = slow.next
        fast = fast.next.next
        if slow == fast:
            slow = head
            while slow != fast:
                slow = slow.next
                fast = fast.next
            return slow
    return None
```

**Go**：
```go
func detectCycle(head *ListNode) *ListNode {
    slow, fast := head, head
    for fast != nil && fast.Next != nil {
        slow = slow.Next
        fast = fast.Next.Next
        if slow == fast {
            slow = head
            for slow != fast {
                slow = slow.Next
                fast = fast.Next
            }
            return slow
        }
    }
    return nil
}
```

**追问**：
- 环的长度怎么算？→ 相遇后继续走，再次相遇
- 为什么 a = c？→ 设头到入环点=a，入环点到相遇点=b，相遇点到入环点=c
  - 慢指针走了：a + b
  - 快指针走了：a + b + c + b = a + 2b + c
  - 快指针是慢指针2倍：a + 2b + c = 2(a + b)
  - 推导：a = c

---

### 3. 合并两个有序链表（LeetCode 21）

**考点**：链表操作、归并思想

**解题切入点**：
- 虚拟头节点简化操作
- 比较两个链表头部，取较小的
- 递归或迭代

**Java**：
```java
public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
    ListNode dummy = new ListNode(0);
    ListNode curr = dummy;
    
    while (l1 != null && l2 != null) {
        if (l1.val <= l2.val) {
            curr.next = l1;
            l1 = l1.next;
        } else {
            curr.next = l2;
            l2 = l2.next;
        }
        curr = curr.next;
    }
    
    curr.next = (l1 != null) ? l1 : l2;
    return dummy.next;
}
```

**Python**：
```python
def mergeTwoLists(self, l1: ListNode, l2: ListNode) -> ListNode:
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

**Go**：
```go
func mergeTwoLists(l1 *ListNode, l2 *ListNode) *ListNode {
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

**追问**：
- 递归怎么做？→ 递归比较头部
- 合并K个有序链表？→ 优先队列或分治
- 时间复杂度？→ O(n)，空间O(1)迭代或O(n)递归

---

### 4. 合并K个有序链表（LeetCode 23）

**考点**：优先队列、分治法

**解题切入点**：
- 优先队列：每次取最小的
- 分治法：两两合并

**Java（优先队列）**：
```java
public ListNode mergeKLists(ListNode[] lists) {
    PriorityQueue<ListNode> pq = new PriorityQueue<>((a, b) -> a.val - b.val);
    for (ListNode node : lists) {
        if (node != null) pq.offer(node);
    }
    
    ListNode dummy = new ListNode(0);
    ListNode curr = dummy;
    
    while (!pq.isEmpty()) {
        ListNode min = pq.poll();
        curr.next = min;
        curr = curr.next;
        if (min.next != null) pq.offer(min.next);
    }
    return dummy.next;
}
```

**Python（优先队列）**：
```python
import heapq

def mergeKLists(self, lists: List[ListNode]) -> ListNode:
    pq = []
    for i, node in enumerate(lists):
        if node:
            heapq.heappush(pq, (node.val, i, node))
    
    dummy = ListNode(0)
    curr = dummy
    
    while pq:
        val, i, node = heapq.heappop(pq)
        curr.next = node
        curr = curr.next
        if node.next:
            heapq.heappush(pq, (node.next.val, i, node.next))
    
    return dummy.next
```

**Go（优先队列）**：
```go
type ListNodeHeap []*ListNode

func (h ListNodeHeap) Len() int           { return len(h) }
func (h ListNodeHeap) Less(i, j int) bool { return h[i].Val < h[j].Val }
func (h ListNodeHeap) Swap(i, j int)      { h[i], h[j] = h[j], h[i] }
func (h *ListNodeHeap) Push(x interface{}) { *h = append(*h, x.(*ListNode) }
func (h *ListNodeHeap) Pop() interface{} {
    old := *h
    n := len(old)
    x := old[n-1]
    *h = old[:n-1]
    return x
}

func mergeKLists(lists []*ListNode) *ListNode {
    h := &ListNodeHeap{}
    heap.Init(h)
    for _, node := range lists {
        if node != nil {
            heap.Push(h, node)
        }
    }
    
    dummy := &ListNode{}
    curr := dummy
    for h.Len() > 0 {
        node := heap.Pop(h).(*ListNode)
        curr.Next = node
        curr = curr.Next
        if node.Next != nil {
            heap.Push(h, node.Next)
        }
    }
    return dummy.Next
}
```

**追问**：
- 分治法怎么做？→ 两两合并，递归
- 时间复杂度？→ 优先队列O(N log K)，分治O(N log K)
- 空间复杂度？→ 优先队列O(K)，分治O(log K)

---

### 5. 删除链表倒数第N个节点（LeetCode 19）

**考点**：双指针、链表删除

**解题切入点**：
- 快指针先走N步
- 然后快慢一起走
- 快指针到末尾时，慢指针就是倒数第N个

**Java**：
```java
public ListNode removeNthFromEnd(ListNode head, int n) {
    ListNode dummy = new ListNode(0);
    dummy.next = head;
    ListNode fast = dummy, slow = dummy;
    
    // 快指针先走n+1步
    for (int i = 0; i <= n; i++) {
        fast = fast.next;
    }
    
    while (fast != null) {
        fast = fast.next;
        slow = slow.next;
    }
    
    slow.next = slow.next.next;
    return dummy.next;
}
```

**Python**：
```python
def removeNthFromEnd(self, head: ListNode, n: int) -> ListNode:
    dummy = ListNode(0)
    dummy.next = head
    fast = slow = dummy
    
    for _ in range(n + 1):
        fast = fast.next
    
    while fast:
        fast = fast.next
        slow = slow.next
    
    slow.next = slow.next.next
    return dummy.next
```

**Go**：
```go
func removeNthFromEnd(head *ListNode, n int) *ListNode {
    dummy := &ListNode{Next: head}
    fast, slow := dummy, dummy
    
    for i := 0; i <= n; i++ {
        fast = fast.Next
    }
    
    for fast != nil {
        fast = fast.Next
        slow = slow.Next
    }
    
    slow.Next = slow.Next.Next
    return dummy.Next
}
```

**追问**：
- 为什么要用虚拟头节点？→ 处理删除头节点的情况
- 如果要删除所有倒数第N个？→ 需要遍历多次
- 时间复杂度？→ O(L)，L是链表长度

---

### 6. 链表排序（LeetCode 148）

**考点**：归并排序、链表操作

**解题切入点**：
- 链表适合归并排序（不需要随机访问）
- 快慢指针找中点
- 递归排序后合并

**Java**：
```java
public ListNode sortList(ListNode head) {
    if (head == null || head.next == null) return head;
    
    ListNode mid = findMiddle(head);
    ListNode right = mid.next;
    mid.next = null;
    
    ListNode leftSorted = sortList(head);
    ListNode rightSorted = sortList(right);
    
    return merge(leftSorted, rightSorted);
}

private ListNode findMiddle(ListNode head) {
    ListNode slow = head, fast = head.next;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
    }
    return slow;
}

private ListNode merge(ListNode l1, ListNode l2) {
    ListNode dummy = new ListNode(0);
    ListNode curr = dummy;
    while (l1 != null && l2 != null) {
        if (l1.val <= l2.val) {
            curr.next = l1;
            l1 = l1.next;
        } else {
            curr.next = l2;
            l2 = l2.next;
        }
        curr = curr.next;
    }
    curr.next = (l1 != null) ? l1 : l2;
    return dummy.next;
}
```

**Python**：
```python
def sortList(self, head: ListNode) -> ListNode:
    if not head or not head.next:
        return head
    
    # 快慢指针找中点
    slow, fast = head, head.next
    while fast and fast.next:
        slow = slow.next
        fast = fast.next.next
    
    mid = slow.next
    slow.next = None
    
    left = self.sortList(head)
    right = self.sortList(mid)
    
    return self.merge(left, right)

def merge(self, l1, l2):
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

**Go**：
```go
func sortList(head *ListNode) *ListNode {
    if head == nil || head.Next == nil {
        return head
    }
    
    mid := findMiddle(head)
    right := mid.Next
    mid.Next = nil
    
    leftSorted := sortList(head)
    rightSorted := sortList(right)
    
    return merge(leftSorted, rightSorted)
}

func findMiddle(head *ListNode) *ListNode {
    slow, fast := head, head.Next
    for fast != nil && fast.Next != nil {
        slow = slow.Next
        fast = fast.Next.Next
    }
    return slow
}

func merge(l1, l2 *ListNode) *ListNode {
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

**追问**：
- 为什么用归并不用快排？→ 链表不支持随机访问
- 时间复杂度？→ O(N log N)
- 空间复杂度？→ O(log N)递归栈

---

### 7. 回文链表（LeetCode 234）

**考点**：快慢指针、链表反转

**解题切入点**：
- 快慢指针找中点
- 反转后半部分
- 比较前后两部分

**Java**：
```java
public boolean isPalindrome(ListNode head) {
    if (head == null || head.next == null) return true;
    
    ListNode slow = head, fast = head;
    while (fast.next != null && fast.next.next != null) {
        slow = slow.next;
        fast = fast.next.next;
    }
    
    ListNode secondHalf = reverseList(slow.next);
    ListNode firstHalf = head;
    
    while (secondHalf != null) {
        if (firstHalf.val != secondHalf.val) return false;
        firstHalf = firstHalf.next;
        secondHalf = secondHalf.next;
    }
    
    return true;
}

private ListNode reverseList(ListNode head) {
    ListNode prev = null;
    while (head != null) {
        ListNode next = head.next;
        head.next = prev;
        prev = head;
        head = next;
    }
    return prev;
}
```

**Python**：
```python
def isPalindrome(self, head: ListNode) -> bool:
    if not head or not head.next:
        return True
    
    slow = fast = head
    while fast.next and fast.next.next:
        slow = slow.next
        fast = fast.next.next
    
    second_half = self.reverse(slow.next)
    first_half = head
    
    while second_half:
        if first_half.val != second_half.val:
            return False
        first_half = first_half.next
        second_half = second_half.next
    
    return True

def reverse(self, head):
    prev = None
    while head:
        nxt = head.next
        head.next = prev
        prev = head
        head = nxt
    return prev
```

**Go**：
```go
func isPalindrome(head *ListNode) bool {
    if head == nil || head.Next == nil {
        return true
    }
    
    slow, fast := head, head
    for fast.Next != nil && fast.Next.Next != nil {
        slow = slow.Next
        fast = fast.Next.Next
    }
    
    secondHalf := reverse(slow.Next)
    firstHalf := head
    
    for secondHalf != nil {
        if firstHalf.Val != secondHalf.Val {
            return false
        }
        firstHalf = firstHalf.Next
        secondHalf = secondHalf.Next
    }
    
    return true
}

func reverse(head *ListNode) *ListNode {
    var prev *ListNode
    for head != nil {
        nxt := head.Next
        head.Next = prev
        prev = head
        head = nxt
    }
    return prev
}
```

**追问**：
- 如何恢复链表？→ 再次反转后半部分
- 时间复杂度？→ O(N)
- 空间复杂度？→ O(1)

---

### 8. 两数相加（LeetCode 2）

**考点**：链表操作、进位处理

**解题切入点**：
- 同时遍历两个链表
- 处理进位
- 注意长度不同

**Java**：
```java
public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
    ListNode dummy = new ListNode(0);
    ListNode curr = dummy;
    int carry = 0;
    
    while (l1 != null || l2 != null || carry > 0) {
        int sum = carry;
        if (l1 != null) {
            sum += l1.val;
            l1 = l1.next;
        }
        if (l2 != null) {
            sum += l2.val;
            l2 = l2.next;
        }
        carry = sum / 10;
        curr.next = new ListNode(sum % 10);
        curr = curr.next;
    }
    
    return dummy.next;
}
```

**Python**：
```python
def addTwoNumbers(self, l1: ListNode, l2: ListNode) -> ListNode:
    dummy = ListNode(0)
    curr = dummy
    carry = 0
    
    while l1 or l2 or carry:
        total = carry
        if l1:
            total += l1.val
            l1 = l1.next
        if l2:
            total += l2.val
            l2 = l2.next
        carry = total // 10
        curr.next = ListNode(total % 10)
        curr = curr.next
    
    return dummy.next
```

**Go**：
```go
func addTwoNumbers(l1 *ListNode, l2 *ListNode) *ListNode {
    dummy := &ListNode{}
    curr := dummy
    carry := 0
    
    for l1 != nil || l2 != nil || carry > 0 {
        sum := carry
        if l1 != nil {
            sum += l1.Val
            l1 = l1.Next
        }
        if l2 != nil {
            sum += l2.Val
            l2 = l2.Next
        }
        carry = sum / 10
        curr.Next = &ListNode{Val: sum % 10}
        curr = curr.Next
    }
    
    return dummy.Next
}
```

**追问**：
- 如果链表是正序的？→ 先反转，相加，再反转
- 大数相加？→ 同样思路
- 时间复杂度？→ O(max(m,n))

---

### 9. 旋转链表（LeetCode 61）

**考点**：链表操作、环形链表

**解题切入点**：
- 连成环
- 找到新的头节点
- 断开环

**Java**：
```java
public ListNode rotateRight(ListNode head, int k) {
    if (head == null || head.next == null || k == 0) return head;
    
    // 计算长度，连成环
    int length = 1;
    ListNode tail = head;
    while (tail.next != null) {
        tail = tail.next;
        length++;
    }
    tail.next = head;
    
    // 找到新的尾节点
    k = k % length;
    ListNode newTail = head;
    for (int i = 0; i < length - k - 1; i++) {
        newTail = newTail.next;
    }
    
    ListNode newHead = newTail.next;
    newTail.next = null;
    
    return newHead;
}
```

**Python**：
```python
def rotateRight(self, head: ListNode, k: int) -> ListNode:
    if not head or not head.next or k == 0:
        return head
    
    # 计算长度，连成环
    length = 1
    tail = head
    while tail.next:
        tail = tail.next
        length += 1
    tail.next = head
    
    # 找到新的尾节点
    k = k % length
    new_tail = head
    for _ in range(length - k - 1):
        new_tail = new_tail.next
    
    new_head = new_tail.next
    new_tail.next = None
    
    return new_head
```

**Go**：
```go
func rotateRight(head *ListNode, k int) *ListNode {
    if head == nil || head.Next == nil || k == 0 {
        return head
    }
    
    length := 1
    tail := head
    for tail.Next != nil {
        tail = tail.Next
        length++
    }
    tail.Next = head
    
    k = k % length
    newTail := head
    for i := 0; i < length - k - 1; i++ {
        newTail = newTail.Next
    }
    
    newHead := newTail.Next
    newTail.Next = nil
    
    return newHead
}
```

**追问**：
- 左旋转怎么做？→ k = length - k
- 时间复杂度？→ O(N)
- 空间复杂度？→ O(1)

---

### 10. 分隔链表（LeetCode 86）

**考点**：链表操作、双指针

**解题切入点**：
- 分成两个链表：小于x和大于等于x
- 然后连接两个链表

**Java**：
```java
public ListNode partition(ListNode head, int x) {
    ListNode small = new ListNode(0);
    ListNode large = new ListNode(0);
    ListNode smallTail = small, largeTail = large;
    
    while (head != null) {
        if (head.val < x) {
            smallTail.next = head;
            smallTail = smallTail.next;
        } else {
            largeTail.next = head;
            largeTail = largeTail.next;
        }
        head = head.next;
    }
    
    smallTail.next = large.next;
    largeTail.next = null;
    
    return small.next;
}
```

**Python**：
```python
def partition(self, head: ListNode, x: int) -> ListNode:
    small = ListNode(0)
    large = ListNode(0)
    small_tail, large_tail = small, large
    
    while head:
        if head.val < x:
            small_tail.next = head
            small_tail = small_tail.next
        else:
            large_tail.next = head
            large_tail = large_tail.next
        head = head.next
    
    small_tail.next = large.next
    large_tail.next = None
    
    return small.next
```

**Go**：
```go
func partition(head *ListNode, x int) *ListNode {
    small := &ListNode{}
    large := &ListNode{}
    smallTail, largeTail := small, large
    
    for head != nil {
        if head.Val < x {
            smallTail.Next = head
            smallTail = smallTail.Next
        } else {
            largeTail.Next = head
            largeTail = largeTail.Next
        }
        head = head.Next
    }
    
    smallTail.Next = large.Next
    largeTail.Next = nil
    
    return small.Next
}
```

**追问**：
- 如何保持相对顺序？→ 用尾插法
- 时间复杂度？→ O(N)
- 空间复杂度？→ O(1)

---

## 二、数组/字符串

### 11. 两数之和（LeetCode 1）

**考点**：HashMap、查找

**解题切入点**：
- 用HashMap存储已遍历的数
- 查找 complement = target - num
- 一次遍历

**Java**：
```java
public int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> map = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (map.containsKey(complement)) {
            return new int[]{map.get(complement), i};
        }
        map.put(nums[i], i);
    }
    return new int[0];
}
```

**Python**：
```python
def twoSum(self, nums: List[int], target: int) -> List[int]:
    seen = {}
    for i, num in enumerate(nums):
        complement = target - num
        if complement in seen:
            return [seen[complement], i]
        seen[num] = i
    return []
```

**Go**：
```go
func twoSum(nums []int, target int) []int {
    seen := make(map[int]int)
    for i, num := range nums {
        complement := target - num
        if j, ok := seen[complement]; ok {
            return []int{j, i}
        }
        seen[num] = i
    }
    return nil
}
```

**追问**：
- 有序数组？→ 双指针
- 三数之和？→ 排序 + 双指针
- 四数之和？→ 排序 + 双指针

---

### 12. 三数之和（LeetCode 15）

**考点**：排序、双指针、去重

**解题切入点**：
- 排序
- 固定一个数，双指针找另外两个
- 去重：跳过相同元素

**Java**：
```java
public List<List<Integer>> threeSum(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    Arrays.sort(nums);
    
    for (int i = 0; i < nums.length - 2; i++) {
        if (i > 0 && nums[i] == nums[i - 1]) continue;
        
        int left = i + 1, right = nums.length - 1;
        while (left < right) {
            int sum = nums[i] + nums[left] + nums[right];
            if (sum == 0) {
                result.add(Arrays.asList(nums[i], nums[left], nums[right]));
                while (left < right && nums[left] == nums[left + 1]) left++;
                while (left < right && nums[right] == nums[right - 1]) right--;
                left++;
                right--;
            } else if (sum < 0) {
                left++;
            } else {
                right--;
            }
        }
    }
    return result;
}
```

**Python**：
```python
def threeSum(self, nums: List[int]) -> List[List[int]]:
    nums.sort()
    result = []
    
    for i in range(len(nums) - 2):
        if i > 0 and nums[i] == nums[i - 1]:
            continue
        
        left, right = i + 1, len(nums) - 1
        while left < right:
            total = nums[i] + nums[left] + nums[right]
            if total == 0:
                result.append([nums[i], nums[left], nums[right]])
                while left < right and nums[left] == nums[left + 1]:
                    left += 1
                while left < right and nums[right] == nums[right - 1]:
                    right -= 1
                left += 1
                right -= 1
            elif total < 0:
                left += 1
            else:
                right -= 1
    
    return result
```

**Go**：
```go
func threeSum(nums []int) [][]int {
    sort.Ints(nums)
    var result [][]int
    
    for i := 0; i < len(nums) - 2; i++ {
        if i > 0 && nums[i] == nums[i-1] {
            continue
        }
        
        left, right := i + 1, len(nums) - 1
        for left < right {
            sum := nums[i] + nums[left] + nums[right]
            if sum == 0 {
                result = append(result, []int{nums[i], nums[left], nums[right]})
                for left < right && nums[left] == nums[left+1] {
                    left++
                }
                for left < right && nums[right] == nums[right-1] {
                    right--
                }
                left++
                right--
            } else if sum < 0 {
                left++
            } else {
                right--
            }
        }
    }
    return result
}
```

**追问**：
- 时间复杂度？→ O(N²)
- 如何去重？→ 排序 + 跳过相同元素
- 最接近的三数之和？→ 记录最小差值

---

### 13. 最长无重复子串（LeetCode 3）

**考点**：滑动窗口、HashSet

**解题切入点**：
- 滑动窗口
- HashSet记录窗口内字符
- 有重复时收缩左边界

**Java**：
```java
public int lengthOfLongestSubstring(String s) {
    Set<Character> set = new HashSet<>();
    int left = 0, maxLen = 0;
    
    for (int right = 0; right < s.length(); right++) {
        while (set.contains(s.charAt(right))) {
            set.remove(s.charAt(left));
            left++;
        }
        set.add(s.charAt(right));
        maxLen = Math.max(maxLen, right - left + 1);
    }
    return maxLen;
}
```

**Python**：
```python
def lengthOfLongestSubstring(self, s: str) -> int:
    char_set = set()
    left = 0
    max_len = 0
    
    for right in range(len(s)):
        while s[right] in char_set:
            char_set.remove(s[left])
            left += 1
        char_set.add(s[right])
        max_len = max(max_len, right - left + 1)
    
    return max_len
```

**Go**：
```go
func lengthOfLongestSubstring(s string) int {
    charSet := make(map[byte]bool)
    left, maxLen := 0, 0
    
    for right := 0; right < len(s); right++ {
        for charSet[s[right]] {
            delete(charSet, s[left])
            left++
        }
        charSet[s[right]] = true
        if right - left + 1 > maxLen {
            maxLen = right - left + 1
        }
    }
    return maxLen
}
```

**追问**：
- 用HashMap怎么做？→ 存字符和索引，直接跳到重复位置+1
- 最长最多K个不同字符？→ HashMap计数
- 时间复杂度？→ O(N)

---

### 14. 接雨水（LeetCode 42）

**考点**：双指针、动态规划

**解题切入点**：
- 每个位置能接的水 = min(左边最大, 右边最大) - 当前高度
- 双指针从两端向中间
- 记录左右最大高度

**Java**：
```java
public int trap(int[] height) {
    int left = 0, right = height.length - 1;
    int leftMax = 0, rightMax = 0;
    int water = 0;
    
    while (left < right) {
        if (height[left] < height[right]) {
            if (height[left] >= leftMax) {
                leftMax = height[left];
            } else {
                water += leftMax - height[left];
            }
            left++;
        } else {
            if (height[right] >= rightMax) {
                rightMax = height[right];
            } else {
                water += rightMax - height[right];
            }
            right--;
        }
    }
    return water;
}
```

**Python**：
```python
def trap(self, height: List[int]) -> int:
    left, right = 0, len(height) - 1
    left_max = right_max = 0
    water = 0
    
    while left < right:
        if height[left] < height[right]:
            if height[left] >= left_max:
                left_max = height[left]
            else:
                water += left_max - height[left]
            left += 1
        else:
            if height[right] >= right_max:
                right_max = height[right]
            else:
                water += right_max - height[right]
            right -= 1
    
    return water
```

**Go**：
```go
func trap(height []int) int {
    left, right := 0, len(height) - 1
    leftMax, rightMax := 0, 0
    water := 0
    
    for left < right {
        if height[left] < height[right] {
            if height[left] >= leftMax {
                leftMax = height[left]
            } else {
                water += leftMax - height[left]
            }
            left++
        } else {
            if height[right] >= rightMax {
                rightMax = height[right]
            } else {
                water += rightMax - height[right]
            }
            right--
        }
    }
    return water
}
```

**追问**：
- 动态规划怎么做？→ 预计算每个位置的左右最大值
- 栈怎么做？→ 单调栈
- 柱状图最大矩形？→ 单调栈

---

## 三、动态规划

### 15. 爬楼梯（LeetCode 70）

**考点**：斐波那契数列、动态规划

**解题切入点**：
- dp[i] = dp[i-1] + dp[i-2]
- 滚动数组优化空间

**Java**：
```java
public int climbStairs(int n) {
    if (n <= 2) return n;
    int prev2 = 1, prev1 = 2;
    for (int i = 3; i <= n; i++) {
        int curr = prev1 + prev2;
        prev2 = prev1;
        prev1 = curr;
    }
    return prev1;
}
```

**Python**：
```python
def climbStairs(self, n: int) -> int:
    if n <= 2:
        return n
    prev2, prev1 = 1, 2
    for _ in range(3, n + 1):
        prev2, prev1 = prev1, prev1 + prev2
    return prev1
```

**Go**：
```go
func climbStairs(n int) int {
    if n <= 2 {
        return n
    }
    prev2, prev1 := 1, 2
    for i := 3; i <= n; i++ {
        prev2, prev1 = prev1, prev1 + prev2
    }
    return prev1
}
```

---

### 16. 最长递增子序列（LeetCode 300）

**考点**：动态规划、二分查找

**解题切入点**：
- dp[i]：以i结尾的最长递增子序列长度
- 贪心 + 二分：维护一个递增数组

**Java（贪心+二分）**：
```java
public int lengthOfLIS(int[] nums) {
    List<Integer> tails = new ArrayList<>();
    for (int num : nums) {
        int pos = Collections.binarySearch(tails, num);
        if (pos < 0) pos = -(pos + 1);
        if (pos == tails.size()) {
            tails.add(num);
        } else {
            tails.set(pos, num);
        }
    }
    return tails.size();
}
```

**Python（贪心+二分）**：
```python
def lengthOfLIS(self, nums: List[int]) -> int:
    tails = []
    for num in nums:
        pos = bisect.bisect_left(tails, num)
        if pos == len(tails):
            tails.append(num)
        else:
            tails[pos] = num
    return len(tails)
```

**Go（贪心+二分）**：
```go
func lengthOfLIS(nums []int) int {
    tails := []int{}
    for _, num := range nums {
        pos := sort.SearchInts(tails, num)
        if pos == len(tails) {
            tails = append(tails, num)
        } else {
            tails[pos] = num
        }
    }
    return len(tails)
}
```

---

### 17. 零钱兑换（LeetCode 322）

**考点**：完全背包、动态规划

**解题切入点**：
- dp[i]：凑出金额i的最少硬币数
- dp[i] = min(dp[i-coin] + 1) for each coin

**Java**：
```java
public int coinChange(int[] coins, int amount) {
    int[] dp = new int[amount + 1];
    Arrays.fill(dp, amount + 1);
    dp[0] = 0;
    
    for (int i = 1; i <= amount; i++) {
        for (int coin : coins) {
            if (coin <= i) {
                dp[i] = Math.min(dp[i], dp[i - coin] + 1);
            }
        }
    }
    return dp[amount] > amount ? -1 : dp[amount];
}
```

**Python**：
```python
def coinChange(self, coins: List[int], amount: int) -> int:
    dp = [float('inf')] * (amount + 1)
    dp[0] = 0
    
    for i in range(1, amount + 1):
        for coin in coins:
            if coin <= i:
                dp[i] = min(dp[i], dp[i - coin] + 1)
    
    return dp[amount] if dp[amount] != float('inf') else -1
```

**Go**：
```go
func coinChange(coins []int, amount int) int {
    dp := make([]int, amount + 1)
    for i := range dp {
        dp[i] = amount + 1
    }
    dp[0] = 0
    
    for i := 1; i <= amount; i++ {
        for _, coin := range coins {
            if coin <= i && dp[i - coin] + 1 < dp[i] {
                dp[i] = dp[i - coin] + 1
            }
        }
    }
    
    if dp[amount] > amount {
        return -1
    }
    return dp[amount]
}
```

---

## 四、树

### 18. 二叉树的层序遍历（LeetCode 102）

**考点**：BFS、队列

**解题切入点**：
- 队列
- 记录每层节点数
- 逐层处理

**Java**：
```java
public List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;
    
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    
    while (!queue.isEmpty()) {
        int size = queue.size();
        List<Integer> level = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            TreeNode node = queue.poll();
            level.add(node.val);
            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
        result.add(level);
    }
    return result;
}
```

**Python**：
```python
def levelOrder(self, root: TreeNode) -> List[List[int]]:
    if not root:
        return []
    
    result = []
    queue = collections.deque([root])
    
    while queue:
        level = []
        for _ in range(len(queue)):
            node = queue.popleft()
            level.append(node.val)
            if node.left:
                queue.append(node.left)
            if node.right:
                queue.append(node.right)
        result.append(level)
    
    return result
```

**Go**：
```go
func levelOrder(root *TreeNode) [][]int {
    if root == nil {
        return nil
    }
    
    var result [][]int
    queue := []*TreeNode{root}
    
    for len(queue) > 0 {
        size := len(queue)
        var level []int
        for i := 0; i < size; i++ {
            node := queue[0]
            queue = queue[1:]
            level = append(level, node.Val)
            if node.Left != nil {
                queue = append(queue, node.Left)
            }
            if node.Right != nil {
                queue = append(queue, node.Right)
            }
        }
        result = append(result, level)
    }
    return result
}
```

---

### 19. 验证二叉搜索树（LeetCode 98）

**考点**：BST性质、中序遍历

**解题切入点**：
- 中序遍历是有序的
- 递归传递范围

**Java（递归）**：
```java
public boolean isValidBST(TreeNode root) {
    return helper(root, Long.MIN_VALUE, Long.MAX_VALUE);
}

private boolean helper(TreeNode root, long min, long max) {
    if (root == null) return true;
    if (root.val <= min || root.val >= max) return false;
    return helper(root.left, min, root.val) && helper(root.right, root.val, max);
}
```

**Python（递归）**：
```python
def isValidBST(self, root: TreeNode) -> bool:
    def helper(node, min_val, max_val):
        if not node:
            return True
        if node.val <= min_val or node.val >= max_val:
            return False
        return helper(node.left, min_val, node.val) and helper(node.right, node.val, max_val)
    
    return helper(root, float('-inf'), float('inf'))
```

**Go（递归）**：
```go
func isValidBST(root *TreeNode) bool {
    return helper(root, math.MinInt64, math.MaxInt64)
}

func helper(root *TreeNode, min, max int) bool {
    if root == nil {
        return true
    }
    if root.Val <= min || root.Val >= max {
        return false
    }
    return helper(root.Left, min, root.Val) && helper(root.Right, root.Val, max)
}
```

---

## 五、图

### 20. 岛屿数量（LeetCode 200）

**考点**：DFS、BFS、连通分量

**解题切入点**：
- 遍历网格
- 遇到'1'就DFS标记访问
- 计数

**Java（DFS）**：
```java
public int numIslands(char[][] grid) {
    int count = 0;
    for (int i = 0; i < grid.length; i++) {
        for (int j = 0; j < grid[0].length; j++) {
            if (grid[i][j] == '1') {
                count++;
                dfs(grid, i, j);
            }
        }
    }
    return count;
}

private void dfs(char[][] grid, int i, int j) {
    if (i < 0 || i >= grid.length || j < 0 || j >= grid[0].length) return;
    if (grid[i][j] != '1') return;
    grid[i][j] = '0';
    dfs(grid, i + 1, j);
    dfs(grid, i - 1, j);
    dfs(grid, i, j + 1);
    dfs(grid, i, j - 1);
}
```

**Python（DFS）**：
```python
def numIslands(self, grid: List[List[str]]) -> int:
    if not grid:
        return 0
    
    count = 0
    for i in range(len(grid)):
        for j in range(len(grid[0])):
            if grid[i][j] == '1':
                count += 1
                self.dfs(grid, i, j)
    return count

def dfs(self, grid, i, j):
    if i < 0 or i >= len(grid) or j < 0 or j >= len(grid[0]):
        return
    if grid[i][j] != '1':
        return
    grid[i][j] = '0'
    self.dfs(grid, i + 1, j)
    self.dfs(grid, i - 1, j)
    self.dfs(grid, i, j + 1)
    self.dfs(grid, i, j - 1)
```

**Go（DFS）**：
```go
func numIslands(grid [][]byte) int {
    count := 0
    for i := 0; i < len(grid); i++ {
        for j := 0; j < len(grid[0]); j++ {
            if grid[i][j] == '1' {
                count++
                dfs(grid, i, j)
            }
        }
    }
    return count
}

func dfs(grid [][]byte, i, j int) {
    if i < 0 || i >= len(grid) || j < 0 || j >= len(grid[0]) {
        return
    }
    if grid[i][j] != '1' {
        return
    }
    grid[i][j] = '0'
    dfs(grid, i + 1, j)
    dfs(grid, i - 1, j)
    dfs(grid, i, j + 1)
    dfs(grid, i, j - 1)
}
```

---

## 考点分析总结

### 按考点分类

| 考点 | 题目 | 核心技巧 |
|------|------|---------|
| **双指针** | 两数之和、三数之和、接雨水、盛水容器 | 左右指针、快慢指针 |
| **滑动窗口** | 最长无重复子串、最小覆盖子串 | 窗口收缩、HashMap计数 |
| **链表操作** | 反转、合并、环检测、排序 | 虚拟头节点、快慢指针 |
| **二叉树** | 层序遍历、验证BST、LCA | 递归、BFS、中序遍历 |
| **动态规划** | 爬楼梯、LIS、零钱兑换 | 状态定义、转移方程 |
| **图** | 岛屿数量、课程表 | DFS、BFS、拓扑排序 |
| **排序** | 快排、归并、堆排 | 分治、比较 |
| **回溯** | 全排列、子集 | 路径选择、剪枝 |

### 解题切入点总结

```
1. 看到"有序" → 二分查找、双指针
2. 看到"子串/子数组" → 滑动窗口
3. 看到"所有组合" → 回溯
4. 看到"最优解" → 动态规划
5. 看到"图/网格" → DFS/BFS
6. 看到"链表" → 快慢指针、虚拟头节点
7. 看到"树" → 递归、BFS
8. 看到"第K大/小" → 堆、快排partition
```

---

文档生成时间：2026-07-04
