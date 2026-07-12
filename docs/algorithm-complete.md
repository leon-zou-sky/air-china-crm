# 高频算法题 - Java/Python/Go/C++ 完整版

> 结合中大厂面试，含完整题目描述、示例、业务场景、多语言代码

---

## 一、数组/字符串

### 1. 两数之和（LeetCode 1-Easy）

**题目描述**：给定一个整数数组 `nums` 和一个目标值 `target`，请你在数组中找出和为目标值的两个数，并返回它们的下标。假设每个输入只对应一个答案。

**输入输出示例**：
```
输入：nums = [2, 7, 11, 15], target = 9
输出：[0, 1]
解释：nums[0] + nums[1] = 2 + 7 = 9

输入：nums = [3, 3], target = 6
输出：[0, 1]
```

**实际业务场景**：
- 电商优惠券凑单：从购物车中找两件商品价格之和等于优惠券门槛
- 银行对账：两笔流水核对，找到金额匹配的交易记录
- 机票价格匹配：查找两个航班价格之和等于预算的路线组合

**解题思路**：HashMap存已遍历的数，查找 complement = target - num，一次遍历。

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

**C++**：
```cpp
vector<int> twoSum(vector<int>& nums, int target) {
    unordered_map<int, int> seen;
    for (int i = 0; i < nums.size(); i++) {
        int complement = target - nums[i];
        if (seen.count(complement)) {
            return {seen[complement], i};
        }
        seen[nums[i]] = i;
    }
    return {};
}
```

**复杂度**：时间 O(n)，空间 O(n)

**追问**：有序数组怎么做？三数之和怎么解？

---

### 2. 三数之和（LeetCode 15-Medium）

**题目描述**：给你一个整数数组 `nums`，找出所有和为 0 且不重复的三元组。

**输入输出示例**：
```
输入：nums = [-1, 0, 1, 2, -1, -4]
输出：[[-1, -1, 2], [-1, 0, 1]]
解释：-1 + -1 + 2 = 0，-1 + 0 + 1 = 0
```

**实际业务场景**：
- 财务风控：找出三笔相互抵消的异常流水
- 物流路径：找到三段路程之和为总距离的路径组合
- 电商推荐：找出三个商品总价为指定金额的组合

**解题思路**：排序 + 固定一个数 + 双指针找另外两个，注意去重。

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
            } else if (sum < 0) left++;
            else right--;
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
                left += 1; right -= 1
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
    for i := 0; i < len(nums)-2; i++ {
        if i > 0 && nums[i] == nums[i-1] { continue }
        left, right := i+1, len(nums)-1
        for left < right {
            sum := nums[i] + nums[left] + nums[right]
            if sum == 0 {
                result = append(result, []int{nums[i], nums[left], nums[right]})
                for left < right && nums[left] == nums[left+1] { left++ }
                for left < right && nums[right] == nums[right-1] { right-- }
                left++; right--
            } else if sum < 0 { left++ } else { right-- }
        }
    }
    return result
}
```

**C++**：
```cpp
vector<vector<int>> threeSum(vector<int>& nums) {
    sort(nums.begin(), nums.end());
    vector<vector<int>> result;
    for (int i = 0; i < (int)nums.size() - 2; i++) {
        if (i > 0 && nums[i] == nums[i - 1]) continue;
        int left = i + 1, right = nums.size() - 1;
        while (left < right) {
            int sum = nums[i] + nums[left] + nums[right];
            if (sum == 0) {
                result.push_back({nums[i], nums[left], nums[right]});
                while (left < right && nums[left] == nums[left + 1]) left++;
                while (left < right && nums[right] == nums[right - 1]) right--;
                left++; right--;
            } else if (sum < 0) left++;
            else right--;
        }
    }
    return result;
}
```

**复杂度**：时间 O(n²)，空间 O(1)

**追问**：为什么排序？怎么去重的？四数之和怎么解？

---

### 3. 最长无重复子串（LeetCode 3-Medium）

**题目描述**：给定一个字符串 `s`，找出其中不含有重复字符的最长子串的长度。

**输入输出示例**：
```
输入：s = "abcabcbb"
输出：3
解释：最长无重复子串是 "abc"，长度为3

输入：s = "bbbbb"
输出：1
```

**实际业务场景**：
- 密码强度校验：检测连续不重复字符的最长段
- 基因序列分析：查找无重复碱基的最长DNA片段
- 日志去重：分析最大连续不重复时间窗口内的请求

**解题思路**：滑动窗口 + HashSet，有重复时收缩左边界。

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
    left = max_len = 0
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
    set := make(map[byte]bool)
    left, maxLen := 0, 0
    for right := 0; right < len(s); right++ {
        for set[s[right]] {
            delete(set, s[left])
            left++
        }
        set[s[right]] = true
        if right-left+1 > maxLen { maxLen = right - left + 1 }
    }
    return maxLen
}
```

**C++**：
```cpp
int lengthOfLongestSubstring(string s) {
    unordered_set<char> set;
    int left = 0, maxLen = 0;
    for (int right = 0; right < s.size(); right++) {
        while (set.count(s[right])) {
            set.erase(s[left]);
            left++;
        }
        set.insert(s[right]);
        maxLen = max(maxLen, right - left + 1);
    }
    return maxLen;
}
```

**复杂度**：时间 O(n)，空间 O(字符集大小)

---

### 4. 接雨水（LeetCode 42-Hard）

**题目描述**：给定 `n` 个非负整数表示柱子高度，计算下雨后能接多少雨水。

**输入输出示例**：
```
输入：height = [0,1,0,2,1,0,1,3,2,1,2,1]
输出：6
```

**实际业务场景**：
- 降雨蓄水计算：计算高低地形之间能蓄多少水（水库设计）
- 股票K线分析：寻找价格低谷之间的"回填"空间
- 数据平滑：填充两个峰值之间的数据凹槽

**解题思路**：双指针从两端向中间，记录左右最大高度。

**Java**：
```java
public int trap(int[] height) {
    int left = 0, right = height.length - 1;
    int leftMax = 0, rightMax = 0, water = 0;
    while (left < right) {
        if (height[left] < height[right]) {
            if (height[left] >= leftMax) leftMax = height[left];
            else water += leftMax - height[left];
            left++;
        } else {
            if (height[right] >= rightMax) rightMax = height[right];
            else water += rightMax - height[right];
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
    left_max = right_max = water = 0
    while left < right:
        if height[left] < height[right]:
            if height[left] >= left_max: left_max = height[left]
            else: water += left_max - height[left]
            left += 1
        else:
            if height[right] >= right_max: right_max = height[right]
            else: water += right_max - height[right]
            right -= 1
    return water
```

**Go**：
```go
func trap(height []int) int {
    left, right := 0, len(height)-1
    leftMax, rightMax, water := 0, 0, 0
    for left < right {
        if height[left] < height[right] {
            if height[left] >= leftMax { leftMax = height[left] }
            else { water += leftMax - height[left] }
            left++
        } else {
            if height[right] >= rightMax { rightMax = height[right] }
            else { water += rightMax - height[right] }
            right--
        }
    }
    return water
}
```

**C++**：
```cpp
int trap(vector<int>& height) {
    int left = 0, right = height.size() - 1;
    int leftMax = 0, rightMax = 0, water = 0;
    while (left < right) {
        if (height[left] < height[right]) {
            if (height[left] >= leftMax) leftMax = height[left];
            else water += leftMax - height[left];
            left++;
        } else {
            if (height[right] >= rightMax) rightMax = height[right];
            else water += rightMax - height[right];
            right--;
        }
    }
    return water;
}
```

**复杂度**：时间 O(n)，空间 O(1)

---

### 5. 盛最多水的容器（LeetCode 11-Medium）

**题目描述**：找出数组中的两条线，使它们与 x 轴构成的容器能容纳最多的水。

**输入输出示例**：
```
输入：height = [1,8,6,2,5,4,8,3,7]
输出：49
解释：选择 height[1]=8 和 height[8]=7，宽度7，面积 min(8,7)*7=49
```

**实际业务场景**：
- 容器设计：给定板材高度限制，求最大容积
- 广告位优化：选两个高度最好的展示位，最大化展示面积

**Java**：
```java
public int maxArea(int[] height) {
    int left = 0, right = height.length - 1, maxArea = 0;
    while (left < right) {
        int area = Math.min(height[left], height[right]) * (right - left);
        maxArea = Math.max(maxArea, area);
        if (height[left] < height[right]) left++;
        else right--;
    }
    return maxArea;
}
```

**Python**：
```python
def maxArea(self, height: List[int]) -> int:
    left, right = 0, len(height) - 1
    max_area = 0
    while left < right:
        area = min(height[left], height[right]) * (right - left)
        max_area = max(max_area, area)
        if height[left] < height[right]: left += 1
        else: right -= 1
    return max_area
```

**Go**：
```go
func maxArea(height []int) int {
    left, right := 0, len(height)-1
    maxArea := 0
    for left < right {
        h := height[left]
        if height[right] < h { h = height[right] }
        area := h * (right - left)
        if area > maxArea { maxArea = area }
        if height[left] < height[right] { left++ } else { right-- }
    }
    return maxArea
}
```

**C++**：
```cpp
int maxArea(vector<int>& height) {
    int left = 0, right = height.size() - 1, maxArea = 0;
    while (left < right) {
        int h = min(height[left], height[right]);
        maxArea = max(maxArea, h * (right - left));
        if (height[left] < height[right]) left++;
        else right--;
    }
    return maxArea;
}
```

---

### 6. 最小覆盖子串（LeetCode 76-Hard）

**题目描述**：从字符串 `s` 中找出包含 `t` 所有字符的最小子串，不存在则返回 `""`。

**输入输出示例**：
```
输入：s = "ADOBECODEBANC", t = "ABC"
输出："BANC"
```

**实际业务场景**：
- 搜索引擎：输入多个关键词，找到包含所有关键词的最小文档片段
- DNA序列匹配：找到包含目标基因序列的最短DNA片段

**Java**：
```java
public String minWindow(String s, String t) {
    Map<Character, Integer> need = new HashMap<>();
    for (char c : t.toCharArray()) need.put(c, need.getOrDefault(c, 0) + 1);
    Map<Character, Integer> window = new HashMap<>();
    int left = 0, valid = 0, start = 0, minLen = Integer.MAX_VALUE;
    for (int right = 0; right < s.length(); right++) {
        char c = s.charAt(right);
        if (need.containsKey(c)) {
            window.put(c, window.getOrDefault(c, 0) + 1);
            if (window.get(c).equals(need.get(c))) valid++;
        }
        while (valid == need.size()) {
            if (right - left + 1 < minLen) {
                start = left; minLen = right - left + 1;
            }
            char d = s.charAt(left);
            if (need.containsKey(d)) {
                if (window.get(d).equals(need.get(d))) valid--;
                window.put(d, window.get(d) - 1);
            }
            left++;
        }
    }
    return minLen == Integer.MAX_VALUE ? "" : s.substring(start, start + minLen);
}
```

**Python**：
```python
def minWindow(self, s: str, t: str) -> str:
    from collections import Counter
    need = Counter(t)
    window = {}
    left = valid = 0
    start, min_len = 0, float('inf')
    for right, c in enumerate(s):
        if c in need:
            window[c] = window.get(c, 0) + 1
            if window[c] == need[c]: valid += 1
        while valid == len(need):
            if right - left + 1 < min_len:
                start, min_len = left, right - left + 1
            d = s[left]
            if d in need:
                if window[d] == need[d]: valid -= 1
                window[d] -= 1
            left += 1
    return "" if min_len == float('inf') else s[start:start+min_len]
```

**Go**：
```go
func minWindow(s string, t string) string {
    need := make(map[byte]int)
    for i := range t { need[t[i]]++ }
    window := make(map[byte]int)
    left, valid, start, minLen := 0, 0, 0, len(s)+1
    for right := 0; right < len(s); right++ {
        c := s[right]
        if need[c] > 0 {
            window[c]++
            if window[c] == need[c] { valid++ }
        }
        for valid == len(need) {
            if right-left+1 < minLen {
                start, minLen = left, right-left+1
            }
            d := s[left]
            if need[d] > 0 {
                if window[d] == need[d] { valid-- }
                window[d]--
            }
            left++
        }
    }
    if minLen > len(s) { return "" }
    return s[start : start+minLen]
}
```

**C++**：
```cpp
string minWindow(string s, string t) {
    unordered_map<char, int> need;
    for (char c : t) need[c]++;
    unordered_map<char, int> window;
    int left = 0, valid = 0, start = 0, minLen = INT_MAX;
    for (int right = 0; right < s.size(); right++) {
        char c = s[right];
        if (need.count(c)) {
            window[c]++;
            if (window[c] == need[c]) valid++;
        }
        while (valid == need.size()) {
            if (right - left + 1 < minLen) {
                start = left; minLen = right - left + 1;
            }
            char d = s[left];
            if (need.count(d)) {
                if (window[d] == need[d]) valid--;
                window[d]--;
            }
            left++;
        }
    }
    return minLen == INT_MAX ? "" : s.substr(start, minLen);
}
```

---

## 二、链表专题

### 7. 反转链表（LeetCode 206-Easy）

**题目描述**：反转一个单链表，返回反转后的链表头。

**输入输出示例**：
```
输入：1 → 2 → 3 → 4 → 5 → null
输出：5 → 4 → 3 → 2 → 1 → null
```

**实际业务场景**：
- 浏览器前进后退：浏览历史需要倒序展示
- 消息列表：聊天记录从最新到最旧排序需要反转
- 回文检测：判断链表是否回文需要反转后半部分

**Java**：
```java
public ListNode reverseList(ListNode head) {
    ListNode prev = null, curr = head;
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

**C++**：
```cpp
ListNode* reverseList(ListNode* head) {
    ListNode* prev = nullptr;
    ListNode* curr = head;
    while (curr) {
        ListNode* next = curr->next;
        curr->next = prev;
        prev = curr;
        curr = next;
    }
    return prev;
}
```

---

### 8. 环形链表（LeetCode 141/142-Easy/Medium）

**题目描述**：141判断链表是否有环；142返回入环节点。

**输入输出示例**：
```
输入：1 → 2 → 3 → 4 → 2（环，4→2）
输出：true（141）/ 节点2（142）
```

**实际业务场景**：
- 内存泄漏检测：对象引用链中出现循环引用
- 死锁检测：进程等待图中检测环
- 微服务调用链：检测循环依赖

**Java（141+142）**：
```java
// 141 判断环
public boolean hasCycle(ListNode head) {
    ListNode slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next; fast = fast.next.next;
        if (slow == fast) return true;
    }
    return false;
}
// 142 入环节点
public ListNode detectCycle(ListNode head) {
    ListNode slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next; fast = fast.next.next;
        if (slow == fast) {
            slow = head;
            while (slow != fast) { slow = slow.next; fast = fast.next; }
            return slow;
        }
    }
    return null;
}
```

**Python（141+142）**：
```python
# 141
def hasCycle(self, head: ListNode) -> bool:
    slow = fast = head
    while fast and fast.next:
        slow, fast = slow.next, fast.next.next
        if slow == fast: return True
    return False

# 142
def detectCycle(self, head: ListNode) -> ListNode:
    slow = fast = head
    while fast and fast.next:
        slow, fast = slow.next, fast.next.next
        if slow == fast:
            slow = head
            while slow != fast:
                slow, fast = slow.next, fast.next
            return slow
    return None
```

**Go（141+142）**：
```go
// 141
func hasCycle(head *ListNode) bool {
    slow, fast := head, head
    for fast != nil && fast.Next != nil {
        slow, fast = slow.Next, fast.Next.Next
        if slow == fast { return true }
    }
    return false
}
// 142
func detectCycle(head *ListNode) *ListNode {
    slow, fast := head, head
    for fast != nil && fast.Next != nil {
        slow, fast = slow.Next, fast.Next.Next
        if slow == fast {
            slow = head
            for slow != fast { slow, fast = slow.Next, fast.Next }
            return slow
        }
    }
    return nil
}
```

**C++（141+142）**：
```cpp
bool hasCycle(ListNode* head) {
    ListNode *slow = head, *fast = head;
    while (fast && fast->next) {
        slow = slow->next; fast = fast->next->next;
        if (slow == fast) return true;
    }
    return false;
}
ListNode* detectCycle(ListNode* head) {
    ListNode *slow = head, *fast = head;
    while (fast && fast->next) {
        slow = slow->next; fast = fast->next->next;
        if (slow == fast) {
            slow = head;
            while (slow != fast) { slow = slow->next; fast = fast->next; }
            return slow;
        }
    }
    return nullptr;
}
```

**追问**：为什么 a = c？（数学推导）环的长度怎么算？

---

### 9. 合并两个有序链表（LeetCode 21-Easy）

**题目描述**：合并两个升序链表为一个升序链表。

**输入输出示例**：
```
输入：l1 = 1→2→4, l2 = 1→3→4
输出：1→1→2→3→4→4
```

**实际业务场景**：
- 日志合并：多个服务的时间排序日志合并
- 搜索结果聚合：多个数据源的排序结果合并
- 股票K线合并：多个交易所行情数据按时间合并

**Java**：
```java
public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
    ListNode dummy = new ListNode(0), curr = dummy;
    while (l1 != null && l2 != null) {
        if (l1.val <= l2.val) { curr.next = l1; l1 = l1.next; }
        else { curr.next = l2; l2 = l2.next; }
        curr = curr.next;
    }
    curr.next = l1 != null ? l1 : l2;
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
            curr.next, l1 = l1, l1.next
        else:
            curr.next, l2 = l2, l2.next
        curr = curr.next
    curr.next = l1 or l2
    return dummy.next
```

**Go**：
```go
func mergeTwoLists(l1, l2 *ListNode) *ListNode {
    dummy := &ListNode{}
    curr := dummy
    for l1 != nil && l2 != nil {
        if l1.Val <= l2.Val { curr.Next, l1 = l1, l1.Next }
        else { curr.Next, l2 = l2, l2.Next }
        curr = curr.Next
    }
    if l1 != nil { curr.Next = l1 } else { curr.Next = l2 }
    return dummy.Next
}
```

**C++**：
```cpp
ListNode* mergeTwoLists(ListNode* l1, ListNode* l2) {
    ListNode dummy(0);
    ListNode* curr = &dummy;
    while (l1 && l2) {
        if (l1->val <= l2->val) { curr->next = l1; l1 = l1->next; }
        else { curr->next = l2; l2 = l2->next; }
        curr = curr->next;
    }
    curr->next = l1 ? l1 : l2;
    return dummy.next;
}
```

---

### 10. 合并K个有序链表（LeetCode 23-Hard）

**题目描述**：合并K个升序链表为一个升序链表。

**输入输出示例**：
```
输入：[[1,4,5], [1,3,4], [2,6]]
输出：[1,1,2,3,4,4,5,6]
```

**实际业务场景**：
- 多数据源归并：数据库分库分表后，跨库排序查询需要归并
- 外部排序：磁盘上K个有序文件的归并
- 多路归并索引：搜索引擎多个分片结果归并

**Java**：
```java
public ListNode mergeKLists(ListNode[] lists) {
    PriorityQueue<ListNode> pq = new PriorityQueue<>((a, b) -> a.val - b.val);
    for (ListNode node : lists) if (node != null) pq.offer(node);
    ListNode dummy = new ListNode(0), curr = dummy;
    while (!pq.isEmpty()) {
        ListNode min = pq.poll();
        curr.next = min; curr = curr.next;
        if (min.next != null) pq.offer(min.next);
    }
    return dummy.next;
}
```

**Python**：
```python
import heapq
def mergeKLists(self, lists: List[ListNode]) -> ListNode:
    pq = []
    for i, node in enumerate(lists):
        if node: heapq.heappush(pq, (node.val, i, node))
    dummy = curr = ListNode(0)
    while pq:
        _, i, node = heapq.heappop(pq)
        curr.next = node; curr = curr.next
        if node.next: heapq.heappush(pq, (node.next.val, i, node.next))
    return dummy.next
```

**Go**：
```go
func mergeKLists(lists []*ListNode) *ListNode {
    h := &IntHeap{}; heap.Init(h)
    for _, node := range lists {
        if node != nil { heap.Push(h, node) }
    }
    dummy := &ListNode{}; curr := dummy
    for h.Len() > 0 {
        min := heap.Pop(h).(*ListNode)
        curr.Next = min; curr = curr.Next
        if min.Next != nil { heap.Push(h, min.Next) }
    }
    return dummy.Next
}
```

**C++**：
```cpp
ListNode* mergeKLists(vector<ListNode*>& lists) {
    auto cmp = [](ListNode* a, ListNode* b) { return a->val > b->val; };
    priority_queue<ListNode*, vector<ListNode*>, decltype(cmp)> pq(cmp);
    for (auto node : lists) if (node) pq.push(node);
    ListNode dummy(0); ListNode* curr = &dummy;
    while (!pq.empty()) {
        auto min = pq.top(); pq.pop();
        curr->next = min; curr = curr->next;
        if (min->next) pq.push(min->next);
    }
    return dummy.next;
}
```

---

### 11. 删除链表倒数第N个节点（LeetCode 19-Medium）

**题目描述**：删除链表倒数第N个节点，返回头节点。

**输入输出示例**：
```
输入：head = [1,2,3,4,5], n = 2
输出：[1,2,3,5]
解释：删除倒数第2个节点（值为4）
```

**实际业务场景**：
- 缓存淘汰：删除倒数第N个访问时间的数据（LRU变体）
- 日志清理：保留最近N条，删除更早的
- 队列剪裁：固定长度队列，删除队尾前第N个元素

**Java**：
```java
public ListNode removeNthFromEnd(ListNode head, int n) {
    ListNode dummy = new ListNode(0, head), fast = dummy, slow = dummy;
    for (int i = 0; i <= n; i++) fast = fast.next;
    while (fast != null) { fast = fast.next; slow = slow.next; }
    slow.next = slow.next.next;
    return dummy.next;
}
```

**Python**：
```python
def removeNthFromEnd(self, head: ListNode, n: int) -> ListNode:
    dummy = ListNode(0, head)
    fast = slow = dummy
    for _ in range(n + 1): fast = fast.next
    while fast:
        fast, slow = fast.next, slow.next
    slow.next = slow.next.next
    return dummy.next
```

**Go**：
```go
func removeNthFromEnd(head *ListNode, n int) *ListNode {
    dummy := &ListNode{Next: head}
    fast, slow := dummy, dummy
    for i := 0; i <= n; i++ { fast = fast.Next }
    for fast != nil { fast, slow = fast.Next, slow.Next }
    slow.Next = slow.Next.Next
    return dummy.Next
}
```

**C++**：
```cpp
ListNode* removeNthFromEnd(ListNode* head, int n) {
    ListNode dummy(0, head);
    ListNode *fast = &dummy, *slow = &dummy;
    for (int i = 0; i <= n; i++) fast = fast->next;
    while (fast) { fast = fast->next; slow = slow->next; }
    slow->next = slow->next->next;
    return dummy.next;
}
```

---

### 12. 链表排序（LeetCode 148-Medium）

**题目描述**：对链表进行升序排序，要求 O(n log n) 时间。

**输入输出示例**：
```
输入：4→2→1→3
输出：1→2→3→4
```

**实际业务场景**：
- 消息排序：按优先级对通知消息链表排序
- 排行榜更新：定时对玩家排名链表重新排序
- 订单合并：多个渠道订单链表排序后合并

**Java（归并排序）**：
```java
public ListNode sortList(ListNode head) {
    if (head == null || head.next == null) return head;
    ListNode mid = findMid(head), right = mid.next;
    mid.next = null;
    return merge(sortList(head), sortList(right));
}
private ListNode findMid(ListNode head) {
    ListNode slow = head, fast = head.next;
    while (fast != null && fast.next != null) { slow = slow.next; fast = fast.next.next; }
    return slow;
}
private ListNode merge(ListNode l1, ListNode l2) {
    ListNode dummy = new ListNode(0), curr = dummy;
    while (l1 != null && l2 != null) {
        if (l1.val <= l2.val) { curr.next = l1; l1 = l1.next; }
        else { curr.next = l2; l2 = l2.next; }
        curr = curr.next;
    }
    curr.next = l1 != null ? l1 : l2;
    return dummy.next;
}
```

**Python**：
```python
def sortList(self, head: ListNode) -> ListNode:
    if not head or not head.next: return head
    slow, fast = head, head.next
    while fast and fast.next: slow, fast = slow.next, fast.next.next
    mid, slow.next = slow.next, None
    return self.merge(self.sortList(head), self.sortList(mid))

def merge(self, l1, l2):
    dummy = curr = ListNode(0)
    while l1 and l2:
        if l1.val <= l2.val: curr.next, l1 = l1, l1.next
        else: curr.next, l2 = l2, l2.next
        curr = curr.next
    curr.next = l1 or l2
    return dummy.next
```

**Go**：
```go
func sortList(head *ListNode) *ListNode {
    if head == nil || head.Next == nil { return head }
    mid := findMid(head)
    right := mid.Next; mid.Next = nil
    return merge(sortList(head), sortList(right))
}
func findMid(head *ListNode) *ListNode {
    slow, fast := head, head.Next
    for fast != nil && fast.Next != nil { slow, fast = slow.Next, fast.Next.Next }
    return slow
}
```

**C++**：
```cpp
ListNode* sortList(ListNode* head) {
    if (!head || !head->next) return head;
    auto findMid = [](ListNode* h) {
        ListNode *s = h, *f = h->next;
        while (f && f->next) { s = s->next; f = f->next->next; }
        return s;
    };
    ListNode* mid = findMid(head);
    ListNode* right = mid->next; mid->next = nullptr;
    auto l = sortList(head), r = sortList(right);
    // merge
    ListNode dummy(0), *curr = &dummy;
    while (l && r) {
        if (l->val <= r->val) { curr->next = l; l = l->next; }
        else { curr->next = r; r = r->next; }
        curr = curr->next;
    }
    curr->next = l ? l : r;
    return dummy.next;
}
```

---

### 13. 回文链表（LeetCode 234-Easy）

**题目描述**：判断链表是否是回文结构。

**输入输出示例**：
```
输入：1→2→2→1
输出：true

输入：1→2
输出：false
```

**实际业务场景**：
- 字符序列验证：基因序列回文检测
- 数据完整性校验：某些协议中数据段回文验证
- 用户输入校验：注册ID回文检测

**Java**：
```java
public boolean isPalindrome(ListNode head) {
    if (head == null || head.next == null) return true;
    ListNode slow = head, fast = head;
    while (fast.next != null && fast.next.next != null) { slow = slow.next; fast = fast.next.next; }
    ListNode second = reverse(slow.next), first = head;
    while (second != null) {
        if (first.val != second.val) return false;
        first = first.next; second = second.next;
    }
    return true;
}
private ListNode reverse(ListNode head) {
    ListNode prev = null;
    while (head != null) { ListNode nxt = head.next; head.next = prev; prev = head; head = nxt; }
    return prev;
}
```

**Python**：
```python
def isPalindrome(self, head: ListNode) -> bool:
    slow = fast = head
    while fast and fast.next: slow, fast = slow.next, fast.next.next
    rev = self.reverse(slow)
    while rev:
        if head.val != rev.val: return False
        head, rev = head.next, rev.next
    return True

def reverse(self, head):
    prev = None
    while head: head.next, prev, head = prev, head, head.next
    return prev
```

**Go**：
```go
func isPalindrome(head *ListNode) bool {
    slow, fast := head, head
    for fast != nil && fast.Next != nil { slow, fast = slow.Next, fast.Next.Next }
    rev := reverse(slow)
    for rev != nil {
        if head.Val != rev.Val { return false }
        head, rev = head.Next, rev.Next
    }
    return true
}
```

**C++**：
```cpp
bool isPalindrome(ListNode* head) {
    auto reverse = [](ListNode* h) {
        ListNode *prev = nullptr, *curr = h;
        while (curr) { auto nxt = curr->next; curr->next = prev; prev = curr; curr = nxt; }
        return prev;
    };
    ListNode *slow = head, *fast = head;
    while (fast && fast->next) { slow = slow->next; fast = fast->next->next; }
    ListNode *rev = reverse(slow);
    while (rev) {
        if (head->val != rev->val) return false;
        head = head->next; rev = rev->next;
    }
    return true;
}
```

---

### 14. 两数相加（LeetCode 2-Medium）

**题目描述**：两个非空链表表示逆序存储的非负整数，求两数之和的链表表示。

**输入输出示例**：
```
输入：l1 = [2,4,3], l2 = [5,6,4]
输出：[7,0,8]
解释：342 + 465 = 807
```

**实际业务场景**：
- 大数计算：超出int64范围的大整数加法
- 区块链地址运算：256位整数运算
- 金额精确计算：避免浮点数精度丢失

**Java**：
```java
public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
    ListNode dummy = new ListNode(0), curr = dummy;
    int carry = 0;
    while (l1 != null || l2 != null || carry > 0) {
        int sum = carry;
        if (l1 != null) { sum += l1.val; l1 = l1.next; }
        if (l2 != null) { sum += l2.val; l2 = l2.next; }
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
    dummy = curr = ListNode(0)
    carry = 0
    while l1 or l2 or carry:
        total = carry
        if l1: total, l1 = total + l1.val, l1.next
        if l2: total, l2 = total + l2.val, l2.next
        carry, total = divmod(total, 10)
        curr.next = ListNode(total); curr = curr.next
    return dummy.next
```

**Go**：
```go
func addTwoNumbers(l1, l2 *ListNode) *ListNode {
    dummy := &ListNode{}; curr := dummy
    carry := 0
    for l1 != nil || l2 != nil || carry > 0 {
        sum := carry
        if l1 != nil { sum += l1.Val; l1 = l1.Next }
        if l2 != nil { sum += l2.Val; l2 = l2.Next }
        carry = sum / 10
        curr.Next = &ListNode{Val: sum % 10}
        curr = curr.Next
    }
    return dummy.Next
}
```

**C++**：
```cpp
ListNode* addTwoNumbers(ListNode* l1, ListNode* l2) {
    ListNode dummy(0); ListNode* curr = &dummy;
    int carry = 0;
    while (l1 || l2 || carry) {
        int sum = carry;
        if (l1) { sum += l1->val; l1 = l1->next; }
        if (l2) { sum += l2->val; l2 = l2->next; }
        carry = sum / 10;
        curr->next = new ListNode(sum % 10);
        curr = curr->next;
    }
    return dummy.next;
}
```

---

## 三、栈/队列

### 15. 有效括号（LeetCode 20-Easy）

**题目描述**：给定只含 `()[]{}` 的字符串，判断括号是否有效闭合。

**输入输出示例**：
```
输入：s = "()[]{}"  → true
输入：s = "([)]"    → false
输入：s = "{[]}"    → true
```

**实际业务场景**：
- 代码编辑器：检查括号是否匹配
- JSON/XML解析：标签嵌套合法性检查
- SQL注入防护：检测不完整的括号语法

**Java**：
```java
public boolean isValid(String s) {
    Stack<Character> stack = new Stack<>();
    for (char c : s.toCharArray()) {
        if (c == '(') stack.push(')');
        else if (c == '[') stack.push(']');
        else if (c == '{') stack.push('}');
        else if (stack.isEmpty() || stack.pop() != c) return false;
    }
    return stack.isEmpty();
}
```

**Python**：
```python
def isValid(self, s: str) -> bool:
    stack = []
    pairs = {')': '(', ']': '[', '}': '{'}
    for c in s:
        if c in '([{':
            stack.append(c)
        else:
            if not stack or stack.pop() != pairs[c]:
                return False
    return not stack
```

**Go**：
```go
func isValid(s string) bool {
    stack := []rune{}
    for _, c := range s {
        if c == '(' { stack = append(stack, ')') }
        else if c == '[' { stack = append(stack, ']') }
        else if c == '{' { stack = append(stack, '}') }
        else {
            if len(stack) == 0 || stack[len(stack)-1] != c { return false }
            stack = stack[:len(stack)-1]
        }
    }
    return len(stack) == 0
}
```

**C++**：
```cpp
bool isValid(string s) {
    stack<char> st;
    for (char c : s) {
        if (c == '(') st.push(')');
        else if (c == '[') st.push(']');
        else if (c == '{') st.push('}');
        else {
            if (st.empty() || st.top() != c) return false;
            st.pop();
        }
    }
    return st.empty();
}
```

---

### 16. 最小栈（LeetCode 155-Easy）

**题目描述**：设计一个栈，支持 push、pop、top 和 getMin（O(1)时间）。

**输入输出示例**：
```
MinStack minStack = new MinStack();
minStack.push(-2);
minStack.push(0);
minStack.push(-3);
minStack.getMin();  // 返回 -3
minStack.pop();
minStack.top();     // 返回 0
minStack.getMin();  // 返回 -2
```

**实际业务场景**：
- 股票行情：实时维护价格栈的最低点
- 限时优惠：商品价格变动时快速获取历史最低价
- 系统监控：实时查看CPU使用率的历史最小值

**Java**：
```java
class MinStack {
    Stack<Integer> stack = new Stack<>();
    Stack<Integer> minStack = new Stack<>();
    public void push(int val) {
        stack.push(val);
        if (minStack.isEmpty() || val <= minStack.peek()) minStack.push(val);
    }
    public void pop() { if (stack.pop().equals(minStack.peek())) minStack.pop(); }
    public int top() { return stack.peek(); }
    public int getMin() { return minStack.peek(); }
}
```

**Python**：
```python
class MinStack:
    def __init__(self):
        self.stack = []
        self.min_stack = []
    def push(self, val: int) -> None:
        self.stack.append(val)
        if not self.min_stack or val <= self.min_stack[-1]:
            self.min_stack.append(val)
    def pop(self) -> None:
        if self.stack.pop() == self.min_stack[-1]:
            self.min_stack.pop()
    def top(self) -> int: return self.stack[-1]
    def getMin(self) -> int: return self.min_stack[-1]
```

**Go**：
```go
type MinStack struct { stack, minStack []int }
func Constructor() MinStack { return MinStack{} }
func (s *MinStack) Push(val int) {
    s.stack = append(s.stack, val)
    if len(s.minStack) == 0 || val <= s.minStack[len(s.minStack)-1] {
        s.minStack = append(s.minStack, val)
    }
}
func (s *MinStack) Pop() {
    if s.stack[len(s.stack)-1] == s.minStack[len(s.minStack)-1] {
        s.minStack = s.minStack[:len(s.minStack)-1]
    }
    s.stack = s.stack[:len(s.stack)-1]
}
func (s *MinStack) Top() int { return s.stack[len(s.stack)-1] }
func (s *MinStack) GetMin() int { return s.minStack[len(s.minStack)-1] }
```

**C++**：
```cpp
class MinStack {
    stack<int> st, minSt;
public:
    void push(int val) {
        st.push(val);
        if (minSt.empty() || val <= minSt.top()) minSt.push(val);
    }
    void pop() {
        if (st.top() == minSt.top()) minSt.pop();
        st.pop();
    }
    int top() { return st.top(); }
    int getMin() { return minSt.top(); }
};
```

---

## 四、树

### 17. 二叉树层序遍历（LeetCode 102-Medium）

**题目描述**：按层遍历二叉树，每层从左到右返回节点值。

**输入输出示例**：
```
输入：    3
        / \
       9  20
         /  \
        15   7
输出：[[3], [9, 20], [15, 7]]
```

**实际业务场景**：
- 组织架构：公司层级关系展示
- 目录遍历：文件系统逐层遍历
- 社交网络：按距离（层数）展示好友关系

**Java**：
```java
public List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;
    Queue<TreeNode> q = new LinkedList<>(); q.offer(root);
    while (!q.isEmpty()) {
        int size = q.size();
        List<Integer> level = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            TreeNode n = q.poll();
            level.add(n.val);
            if (n.left != null) q.offer(n.left);
            if (n.right != null) q.offer(n.right);
        }
        result.add(level);
    }
    return result;
}
```

**Python**：
```python
def levelOrder(self, root: TreeNode) -> List[List[int]]:
    if not root: return []
    from collections import deque
    q, result = deque([root]), []
    while q:
        level = []
        for _ in range(len(q)):
            n = q.popleft()
            level.append(n.val)
            if n.left: q.append(n.left)
            if n.right: q.append(n.right)
        result.append(level)
    return result
```

**Go**：
```go
func levelOrder(root *TreeNode) [][]int {
    if root == nil { return nil }
    var result [][]int
    q := []*TreeNode{root}
    for len(q) > 0 {
        var level []int
        for size := len(q); size > 0; size-- {
            n := q[0]; q = q[1:]
            level = append(level, n.Val)
            if n.Left != nil { q = append(q, n.Left) }
            if n.Right != nil { q = append(q, n.Right) }
        }
        result = append(result, level)
    }
    return result
}
```

**C++**：
```cpp
vector<vector<int>> levelOrder(TreeNode* root) {
    vector<vector<int>> result;
    if (!root) return result;
    queue<TreeNode*> q; q.push(root);
    while (!q.empty()) {
        int size = q.size();
        vector<int> level;
        for (int i = 0; i < size; i++) {
            auto n = q.front(); q.pop();
            level.push_back(n->val);
            if (n->left) q.push(n->left);
            if (n->right) q.push(n->right);
        }
        result.push_back(level);
    }
    return result;
}
```

---

### 18. 二叉树最大深度（LeetCode 104-Easy）

**题目描述**：求二叉树的最大深度（根到最远叶子的节点数）。

**输入输出示例**：
```
输入：[3,9,20,null,null,15,7]
输出：3
```

**实际业务场景**：
- 菜单层级：求导航菜单最大嵌套深度
- 审批流程：求审批链的最长路径
- 文件系统：计算目录结构最大深度

**Java**：
```java
public int maxDepth(TreeNode root) {
    if (root == null) return 0;
    return Math.max(maxDepth(root.left), maxDepth(root.right)) + 1;
}
```

**Python**：
```python
def maxDepth(self, root: TreeNode) -> int:
    if not root: return 0
    return max(self.maxDepth(root.left), self.maxDepth(root.right)) + 1
```

**Go**：
```go
func maxDepth(root *TreeNode) int {
    if root == nil { return 0 }
    l, r := maxDepth(root.Left), maxDepth(root.Right)
    if l > r { return l + 1 }
    return r + 1
}
```

**C++**：
```cpp
int maxDepth(TreeNode* root) {
    if (!root) return 0;
    return max(maxDepth(root->left), maxDepth(root->right)) + 1;
}
```

---

### 19. 验证二叉搜索树（LeetCode 98-Medium）

**题目描述**：判断给定二叉树是否是一个有效的 BST（左 < 根 < 右）。

**输入输出示例**：
```
输入：[2,1,3] → true
输入：[5,1,4,null,null,3,6] → false（4的右子树有3<4）
```

**实际业务场景**：
- 数据库索引校验：验证B+树索引是否有效
- 权限系统：权限层级必须左小右大
- 价格区间校验：商品价格树结构合法性检查

**Java**：
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

**Python**：
```python
def isValidBST(self, root: TreeNode) -> bool:
    def helper(node, lo, hi):
        if not node: return True
        if node.val <= lo or node.val >= hi: return False
        return helper(node.left, lo, node.val) and helper(node.right, node.val, hi)
    return helper(root, float('-inf'), float('inf'))
```

**Go**：
```go
func isValidBST(root *TreeNode) bool {
    var helper func(*TreeNode, int64, int64) bool
    helper = func(n *TreeNode, min, max int64) bool {
        if n == nil { return true }
        v := int64(n.Val)
        if v <= min || v >= max { return false }
        return helper(n.Left, min, v) && helper(n.Right, v, max)
    }
    return helper(root, math.MinInt64, math.MaxInt64)
}
```

**C++**：
```cpp
bool isValidBST(TreeNode* root) {
    function<bool(TreeNode*, long, long)> helper = [&](TreeNode* n, long lo, long hi) {
        if (!n) return true;
        if (n->val <= lo || n->val >= hi) return false;
        return helper(n->left, lo, n->val) && helper(n->right, n->val, hi);
    };
    return helper(root, LONG_MIN, LONG_MAX);
}
```

---

### 20. 最近公共祖先（LeetCode 236-Medium）

**题目描述**：找出二叉树中两个节点的最近公共祖先（LCA）。

**输入输出示例**：
```
输入：root = [3,5,1,6,2,0,8,null,null,7,4], p=5, q=1
输出：3
解释：节点5和1的LCA是3
```

**实际业务场景**：
- 组织架构：找两个员工的共同上级
- 文件系统：找两个文件的公共父目录
- 社交网络：找两个用户的共同好友圈

**Java**：
```java
public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
    if (root == null || root == p || root == q) return root;
    TreeNode left = lowestCommonAncestor(root.left, p, q);
    TreeNode right = lowestCommonAncestor(root.right, p, q);
    if (left != null && right != null) return root;
    return left != null ? left : right;
}
```

**Python**：
```python
def lowestCommonAncestor(self, root, p, q):
    if not root or root == p or root == q: return root
    left = self.lowestCommonAncestor(root.left, p, q)
    right = self.lowestCommonAncestor(root.right, p, q)
    if left and right: return root
    return left or right
```

**Go**：
```go
func lowestCommonAncestor(root, p, q *TreeNode) *TreeNode {
    if root == nil || root == p || root == q { return root }
    l := lowestCommonAncestor(root.Left, p, q)
    r := lowestCommonAncestor(root.Right, p, q)
    if l != nil && r != nil { return root }
    if l != nil { return l }; return r
}
```

**C++**：
```cpp
TreeNode* lowestCommonAncestor(TreeNode* root, TreeNode* p, TreeNode* q) {
    if (!root || root == p || root == q) return root;
    auto l = lowestCommonAncestor(root->left, p, q);
    auto r = lowestCommonAncestor(root->right, p, q);
    if (l && r) return root;
    return l ? l : r;
}
```

---

## 五、动态规划

### 21. 爬楼梯（LeetCode 70-Easy）

**题目描述**：爬 n 级楼梯，每次可爬 1 或 2 级，求有多少种爬法。

**输入输出示例**：
```
输入：n=2 → 输出：2（1+1, 2）
输入：n=3 → 输出：3（1+1+1, 1+2, 2+1）
```

**实际业务场景**：
- 组合方案计算：商品凑单时不同面额券的组合
- 路径规划：网格中不同步长到达终点的方案数
- 积分兑换：不同面值积分兑换方案数量

**Java**：
```java
public int climbStairs(int n) {
    if (n <= 2) return n;
    int prev2 = 1, prev1 = 2;
    for (int i = 3; i <= n; i++) {
        int curr = prev1 + prev2;
        prev2 = prev1; prev1 = curr;
    }
    return prev1;
}
```

**Python**：
```python
def climbStairs(self, n: int) -> int:
    if n <= 2: return n
    a, b = 1, 2
    for _ in range(3, n + 1): a, b = b, a + b
    return b
```

**Go**：
```go
func climbStairs(n int) int {
    if n <= 2 { return n }
    a, b := 1, 2
    for i := 3; i <= n; i++ { a, b = b, a + b }
    return b
}
```

**C++**：
```cpp
int climbStairs(int n) {
    if (n <= 2) return n;
    int a = 1, b = 2;
    for (int i = 3; i <= n; i++) { int t = a + b; a = b; b = t; }
    return b;
}
```

---

### 22. 最长递增子序列（LeetCode 300-Medium）

**题目描述**：找出无序数组中最长严格递增子序列的长度。

**输入输出示例**：
```
输入：[10,9,2,5,3,7,101,18]
输出：4
解释：最长递增子序列 [2,3,7,101]
```

**实际业务场景**：
- 股票分析：找出最长的连续上涨交易日
- 版本管理：找出最长的递增版本号序列
- 日志分析：找出持续增长的请求量时间段

**Java（贪心+二分）**：
```java
public int lengthOfLIS(int[] nums) {
    List<Integer> tails = new ArrayList<>();
    for (int num : nums) {
        int pos = Collections.binarySearch(tails, num);
        if (pos < 0) pos = -(pos + 1);
        if (pos == tails.size()) tails.add(num);
        else tails.set(pos, num);
    }
    return tails.size();
}
```

**Python**：
```python
def lengthOfLIS(self, nums: List[int]) -> int:
    import bisect
    tails = []
    for num in nums:
        pos = bisect.bisect_left(tails, num)
        if pos == len(tails): tails.append(num)
        else: tails[pos] = num
    return len(tails)
```

**Go**：
```go
func lengthOfLIS(nums []int) int {
    tails := []int{}
    for _, num := range nums {
        pos := sort.SearchInts(tails, num)
        if pos == len(tails) { tails = append(tails, num) }
        else { tails[pos] = num }
    }
    return len(tails)
}
```

**C++**：
```cpp
int lengthOfLIS(vector<int>& nums) {
    vector<int> tails;
    for (int num : nums) {
        auto it = lower_bound(tails.begin(), tails.end(), num);
        if (it == tails.end()) tails.push_back(num);
        else *it = num;
    }
    return tails.size();
}
```

---

### 23. 最长公共子序列（LeetCode 1143-Medium）

**题目描述**：返回两个字符串的最长公共子序列的长度。

**输入输出示例**：
```
输入：text1="abcde", text2="ace"
输出：3
解释：最长公共子序列是 "ace"
```

**实际业务场景**：
- 代码差异比较：Git diff 本质是LCS问题
- 基因比对：两条DNA序列的相似度计算
- 文本相似度：抄袭检测

**Java**：
```java
public int longestCommonSubsequence(String t1, String t2) {
    int m = t1.length(), n = t2.length();
    int[] dp = new int[n + 1];
    for (int i = 1; i <= m; i++) {
        int prev = 0;
        for (int j = 1; j <= n; j++) {
            int temp = dp[j];
            dp[j] = t1.charAt(i-1) == t2.charAt(j-1) ? prev + 1 : Math.max(dp[j], dp[j-1]);
            prev = temp;
        }
    }
    return dp[n];
}
```

**Python**：
```python
def longestCommonSubsequence(self, t1: str, t2: str) -> int:
    m, n = len(t1), len(t2)
    dp = [[0]*(n+1) for _ in range(m+1)]
    for i in range(1, m+1):
        for j in range(1, n+1):
            dp[i][j] = dp[i-1][j-1] + 1 if t1[i-1] == t2[j-1] else max(dp[i-1][j], dp[i][j-1])
    return dp[m][n]
```

**Go**：
```go
func longestCommonSubsequence(t1, t2 string) int {
    m, n := len(t1), len(t2)
    dp := make([][]int, m+1)
    for i := range dp { dp[i] = make([]int, n+1) }
    for i := 1; i <= m; i++ {
        for j := 1; j <= n; j++ {
            if t1[i-1] == t2[j-1] { dp[i][j] = dp[i-1][j-1] + 1 }
            else { dp[i][j] = max(dp[i-1][j], dp[i][j-1]) }
        }
    }
    return dp[m][n]
}
```

**C++**：
```cpp
int longestCommonSubsequence(string t1, string t2) {
    int m = t1.size(), n = t2.size();
    vector<int> dp(n + 1);
    for (int i = 1; i <= m; i++) {
        int prev = 0;
        for (int j = 1; j <= n; j++) {
            int temp = dp[j];
            dp[j] = t1[i-1] == t2[j-1] ? prev + 1 : max(dp[j], dp[j-1]);
            prev = temp;
        }
    }
    return dp[n];
}
```

---

### 24. 零钱兑换（LeetCode 322-Medium）

**题目描述**：给定不同面额硬币，计算凑出目标金额所需的最少硬币数。

**输入输出示例**：
```
输入：coins=[1,2,5], amount=11
输出：3
解释：5+5+1=11
```

**实际业务场景**：
- 支付系统：给用户找零最少硬币/纸币组合
- 红包算法：拆红包时不同面额组合
- ATM吐钞：计算最少钞票数量

**Java**：
```java
public int coinChange(int[] coins, int amount) {
    int[] dp = new int[amount + 1];
    Arrays.fill(dp, amount + 1);
    dp[0] = 0;
    for (int i = 1; i <= amount; i++)
        for (int c : coins)
            if (c <= i) dp[i] = Math.min(dp[i], dp[i - c] + 1);
    return dp[amount] > amount ? -1 : dp[amount];
}
```

**Python**：
```python
def coinChange(self, coins: List[int], amount: int) -> int:
    dp = [amount + 1] * (amount + 1)
    dp[0] = 0
    for i in range(1, amount + 1):
        for c in coins:
            if c <= i: dp[i] = min(dp[i], dp[i - c] + 1)
    return dp[amount] if dp[amount] <= amount else -1
```

**Go**：
```go
func coinChange(coins []int, amount int) int {
    dp := make([]int, amount+1)
    for i := range dp { dp[i] = amount + 1 }
    dp[0] = 0
    for i := 1; i <= amount; i++ {
        for _, c := range coins {
            if c <= i && dp[i-c]+1 < dp[i] { dp[i] = dp[i-c] + 1 }
        }
    }
    if dp[amount] > amount { return -1 }; return dp[amount]
}
```

**C++**：
```cpp
int coinChange(vector<int>& coins, int amount) {
    vector<int> dp(amount + 1, amount + 1);
    dp[0] = 0;
    for (int i = 1; i <= amount; i++)
        for (int c : coins)
            if (c <= i) dp[i] = min(dp[i], dp[i - c] + 1);
    return dp[amount] > amount ? -1 : dp[amount];
}
```

---

### 25. 买卖股票的最佳时机（LeetCode 121/122-Easy/Medium）

**题目描述**：
- 121：只能买卖一次，求最大利润
- 122：可买卖多次，求最大总利润

**输入输出示例**：
```
121：[7,1,5,3,6,4] → 输出：5（1买6卖）
122：[7,1,5,3,6,4] → 输出：7（1买5卖+3买6卖）
```

**实际业务场景**：
- 股票交易系统：计算最大收益
- 外汇套利：汇率波动差价计算
- 商品期货：多笔交易累计利润最大化

**Java（121+122）**：
```java
// 121 只能一次
public int maxProfit(int[] prices) {
    int minPrice = Integer.MAX_VALUE, profit = 0;
    for (int p : prices) {
        minPrice = Math.min(minPrice, p);
        profit = Math.max(profit, p - minPrice);
    }
    return profit;
}
// 122 多次交易
public int maxProfit(int[] prices) {
    int profit = 0;
    for (int i = 1; i < prices.length; i++)
        if (prices[i] > prices[i-1]) profit += prices[i] - prices[i-1];
    return profit;
}
```

**Python（121+122）**：
```python
# 121
def maxProfit(self, prices): 
    min_p, profit = float('inf'), 0
    for p in prices:
        min_p = min(min_p, p)
        profit = max(profit, p - min_p)
    return profit
# 122
def maxProfit(self, prices):
    return sum(prices[i]-prices[i-1] for i in range(1, len(prices)) if prices[i] > prices[i-1])
```

**Go（121+122）**：
```go
// 121
func maxProfit(prices []int) int {
    minPrice, profit := int(1e9), 0
    for _, p := range prices {
        if p < minPrice { minPrice = p }
        if p - minPrice > profit { profit = p - minPrice }
    }
    return profit
}
// 122
func maxProfit(prices []int) int {
    profit := 0
    for i := 1; i < len(prices); i++ {
        if prices[i] > prices[i-1] { profit += prices[i] - prices[i-1] }
    }
    return profit
}
```

**C++（121+122）**：
```cpp
// 121
int maxProfit(vector<int>& prices) {
    int minPrice = INT_MAX, profit = 0;
    for (int p : prices) {
        minPrice = min(minPrice, p);
        profit = max(profit, p - minPrice);
    }
    return profit;
}
// 122
int maxProfit(vector<int>& prices) {
    int profit = 0;
    for (int i = 1; i < prices.size(); i++)
        if (prices[i] > prices[i-1]) profit += prices[i] - prices[i-1];
    return profit;
}
```

---

## 六、回溯

### 26. 全排列（LeetCode 46-Medium）

**题目描述**：返回无重复数字数组的所有全排列。

**输入输出示例**：
```
输入：[1,2,3]
输出：[[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]
```

**实际业务场景**：
- 排列组合计算：航班中转城市的所有顺序
- 密码破解：生成所有可能的排列尝试
- 调度问题：工人排班所有可能的安排

**Java**：
```java
public List<List<Integer>> permute(int[] nums) {
    List<List<Integer>> res = new ArrayList<>();
    backtrack(nums, new ArrayList<>(), new boolean[nums.length], res);
    return res;
}
void backtrack(int[] nums, List<Integer> path, boolean[] used, List<List<Integer>> res) {
    if (path.size() == nums.length) { res.add(new ArrayList<>(path)); return; }
    for (int i = 0; i < nums.length; i++) {
        if (used[i]) continue;
        used[i] = true; path.add(nums[i]);
        backtrack(nums, path, used, res);
        used[i] = false; path.remove(path.size() - 1);
    }
}
```

**Python**：
```python
def permute(self, nums: List[int]) -> List[List[int]]:
    res = []
    def backtrack(path, used):
        if len(path) == len(nums): res.append(path[:]); return
        for i, n in enumerate(nums):
            if used[i]: continue
            used[i] = True; path.append(n)
            backtrack(path, used)
            used[i] = False; path.pop()
    backtrack([], [False]*len(nums))
    return res
```

**Go**：
```go
func permute(nums []int) [][]int {
    var res [][]int
    used := make([]bool, len(nums))
    var backtrack func([]int)
    backtrack = func(path []int) {
        if len(path) == len(nums) { res = append(res, append([]int{}, path...)); return }
        for i, n := range nums {
            if used[i] { continue }
            used[i] = true; backtrack(append(path, n)); used[i] = false
        }
    }
    backtrack([]int{})
    return res
}
```

**C++**：
```cpp
vector<vector<int>> permute(vector<int>& nums) {
    vector<vector<int>> res;
    vector<bool> used(nums.size());
    vector<int> path;
    function<void()> backtrack = [&]() {
        if (path.size() == nums.size()) { res.push_back(path); return; }
        for (int i = 0; i < nums.size(); i++) {
            if (used[i]) continue;
            used[i] = true; path.push_back(nums[i]);
            backtrack();
            used[i] = false; path.pop_back();
        }
    };
    backtrack();
    return res;
}
```

---

### 27. 子集（LeetCode 78-Medium）

**题目描述**：返回数组所有可能的子集（幂集）。

**输入输出示例**：
```
输入：[1,2,3]
输出：[[],[1],[2],[3],[1,2],[1,3],[2,3],[1,2,3]]
```

**实际业务场景**：
- 套餐组合：外卖套餐中菜品的所有组合
- 权限管理：角色的权限子集组合
- 特征工程：所有特征子集的选择

**Java**：
```java
public List<List<Integer>> subsets(int[] nums) {
    List<List<Integer>> res = new ArrayList<>();
    backtrack(nums, 0, new ArrayList<>(), res);
    return res;
}
void backtrack(int[] nums, int start, List<Integer> path, List<List<Integer>> res) {
    res.add(new ArrayList<>(path));
    for (int i = start; i < nums.length; i++) {
        path.add(nums[i]);
        backtrack(nums, i + 1, path, res);
        path.remove(path.size() - 1);
    }
}
```

**Python**：
```python
def subsets(self, nums: List[int]) -> List[List[int]]:
    res = []
    def backtrack(start, path):
        res.append(path[:])
        for i in range(start, len(nums)):
            path.append(nums[i])
            backtrack(i + 1, path)
            path.pop()
    backtrack(0, [])
    return res
```

**Go**：
```go
func subsets(nums []int) [][]int {
    var res [][]int
    var backtrack func(int, []int)
    backtrack = func(start int, path []int) {
        res = append(res, append([]int{}, path...))
        for i := start; i < len(nums); i++ {
            backtrack(i+1, append(path, nums[i]))
        }
    }
    backtrack(0, []int{})
    return res
}
```

**C++**：
```cpp
vector<vector<int>> subsets(vector<int>& nums) {
    vector<vector<int>> res;
    vector<int> path;
    function<void(int)> backtrack = [&](int start) {
        res.push_back(path);
        for (int i = start; i < nums.size(); i++) {
            path.push_back(nums[i]);
            backtrack(i + 1);
            path.pop_back();
        }
    };
    backtrack(0);
    return res;
}
```

---

## 七、图

### 28. 岛屿数量（LeetCode 200-Medium）

**题目描述**：网格中'1'是陆地'0'是水，求岛屿数量（岛屿是上下左右连通的陆地）。

**输入输出示例**：
```
输入：grid = [["1","1","0","0","0"],
              ["1","1","0","0","0"],
              ["0","0","1","0","0"],
              ["0","0","0","1","1"]]
输出：3
```

**实际业务场景**：
- 地图分析：卫星图像中识别独立地块
- 社交网络：计算独立社群数量
- 系统监控：识别故障区域的连通性

**Java**：
```java
public int numIslands(char[][] grid) {
    int count = 0;
    for (int i = 0; i < grid.length; i++)
        for (int j = 0; j < grid[0].length; j++)
            if (grid[i][j] == '1') { count++; dfs(grid, i, j); }
    return count;
}
void dfs(char[][] g, int i, int j) {
    if (i < 0 || i >= g.length || j < 0 || j >= g[0].length || g[i][j] != '1') return;
    g[i][j] = '0';
    dfs(g, i+1, j); dfs(g, i-1, j); dfs(g, i, j+1); dfs(g, i, j-1);
}
```

**Python**：
```python
def numIslands(self, grid: List[List[str]]) -> int:
    def dfs(i, j):
        if i < 0 or i >= len(grid) or j < 0 or j >= len(grid[0]) or grid[i][j] != '1':
            return
        grid[i][j] = '0'
        dfs(i+1, j); dfs(i-1, j); dfs(i, j+1); dfs(i, j-1)
    count = 0
    for i in range(len(grid)):
        for j in range(len(grid[0])):
            if grid[i][j] == '1': count += 1; dfs(i, j)
    return count
```

**Go**：
```go
func numIslands(grid [][]byte) int {
    var dfs func(i, j int)
    dfs = func(i, j int) {
        if i < 0 || i >= len(grid) || j < 0 || j >= len(grid[0]) || grid[i][j] != '1' { return }
        grid[i][j] = '0'
        dfs(i+1, j); dfs(i-1, j); dfs(i, j+1); dfs(i, j-1)
    }
    count := 0
    for i := range grid {
        for j := range grid[i] {
            if grid[i][j] == '1' { count++; dfs(i, j) }
        }
    }
    return count
}
```

**C++**：
```cpp
int numIslands(vector<vector<char>>& grid) {
    int count = 0;
    function<void(int,int)> dfs = [&](int i, int j) {
        if (i < 0 || i >= grid.size() || j < 0 || j >= grid[0].size() || grid[i][j] != '1') return;
        grid[i][j] = '0';
        dfs(i+1,j); dfs(i-1,j); dfs(i,j+1); dfs(i,j-1);
    };
    for (int i = 0; i < grid.size(); i++)
        for (int j = 0; j < grid[0].size(); j++)
            if (grid[i][j] == '1') { count++; dfs(i, j); }
    return count;
}
```

---

### 29. 课程表（LeetCode 207-Medium）

**题目描述**：判断是否可能完成所有课程（有向图判环）。

**输入输出示例**：
```
输入：numCourses=2, prerequisites=[[1,0]]
输出：true（先学0再学1）

输入：numCourses=2, prerequisites=[[1,0],[0,1]]
输出：false（循环依赖）
```

**实际业务场景**：
- 编译依赖：项目模块编译顺序检测
- 任务调度：任务依赖关系死锁检测  
- 课程安排：专业课的先修后修关系校验

**Java**：
```java
public boolean canFinish(int n, int[][] pre) {
    List<List<Integer>> g = new ArrayList<>();
    int[] inDeg = new int[n];
    for (int i = 0; i < n; i++) g.add(new ArrayList<>());
    for (int[] p : pre) { g.get(p[1]).add(p[0]); inDeg[p[0]]++; }
    Queue<Integer> q = new LinkedList<>();
    for (int i = 0; i < n; i++) if (inDeg[i] == 0) q.offer(i);
    int count = 0;
    while (!q.isEmpty()) {
        int c = q.poll(); count++;
        for (int next : g.get(c)) if (--inDeg[next] == 0) q.offer(next);
    }
    return count == n;
}
```

**Python**：
```python
def canFinish(self, n: int, pre: List[List[int]]) -> bool:
    g = [[] for _ in range(n)]
    in_deg = [0] * n
    for a, b in pre: g[b].append(a); in_deg[a] += 1
    q = [i for i, d in enumerate(in_deg) if d == 0]
    count = 0
    while q:
        c = q.pop(0); count += 1
        for nx in g[c]:
            in_deg[nx] -= 1
            if in_deg[nx] == 0: q.append(nx)
    return count == n
```

**Go**：
```go
func canFinish(n int, pre [][]int) bool {
    g := make([][]int, n); inDeg := make([]int, n)
    for _, p := range pre { a, b := p[0], p[1]; g[b] = append(g[b], a); inDeg[a]++ }
    q := []int{}
    for i, d := range inDeg { if d == 0 { q = append(q, i) } }
    count := 0
    for len(q) > 0 {
        c := q[0]; q = q[1:]; count++
        for _, nx := range g[c] { inDeg[nx]--; if inDeg[nx] == 0 { q = append(q, nx) } }
    }
    return count == n
}
```

**C++**：
```cpp
bool canFinish(int n, vector<vector<int>>& pre) {
    vector<vector<int>> g(n); vector<int> inDeg(n);
    for (auto& p : pre) { g[p[1]].push_back(p[0]); inDeg[p[0]]++; }
    queue<int> q;
    for (int i = 0; i < n; i++) if (inDeg[i] == 0) q.push(i);
    int count = 0;
    while (!q.empty()) {
        int c = q.front(); q.pop(); count++;
        for (int nx : g[c]) if (--inDeg[nx] == 0) q.push(nx);
    }
    return count == n;
}
```

---

## 八、排序/搜索

### 30. 二分查找（LeetCode 704-Easy）

**题目描述**：在有序数组中搜索目标值，返回下标，不存在返回-1。

**输入输出示例**：
```
输入：nums=[-1,0,3,5,9,12], target=9
输出：4
```

**实际业务场景**：
- 字典查询：二分查找英文词典
- 日志检索：二分定位时间点的日志
- IP地址查询：二分查找IP所属范围

**Java**：
```java
public int search(int[] nums, int target) {
    int left = 0, right = nums.length - 1;
    while (left <= right) {
        int mid = left + (right - left) / 2;
        if (nums[mid] == target) return mid;
        else if (nums[mid] < target) left = mid + 1;
        else right = mid - 1;
    }
    return -1;
}
```

**Python**：
```python
def search(self, nums: List[int], target: int) -> int:
    l, r = 0, len(nums) - 1
    while l <= r:
        mid = (l + r) // 2
        if nums[mid] == target: return mid
        elif nums[mid] < target: l = mid + 1
        else: r = mid - 1
    return -1
```

**Go**：
```go
func search(nums []int, target int) int {
    l, r := 0, len(nums)-1
    for l <= r {
        mid := l + (r-l)/2
        if nums[mid] == target { return mid }
        if nums[mid] < target { l = mid + 1 } else { r = mid - 1 }
    }
    return -1
}
```

**C++**：
```cpp
int search(vector<int>& nums, int target) {
    int l = 0, r = nums.size() - 1;
    while (l <= r) {
        int mid = l + (r - l) / 2;
        if (nums[mid] == target) return mid;
        else if (nums[mid] < target) l = mid + 1;
        else r = mid - 1;
    }
    return -1;
}
```

---

## 九、设计题

### 31. LRU缓存（LeetCode 146-Medium）

**题目描述**：设计LRU(最近最少使用)缓存，支持get/put O(1)。

**输入输出示例**：
```
LRUCache lru = new LRUCache(2);
lru.put(1,1); lru.put(2,2); lru.get(1)=1;
lru.put(3,3); lru.get(2)=-1(已淘汰);
lru.put(4,4); lru.get(1)=-1; lru.get(3)=3; lru.get(4)=4;
```

**实际业务场景**：
- 缓存淘汰策略：Redis内存淘汰
- 浏览器历史：最近访问页面管理
- 数据库Buffer Pool：热点数据管理

**Java**：
```java
class LRUCache {
    class Node { int k, v; Node prev, next; Node(int k, int v) { this.k = k; this.v = v; }}
    Map<Integer, Node> map = new HashMap<>();
    Node head = new Node(0, 0), tail = new Node(0, 0);
    int cap;
    public LRUCache(int cap) {
        this.cap = cap; head.next = tail; tail.prev = head;
    }
    public int get(int k) {
        if (!map.containsKey(k)) return -1;
        Node n = map.get(k); remove(n); addHead(n); return n.v;
    }
    public void put(int k, int v) {
        if (map.containsKey(k)) { Node n = map.get(k); n.v = v; remove(n); addHead(n); }
        else {
            if (map.size() == cap) { map.remove(tail.prev.k); remove(tail.prev); }
            Node n = new Node(k, v); map.put(k, n); addHead(n);
        }
    }
    void remove(Node n) { n.prev.next = n.next; n.next.prev = n.prev; }
    void addHead(Node n) { n.next = head.next; n.prev = head; head.next.prev = n; head.next = n; }
}
```

**Python**：
```python
class LRUCache:
    class Node:
        def __init__(self, k, v): self.k, self.v, self.prev, self.next = k, v, None, None
    def __init__(self, cap):
        self.cap, self.map = cap, {}
        self.head, self.tail = self.Node(0,0), self.Node(0,0)
        self.head.next, self.tail.prev = self.tail, self.head
    def _remove(self, n): n.prev.next, n.next.prev = n.next, n.prev
    def _add(self, n): n.next, n.prev = self.head.next, self.head; self.head.next.prev = n; self.head.next = n
    def get(self, k):
        if k not in self.map: return -1
        n = self.map[k]; self._remove(n); self._add(n); return n.v
    def put(self, k, v):
        if k in self.map: self._remove(self.map[k])
        n = self.Node(k, v); self.map[k] = n; self._add(n)
        if len(self.map) > self.cap: del self.map[self.tail.prev.k]; self._remove(self.tail.prev)
```

**Go**：
```go
type LRUCache struct { cap int; m map[int]*Node; h, t *Node }
type Node struct { k, v int; prev, next *Node }
func Constructor(cap int) LRUCache {
    h, t := &Node{}, &Node{}; h.next, t.prev = t, h
    return LRUCache{cap, make(map[int]*Node), h, t}
}
func (c *LRUCache) Get(k int) int {
    if n, ok := c.m[k]; ok { c.remove(n); c.add(n); return n.v }
    return -1
}
func (c *LRUCache) Put(k, v int) {
    if n, ok := c.m[k]; ok { c.remove(n) }
    n := &Node{k, v, nil, nil}; c.m[k] = n; c.add(n)
    if len(c.m) > c.cap { delete(c.m, c.t.prev.k); c.remove(c.t.prev) }
}
func (c *LRUCache) remove(n *Node) { n.prev.next, n.next.prev = n.next, n.prev }
func (c *LRUCache) add(n *Node) { n.next, n.prev = c.h.next, c.h; c.h.next.prev, c.h.next = n, n }
```

**C++**：
```cpp
class LRUCache {
    struct Node { int k, v; Node *prev, *next; Node(int k, int v): k(k), v(v) {} };
    unordered_map<int, Node*> m;
    Node *head, *tail;
    int cap;
    void remove(Node* n) { n->prev->next = n->next; n->next->prev = n->prev; }
    void add(Node* n) { n->next = head->next; n->prev = head; head->next->prev = n; head->next = n; }
public:
    LRUCache(int cap): cap(cap) {
        head = new Node(0,0); tail = new Node(0,0);
        head->next = tail; tail->prev = head;
    }
    int get(int k) {
        if (!m.count(k)) return -1;
        Node* n = m[k]; remove(n); add(n); return n->v;
    }
    void put(int k, int v) {
        if (m.count(k)) remove(m[k]);
        Node* n = new Node(k, v); m[k] = n; add(n);
        if (m.size() > cap) { m.erase(tail->prev->k); remove(tail->prev); delete tail->prev; }
    }
};
```

---

## 考点总结

| 看到什么 | 想到什么 | 用什么数据结构 |
|---------|---------|-------------|
| 有序 | 二分查找、双指针 | 数组 |
| 子串/子数组 | 滑动窗口 | HashMap/Set |
| 所有组合 | 回溯 | List |
| 最优解/多少种 | 动态规划 | 数组 |
| 图/网格 | DFS/BFS | 栈/队列 |
| 链表 | 快慢指针、虚拟头 | 双指针 |
| 树 | 递归、BFS | 队列 |
| 第K大/小 | 堆、快排partition | 优先队列 |
| 括号/配对 | 栈 | Stack |
| O(1)查找/删除 | HashMap+双向链表 | LRU |

---

## 十、面试追问详细解答

### T1 两数之和追问

**Q：如果数组有序，怎么做？**
用双指针：`left=0, right=n-1`，`sum < target` 则 `left++`，`sum > target` 则 `right--`。这样时间 O(n)，空间 O(1)，不需要 HashMap。

**Q：三数之和/四数之和怎么解？**
三数之和：排序 O(nlogn) + 固定一个数 + 双指针 O(n²)。四数之和同理：排序 + 固定两个数 + 双指针 O(n³)。核心思想都是降维：N数之和 → (N-1)数之和 → 最终到2数之和。

**Q：HashMap的key冲突怎么处理？**
Java 的 HashMap 用链地址法+红黑树。同一个 bucket 的元素用链表串起来，链表长度>8时转红黑树。查找时先 hash → 找到 bucket → 遍历链表/红黑树比较 key。

---

### T2 三数之和追问

**Q：为什么要先排序？**
两个原因：①去重依赖排序，相同元素相邻才能跳过；②双指针依赖有序性，`sum<0`时 `left++` 让和变大，`sum>0`时 `right--` 让和变小。

**Q：去重的具体逻辑是什么？**
三层去重：
- 第一层（固定数）：`if (i > 0 && nums[i]==nums[i-1]) continue`
- 第二层（left）：`while (left<right && nums[left]==nums[left+1]) left++`
- 第三层（right）：`while (left<right && nums[right]==nums[right-1]) right--`

关键在于跳过后还要再移动一次指针，因为此时 left 停在重复元素最后一个。

**Q：四数之和怎么做？**
排序后，固定 i 和 j，然后 `left=j+1, right=n-1` 双指针。复杂度 O(n³)。注意剪枝优化：如果 `nums[i]+最小的三个数 > target` 直接 break。

---

### T3 最长无重复子串追问

**Q：HashMap 版比 HashSet 版好在哪？**
HashMap 存字符→索引，遇到重复时 `left` 可以直接跳到 `max(left, map.get(c)+1)`，不需要 while 循环逐个删除。时间仍然是 O(n)，但实际更快。

```java
public int lengthOfLongestSubstring(String s) {
    Map<Character, Integer> map = new HashMap<>();
    int left = 0, maxLen = 0;
    for (int right = 0; right < s.length(); right++) {
        char c = s.charAt(right);
        if (map.containsKey(c)) {
            left = Math.max(left, map.get(c) + 1);
        }
        map.put(c, right);
        maxLen = Math.max(maxLen, right - left + 1);
    }
    return maxLen;
}
```

**Q：最长最多K个不同字符的子串怎么解？**
用 HashMap 存窗口内每种字符的个数，`map.size() > K` 时收缩左边界，直到某个字符计数归零从 map 中删除。

---

### T4 接雨水追问

**Q：动态规划怎么做？**
先预处理两个数组 `leftMax[i]`和 `rightMax[i]`：
- `leftMax[i]` = `max(height[i], leftMax[i-1])`
- `rightMax[i]` = `max(height[i], rightMax[i+1])`
然后遍历：`water += min(leftMax[i], rightMax[i]) - height[i]`。时间 O(n)，空间 O(n)。

**Q：单调栈怎么做？**
维护递减栈。遇到比栈顶高的柱子时，栈顶就是凹槽底，弹出栈顶作为 `bottom`，宽度=当前索引-新栈顶索引-1，高度=`min(当前高,新栈顶高)-bottom`，面积=宽×高。

**Q：柱状图最大矩形（LeetCode 84）怎么解？**
单调递增栈。当遇到比栈顶小的柱子时，以栈顶为高，向右扩展。关键是哨兵：两端各加一个高度为0的柱子，简化边界处理。

---

### T5 盛水容器追问

**Q：为什么移动较短的边是正确的？**
假设 `height[left] < height[right]`，移动较短边 left：
- 移动 left：宽度-1，新高度可能变大，面积可能更大
- 移动 right：宽度-1，新高度 ≤ height[left]（因为受限于短边），面积一定更小
所以移动短边不会漏掉最优解。

**Q：这题和接雨水的区别？**
盛水容器：选两条线围成容器，面积=min(h1,h2)×宽度；接雨水：每条柱子上的水=min(左max,右max)-自身高度。一个求最大，一个求和。

---

### T6 最小覆盖子串追问

**Q：valid计数是什么意思？**
valid 记录窗口中满足 need 要求的字符种类数。当 `window[c]` 从 `need[c]-1` 变成 `need[c]` 时 valid+1，表示字符 c 的数量刚好满足了要求。当 `valid == need.size()` 时，窗口中包含了 t 的所有字符。

**Q：找所有字母异位词（LeetCode 438）怎么解？**
固定窗口大小 = p.length()。右边界加入，左边界移除，窗口大小等于 p.length() 时检查是否满足条件。不需要 valid 计数，直接比较 count 数组。

---

### T7 反转链表追问

**Q：递归反转的详细过程？**
```
reverseList(1→2→3→null):
  递归到底：reverseList(3→null) → 返回 3
  回溯：head=2, head.next.next=3.next=head=2 → 3→2
       head.next=null → 3→2→null
  回溯：head=1, head.next.next=2.next=head=1 → 3→2→1
       head.next=null → 3→2→1→null
```
关键：`head.next.next = head` 让后面的节点指回自己，`head.next = null` 断开原连接。

**Q：K个一组反转（LeetCode 25-Hard）怎么解？**
递归：先数K个节点，如果不够K个直接返回head。够K个则反转前K个，`head.next = reverseKGroup(第K+1个节点, K)`。

---

### T8 环形链表追问

**Q：为什么 a = c？（数学推导）**
设：
- 头到入环点距离 = a
- 入环点到相遇点距离 = b
- 相遇点到入环点距离 = c（环剩余部分）
- 环长 = b + c

相遇时慢指针走了 `a + b`，快指针走了 `a + b + n(b+c)`（多走了n圈）

快指针速度是慢指针2倍：`2(a+b) = a + b + n(b+c)` → `a + b = n(b+c)` → `a = n(b+c) - b`

当 n=1 时：`a = (b+c) - b = c`

所以从头走 a 步和从相遇点走 c 步，同时到达入环点。

**Q：环的长度怎么算？**
相遇后，慢指针继续走一圈直到再次相遇，走的步数就是环长。

---

### T10 合并K个有序链表追问

**Q：分治法和优先队列怎么选？**
| 维度 | 优先队列 | 分治法 |
|------|---------|--------|
| 时间 | O(N log K) | O(N log K) |
| 空间 | O(K) 堆 | O(log K) 递归栈 |
| 适用 | K较小 | K很大 |

分治法不需要堆，对K很大的场景更友好。

**Q：K很大时的优化思路？**
两两归并：每轮把K个链表两两合并，每轮链表数减半，log K 轮完成。

---

### T11 删除倒数第N个追问

**Q：为什么用虚拟头节点（dummy）？**
处理删除头节点的边界情况。如果删除第1个节点，`dummy` 保证 `slow` 指向倒数第N+1个，`slow.next` 就是待删除节点。

**Q：为什么 fast 先走 n+1 步而不是 n 步？**
要让 slow 指向待删除节点的前一个节点，这样才能 `slow.next = slow.next.next` 删除。fast 多走一步，slow 就少走一步，刚好停在倒数第 n+1 个位置。

---

### T12 链表排序追问

**Q：为什么用归并不用快排？**
链表不支持随机访问，快排的 partition 需要频繁交换元素，在链表上效率极低。归并只需顺序遍历，天然适合链表。

**Q：快排能不能用于链表？**
可以，但很麻烦。需要选 pivot → 分成三部分(小于/等于/大于) → 递归排序每部分 → 拼接。代码量是归并的2-3倍。

**Q：如何找链表中点？**
快慢指针，但 `fast` 从头节点的 next 开始（不是从头节点），这样当偶数长度时，`slow` 停在前半部分的最后一个节点。

---

### T15 有效括号追问

**Q：最长有效括号（LeetCode 32-Hard）怎么解？**
- DP：`dp[i]` 表示以 i 结尾的最长有效括号长度。当 `s[i]==')'` 且 `s[i-1]=='('` 时 `dp[i]=dp[i-2]+2`；当 `s[i]==')'` 且 `s[i-1]==')'` 时检查 `s[i-dp[i-1]-1]` 是否为 `(`。
- 栈：栈底始终保持最后一个未匹配的右括号位置。

**Q：括号生成（LeetCode 22）怎么解？**
回溯：已有 n 个左括号和 n 个右括号可用。左括号只要还有就可以加，右括号只能在已用左括号>已用右括号时加。

---

### T16 最小栈追问

**Q：如果不用辅助栈，怎么做？**
存 `当前值 - 当前最小值` 的差值。push时如果 `val < min`，先存 `val - oldMin`，更新 `min = val`。pop时如果栈顶 < 0，说明这个位置更新过最小值，`min = min - 栈顶值`。这样空间只用一半。

```java
class MinStack {
    Stack<Long> stack = new Stack<>();
    long min;
    public void push(int val) {
        if (stack.isEmpty()) { stack.push(0L); min = val; }
        else { stack.push((long)val - min); if (val < min) min = val; }
    }
    public void pop() {
        long diff = stack.pop();
        if (diff < 0) min = min - diff; // 恢复旧最小值
    }
    public int top() {
        long diff = stack.peek();
        return diff > 0 ? (int)(min + diff) : (int)min;
    }
    public int getMin() { return (int)min; }
}
```

---

### T17 层序遍历追问

**Q：锯齿形遍历（LeetCode 103）怎么解？**
层序遍历时加一个 flag 标记方向，偶数层从左到右，奇数层从右到左（用 `LinkedList.addFirst()`）。

**Q：DFS实现层序遍历？**
递归时传当前深度，`res.get(depth).add(val)`。需要先建好每层的列表。

---

### T19 验证BST追问

**Q：如何找BST的第K小元素（LeetCode 230）？**
中序遍历到第K个停止。迭代版更方便：
```java
public int kthSmallest(TreeNode root, int k) {
    Stack<TreeNode> stack = new Stack<>();
    while (root != null || !stack.isEmpty()) {
        while (root != null) { stack.push(root); root = root.left; }
        root = stack.pop();
        if (--k == 0) return root.val;
        root = root.right;
    }
    return -1;
}
```

**Q：如何验证平衡二叉树（LeetCode 110）？**
递归求高度，同时判断左右子树高度差是否 ≤1。如果发现不平衡返回-1。

---

### T20 最近公共祖先追问

**Q：BST的LCA怎么做（LeetCode 235）？**
利用 BST 性质：如果 `p,q` 都小于 root，去左边找；都大于 root，去右边找；否则当前 root 就是 LCA。时间 O(h)，空间 O(1)。

**Q：如果有parent指针怎么做？**
转化为两个链表找交点的问题。一个指针从 p 走到根，另一个从 q 走到根，记录路径，找第一个公共节点。

**Q：频繁查询 LCA 怎么优化？**
预处理：倍增法（Binary Lifting）。`up[node][k]` 表示 node 向上走 2^k 步的祖先。预处理 O(n log n)，每次查询 O(log n)。适合查询次数 >> 节点数的场景。

---

### T21 爬楼梯追问

**Q：如果每次可以爬1~K级怎么解？**
`dp[i] = sum(dp[i-1] ... dp[i-K])`，可以用滑动窗口优化，不需要每次重新求和。

**Q：如果有障碍物（某些台阶不能踩）怎么解？**
障碍物处 `dp[i] = 0`，其余正常递推。

**Q：最小花费爬楼梯（LeetCode 746）怎么解？**
`dp[i] = min(dp[i-1] + cost[i-1], dp[i-2] + cost[i-2])`，到达第 i 级的最小花费。

---

### T22 LIS追问

**Q：贪心+二分为什么能找到最长递增子序列长度？**
tails 数组的含义：tails[i] 表示长度为 i+1 的递增子序列的最小结尾值。遍历每个 num，用二分找到 tails 中 `>= num` 的第一个位置，替换之。最终 tails 的长度就是 LIS 长度。

注意：tails 数组本身不是 LIS，只是长度等于 LIS 长度。

**Q：如何输出具体的LIS？**
DP 解法：用 `prev[i]` 记录前驱节点索引。贪心解法：用 `indices[i]` 记录 tails[i] 在原数组中的索引，`prev[i]` 记录前驱。

**Q：俄罗斯套娃信封（LeetCode 354）怎么解？**
二维：先按宽度升序，宽度相同按高度降序。然后对高度求 LIS。宽度相同按高度降序可以避免同宽度的信封被套在一起。

---

### T23 LCS追问

**Q：如何输出具体的LCS字符串？**
DP 填完表后，从 `dp[m][n]` 逆向回溯：
- 如果 `t1[i-1]==t2[j-1]`，加入结果，`i--; j--`
- 否则向大的方向走：`dp[i-1][j] > dp[i][j-1]` 则 `i--`，否则 `j--`
最后把结果反转。

**Q：LCS和编辑距离的关系？**
编辑距离 = `m + n - 2 × LCS`（Levenshtein距离），只在插入/删除操作下成立。标准编辑距离（含替换）是另一个 DP 问题。

---

### T24 零钱兑换追问

**Q：零钱兑换II（LeetCode 518-求组合数）和本题的区别？**
- 最少硬币数（322）：`dp[i] = min(dp[i], dp[i-coin]+1)`，coin 循环在内层或外层都可以
- 组合数（518）：`dp[i] += dp[i-coin]`，**coin 循环必须在外层**，避免重复计数

```java
// 求组合数（518）- coin外层是关键
public int change(int amount, int[] coins) {
    int[] dp = new int[amount + 1];
    dp[0] = 1;
    for (int coin : coins)           // coin外层
        for (int i = coin; i <= amount; i++)
            dp[i] += dp[i - coin];
    return dp[amount];
}
```

**Q：01背包和完全背包的区别？**
01背包每个物品只能用一次，内层循环倒序（`i = amount → coin`）；完全背包可以用多次，内层循环正序。

---

### T25 股票买卖追问

**Q：如果最多交易K次（LeetCode 188-Hard）怎么解？**
`dp[i][k][0/1]`：第i天，已完成k次交易，0=不持有股票，1=持有股票。
- `dp[i][k][0] = max(dp[i-1][k][0], dp[i-1][k][1] + price)`
- `dp[i][k][1] = max(dp[i-1][k][1], dp[i-1][k-1][0] - price)`

**Q：含冷冻期（LeetCode 309）和含手续费（LeetCode 714）怎么处理？**
- 冷冻期：卖出后隔一天才能买入，状态变为持有/不持有（冷冻）/不持有（可买）
- 手续费：卖出时扣手续费：`dp[i][0] = max(dp[i-1][0], dp[i-1][1] + price - fee)`

---

### T26 全排列追问

**Q：全排列II（LeetCode 47-有重复元素）怎么去重？**
排序后，`nums[i] == nums[i-1]` 且 `!used[i-1]` 时跳过。核心逻辑：相同元素必须按顺序使用，不能跳着用，否则产生重复排列。

**Q：下一个排列（LeetCode 31）怎么解？**
1. 从右往左找第一个 `nums[i] < nums[i+1]` 的位置 i
2. 从右往左找第一个 `nums[j] > nums[i]` 的位置 j
3. 交换 `nums[i]` 和 `nums[j]`
4. 反转 i+1 到末尾

---

### T27 子集追问

**Q：子集II（LeetCode 90-有重复元素）怎么去重？**
排序后，每层回溯中，如果 `nums[i] == nums[i-1]` 则跳过。注意是 is 层跳过还是同路径跳过。

**Q：组合总和（LeetCode 39）和组合总和II（LeetCode 40）的区别？**
- 39（可重复使用）：backtrack 时 start 不+1，允许重复选同一个数
- 40（每个数只能用一次且有重复元素）：backtrack 时 start+1，且同一层跳过相同元素

**Q：子集的其他解法？**
- 迭代法：`res = [[]]; for each num: res += [s+[num] for s in res]`
- 位运算法：n 个元素，2^n 个子集，用 0~2^n-1 的二进制位表示选或不选

---

### T28 岛屿数量追问

**Q：最大岛屿面积（LeetCode 695）怎么解？**
DFS 返回遍历到的节点数。`area = 1 + dfs(i+1,j) + dfs(i-1,j) + dfs(i,j+1) + dfs(i,j-1)`，记录最大面积。

**Q：岛屿周长（LeetCode 463）怎么解？**
每个陆地贡献4条边，但每对相邻陆地减去2条边。遍历时检查四条边：如果是边界或者邻接水，周长+1。

**Q：DFS和BFS在网格搜索中的选择？**
- DFS：递归简洁，可能栈溢出（网格很大的情况）
- BFS：用队列，不会栈溢出，可以计算最短距离
- 一般网格题目（≤300×300）DFS足够

---

### T29 课程表追问

**Q：如何输出拓扑排序结果？**
用一个列表存储出队顺序即可。`order.add(course)` 在出队时记录。

**Q：DFS怎么做？三色标记法**
- 白色(0)：未访问
- 灰色(1)：正在访问中（在当前DFS路径上）
- 黑色(2)：已访问完毕
如果在DFS中遇到灰色节点，说明有环。

```java
public boolean canFinish(int n, int[][] pre) {
    List<List<Integer>> g = new ArrayList<>();
    for (int i = 0; i < n; i++) g.add(new ArrayList<>());
    for (int[] p : pre) g.get(p[1]).add(p[0]);
    int[] visited = new int[n];
    for (int i = 0; i < n; i++) if (dfs(i, g, visited)) return false;
    return true;
}
boolean dfs(int i, List<List<Integer>> g, int[] visited) {
    if (visited[i] == 1) return true;  // 有环
    if (visited[i] == 2) return false; // 已访问
    visited[i] = 1;
    for (int next : g.get(i)) if (dfs(next, g, visited)) return true;
    visited[i] = 2;
    return false;
}
```

---

### T30 二分查找追问

**Q：查找第一个/最后一个位置（LeetCode 34）怎么解？**
- 第一个位置：找到 target 后，`right = mid` 继续收缩右边界
- 最后一个位置：找到 target 后，`left = mid + 1` 继续收缩左边界
最终 `left` 停在第一个位置，`right` 停在最后一个位置。

**Q：旋转排序数组搜索（LeetCode 33）怎么解？**
先判断 mid 落在左半有序还是右半有序：
- 如果 `nums[left] <= nums[mid]`：左半有序，判断 target 是否在 `[nums[left], nums[mid])` 区间
- 否则右半有序，同理判断
每次排除一半。

**Q：mid 为什么要用 `left + (right - left) / 2` 而不是 `(left + right) / 2`？**
防止整数溢出。`left + right` 可能超过 int 最大值（2^31-1 = 2147483647），而 `right - left` 不会溢出。

---

### T31 LRU追问

**Q：LFU（Least Frequently Used-最不常用）怎么实现？**
需要两个 HashMap：
- `keyToVal`: key → Node(key, value, freq)
- `freqToKeys`: freq → LinkedHashSet（维护相同频率的key的先后顺序）
访问时 `freq+1`，淘汰时找 `minFreq` 对应的最老 key。

**Q：LinkedHashMap 如何实现LRU？**
```java
LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>(cap, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
        return size() > cap;
    }
};
```
构造函数第三个参数 `accessOrder=true` 表示按访问顺序排序，`removeEldestEntry` 在 put 后自动调用。

**Q：LRU和LFU各适合什么场景？**
- LRU：数据访问有良好的时间局部性（最近访问过很可能再次访问），如浏览器缓存
- LFU：数据访问频率差异大（热门内容总是被访问），如热搜榜
- 实际生产更多用 LRU 或 LRU 变体（如 Redis allkeys-lru），实现简单

---

## 附：复杂度速查表

| 算法 | 时间 | 空间 | 关键点 |
|------|------|------|--------|
| 二分查找 | O(log n) | O(1) | 有序数组 |
| 快排 | O(n log n) | O(log n) | 平均最快，不稳定 |
| 归并排序 | O(n log n) | O(n) | 稳定，适合链表 |
| 堆排序 | O(n log n) | O(1) | 原地排序 |
| DFS | O(V+E) | O(V) | 递归/栈 |
| BFS | O(V+E) | O(V) | 队列，最短路 |
| 回溯 | O(2^n) | O(n) | 子集/排列 |
| DP | O(n²)→O(n) | O(n)→O(1) | 滚动数组优化 |
| 滑动窗口 | O(n) | O(K) | 子串问题 |

---

文档生成时间：2026-07-11
