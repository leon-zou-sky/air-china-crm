# 高频算法题 - 中大厂面试

> 按类型分类，每题含详细解答和追问

---

## 一、数组/字符串

### 1. 两数之和（Easy）

**题目**：给定一个整数数组 nums 和一个目标值 target，找出数组中和为目标值的两个数。

```
输入：nums = [2, 7, 11, 15], target = 9
输出：[0, 1]
解释：nums[0] + nums[1] = 2 + 7 = 9
```

**解题思路**：用HashMap存储已遍历的数，查找 complement = target - num

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

**复杂度**：时间 O(n)，空间 O(n)

**追问**：
- 如果数组有序呢？→ 双指针，时间O(n)，空间O(1)
- 如果要找三数之和呢？→ 排序 + 双指针，时间O(n²)
- 如果要找四数之和呢？→ 排序 + 双指针，时间O(n³)

---

### 2. 三数之和（Medium）

**题目**：找出所有和为0的三元组，不能重复。

```
输入：nums = [-1, 0, 1, 2, -1, -4]
输出：[[-1, -1, 2], [-1, 0, 1]]
```

**解题思路**：排序 + 固定一个数 + 双指针

```java
public List<List<Integer>> threeSum(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    Arrays.sort(nums);
    
    for (int i = 0; i < nums.length - 2; i++) {
        // 跳过重复
        if (i > 0 && nums[i] == nums[i - 1]) continue;
        
        int left = i + 1, right = nums.length - 1;
        while (left < right) {
            int sum = nums[i] + nums[left] + nums[right];
            if (sum == 0) {
                result.add(Arrays.asList(nums[i], nums[left], nums[right]));
                // 跳过重复
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

**复杂度**：时间 O(n²)，空间 O(1)

**追问**：
- 为什么先排序？→ 去重 + 双指针前提
- 如何去重？→ 跳过相同元素
- 最接近的三数之和？→ 记录最小差值

---

### 3. 最长无重复子串（Medium）

**题目**：找出不含重复字符的最长子串长度。

```
输入：s = "abcabcbb"
输出：3
解释：最长子串是 "abc"，长度为3
```

**解题思路**：滑动窗口 + HashSet

```java
public int lengthOfLongestSubstring(String s) {
    Set<Character> set = new HashSet<>();
    int left = 0, maxLen = 0;
    
    for (int right = 0; right < s.length(); right++) {
        // 有重复字符，收缩左边界
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

**复杂度**：时间 O(n)，空间 O(字符集大小)

**追问**：
- 如果用HashMap怎么做？→ 存字符和索引，直接跳到重复位置
- 最长重复字符替换？→ 滑动窗口，窗口内最多k个不同字符
- 最小覆盖子串？→ 滑动窗口，找到包含所有字符的最小窗口

---

### 4. 接雨水（Hard）

**题目**：给定n个非负整数表示柱子高度，计算能接多少雨水。

```
输入：height = [0,1,0,2,1,0,1,3,2,1,2,1]
输出：6
```

**解题思路**：双指针，记录左右最大高度

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

**复杂度**：时间 O(n)，空间 O(1)

**追问**：
- 动态规划怎么做？→ 预计算每个位置的左右最大值
- 栈怎么做？→ 单调栈，找到凹槽
- 柱状图最大矩形？→ 单调栈

---

## 二、链表

### 5. 反转链表（Easy）

**题目**：反转一个单链表。

```
输入：1 → 2 → 3 → 4 → 5 → null
输出：5 → 4 → 3 → 2 → 1 → null
```

**解题思路**：迭代，三个指针

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

**递归解法**：

```java
public ListNode reverseList(ListNode head) {
    if (head == null || head.next == null) return head;
    
    ListNode newHead = reverseList(head.next);
    head.next.next = head;
    head.next = null;
    return newHead;
}
```

**复杂度**：时间 O(n)，空间 O(1) 或 O(n) 递归

**追问**：
- 反转链表前N个节点？→ 记录第N+1个节点
- 反转链表的一部分？→ 找到前后节点，反转中间部分
- K个一组反转？→ 分组处理

---

### 6. 环形链表（Easy）

**题目**：判断链表是否有环，返回入环节点。

```
输入：1 → 2 → 3 → 4 → 2（回到2）
输出：节点2
```

**解题思路**：快慢指针，找相遇点

```java
public ListNode detectCycle(ListNode head) {
    ListNode slow = head, fast = head;
    
    // 找相遇点
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) break;
    }
    
    // 无环
    if (fast == null || fast.next == null) return null;
    
    // 找入环点
    slow = head;
    while (slow != fast) {
        slow = slow.next;
        fast = fast.next;
    }
    return slow;
}
```

**复杂度**：时间 O(n)，空间 O(1)

**追问**：
- 为什么快慢指针能找环？→ 快指针每次比慢指针多走1步，一定会相遇
- 环的长度怎么算？→ 相遇后继续走，再次相遇就是环长度
- 入环点怎么算？→ 数学推导：a = c（a是头到入环点，c是相遇到入环点）

---

### 7. 合并K个有序链表（Hard）

**题目**：合并K个升序链表为一个升序链表。

```
输入：[[1,4,5], [1,3,4], [2,6]]
输出：[1,1,2,3,4,4,5,6]
```

**解题思路**：优先队列（最小堆）

```java
public ListNode mergeKLists(ListNode[] lists) {
    PriorityQueue<ListNode> pq = new PriorityQueue<>((a, b) -> a.val - b.val);
    
    // 把所有链表头节点加入堆
    for (ListNode node : lists) {
        if (node != null) pq.offer(node);
    }
    
    ListNode dummy = new ListNode(0);
    ListNode curr = dummy;
    
    while (!pq.isEmpty()) {
        ListNode min = pq.poll();
        curr.next = min;
        curr = curr.next;
        
        if (min.next != null) {
            pq.offer(min.next);
        }
    }
    
    return dummy.next;
}
```

**复杂度**：时间 O(N log K)，空间 O(K)（N是总节点数，K是链表数）

**追问**：
- 分治法怎么做？→ 两两合并，时间O(N log K)
- 如果链表数量很大？→ 分治法更好
- 如果链表很长？→ 优先队列更好

---

## 三、栈/队列

### 8. 有效括号（Easy）

**题目**：判断括号字符串是否有效。

```
输入：s = "()[]{}"
输出：true

输入：s = "([)]"
输出：false
```

**解题思路**：栈，遇到左括号入栈，遇到右括号出栈匹配

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

**复杂度**：时间 O(n)，空间 O(n)

**追问**：
- 最长有效括号？→ 动态规划或栈
- 括号生成？→ 回溯
- 最小添加使括号有效？→ 计数

---

### 9. 最小栈（Easy）

**题目**：设计一个支持 push、pop、top 和 getMin 的栈。

**解题思路**：辅助栈存最小值

```java
class MinStack {
    Stack<Integer> stack;
    Stack<Integer> minStack;
    
    public MinStack() {
        stack = new Stack<>();
        minStack = new Stack<>();
    }
    
    public void push(int val) {
        stack.push(val);
        if (minStack.isEmpty() || val <= minStack.peek()) {
            minStack.push(val);
        }
    }
    
    public void pop() {
        if (stack.pop().equals(minStack.peek())) {
            minStack.pop();
        }
    }
    
    public int top() {
        return stack.peek();
    }
    
    public int getMin() {
        return minStack.peek();
    }
}
```

**追问**：
- 如果用一个栈怎么做？→ 存差值（val - min）
- 如何O(1)时间复杂度？→ 差值法

---

## 四、树

### 10. 二叉树的层序遍历（Medium）

**题目**：逐层从左到右遍历二叉树。

```
输入：    3
        / \
       9  20
         /  \
        15   7

输出：[[3], [9, 20], [15, 7]]
```

**解题思路**：BFS，队列

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

**复杂度**：时间 O(n)，空间 O(n)

**追问**：
- DFS怎么做？→ 递归，传入层级参数
- 锯齿形遍历？→ 奇偶层交替翻转
- 最大层和？→ 记录每层和，取最大

---

### 11. 二叉树的最大深度（Easy）

**题目**：求二叉树的最大深度。

```java
// DFS递归
public int maxDepth(TreeNode root) {
    if (root == null) return 0;
    return Math.max(maxDepth(root.left), maxDepth(root.right)) + 1;
}

// BFS
public int maxDepth(TreeNode root) {
    if (root == null) return 0;
    
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    int depth = 0;
    
    while (!queue.isEmpty()) {
        int size = queue.size();
        depth++;
        
        for (int i = 0; i < size; i++) {
            TreeNode node = queue.poll();
            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
    }
    return depth;
}
```

**追问**：
- 最小深度？→ BFS找到第一个叶子节点
- 平衡二叉树？→ 递归判断左右子树高度差
- 直径？→ 左深度 + 右深度

---

### 12. 验证二叉搜索树（Medium）

**题目**：判断是否是有效的BST（左子树 < 根 < 右子树）。

```java
// 中序遍历，检查是否递增
public boolean isValidBST(TreeNode root) {
    Stack<TreeNode> stack = new Stack<>();
    TreeNode prev = null;
    
    while (root != null || !stack.isEmpty()) {
        while (root != null) {
            stack.push(root);
            root = root.left;
        }
        
        root = stack.pop();
        if (prev != null && root.val <= prev.val) return false;
        prev = root;
        root = root.right;
    }
    return true;
}

// 递归，传递范围
public boolean isValidBST(TreeNode root) {
    return helper(root, Long.MIN_VALUE, Long.MAX_VALUE);
}

private boolean helper(TreeNode root, long min, long max) {
    if (root == null) return true;
    if (root.val <= min || root.val >= max) return false;
    return helper(root.left, min, root.val) && helper(root.right, root.val, max);
}
```

**追问**：
- BST的中序遍历是什么？→ 有序序列
- 如何找BST的第K小？→ 中序遍历第K个
- 如何验证平衡二叉树？→ 递归求高度，判断差值

---

### 13. 最近公共祖先（Medium）

**题目**：找到两个节点的最近公共祖先（LCA）。

```
输入：root = [3,5,1,6,2,0,8,null,null,7,4], p = 5, q = 1
输出：3
```

**解题思路**：递归，左右子树分别查找

```java
public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
    if (root == null || root == p || root == q) return root;
    
    TreeNode left = lowestCommonAncestor(root.left, p, q);
    TreeNode right = lowestCommonAncestor(root.right, p, q);
    
    if (left != null && right != null) return root;
    return left != null ? left : right;
}
```

**复杂度**：时间 O(n)，空间 O(n)

**追问**：
- BST的LCA？→ 利用BST性质，比较大小
- 如果有parent指针？→ 转化为链表相交问题
- 如果节点数很多？→ 预处理 + 倍增法

---

## 五、动态规划

### 14. 爬楼梯（Easy）

**题目**：每次爬1或2级，求到第n级有多少种方式。

```
输入：n = 3
输出：3
解释：1+1+1, 1+2, 2+1
```

**解题思路**：斐波那契数列

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

**复杂度**：时间 O(n)，空间 O(1)

**追问**：
- 如果每次可以爬1~k级？→ dp[i] = sum(dp[i-1]...dp[i-k])
- 如果有障碍物？→ 障碍物处dp=0
- 最小花费？→ dp[i] = min(dp[i-1]+cost[i-1], dp[i-2]+cost[i-2])

---

### 15. 最长递增子序列（Medium）

**题目**：找出数组中最长的严格递增子序列长度。

```
输入：nums = [10,9,2,5,3,7,101,18]
输出：4
解释：最长递增子序列是 [2,3,7,101]
```

**解题思路**：动态规划 O(n²) 或 贪心+二分 O(n log n)

```java
// 动态规划 O(n²)
public int lengthOfLIS(int[] nums) {
    int[] dp = new int[nums.length];
    Arrays.fill(dp, 1);
    int maxLen = 1;
    
    for (int i = 1; i < nums.length; i++) {
        for (int j = 0; j < i; j++) {
            if (nums[j] < nums[i]) {
                dp[i] = Math.max(dp[i], dp[j] + 1);
            }
        }
        maxLen = Math.max(maxLen, dp[i]);
    }
    return maxLen;
}

// 贪心+二分 O(n log n)
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

**追问**：
- 如何输出具体的子序列？→ 记录前驱节点
- 最长非递减子序列？→ 条件改为 <=
- 俄罗斯套娃信封？→ 二维排序 + LIS

---

### 16. 最长公共子序列（Medium）

**题目**：找出两个字符串的最长公共子序列长度。

```
输入：text1 = "abcde", text2 = "ace"
输出：3
解释：最长公共子序列是 "ace"
```

**解题思路**：二维DP

```java
public int longestCommonSubsequence(String text1, String text2) {
    int m = text1.length(), n = text2.length();
    int[][] dp = new int[m + 1][n + 1];
    
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                dp[i][j] = dp[i - 1][j - 1] + 1;
            } else {
                dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }
    }
    return dp[m][n];
}
```

**复杂度**：时间 O(mn)，空间 O(mn) 可优化到 O(n)

**追问**：
- 如何输出具体的子序列？→ 回溯dp数组
- 最长公共子串？→ 连续的公共子串
- 编辑距离？→ 类似思路

---

### 17. 零钱兑换（Medium）

**题目**：用最少的硬币数凑出目标金额。

```
输入：coins = [1, 2, 5], amount = 11
输出：3
解释：5 + 5 + 1 = 11
```

**解题思路**：完全背包

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

**复杂度**：时间 O(amount * coins.length)，空间 O(amount)

**追问**：
- 如果要求组合数？→ dp[i] += dp[i - coin]
- 完全背包和01背包区别？→ 01背包每个物品只能用一次
- 如何优化？→ BFS

---

### 18. 股票买卖问题（Medium/Hard）

**题目**：买卖股票求最大利润（多种变体）。

```
变体1：只能买卖一次
输入：[7,1,5,3,6,4]
输出：5（1买6卖）

变体2：可以买卖多次
输出：7（1买5卖 + 3买6卖）

变体3：最多买卖两次
输出：？？？
```

**解题思路**：状态机DP

```java
// 变体1：只能买卖一次
public int maxProfit(int[] prices) {
    int minPrice = Integer.MAX_VALUE;
    int maxProfit = 0;
    
    for (int price : prices) {
        minPrice = Math.min(minPrice, price);
        maxProfit = Math.max(maxProfit, price - minPrice);
    }
    return maxProfit;
}

// 变体2：可以买卖多次
public int maxProfit(int[] prices) {
    int profit = 0;
    for (int i = 1; i < prices.length; i++) {
        if (prices[i] > prices[i - 1]) {
            profit += prices[i] - prices[i - 1];
        }
    }
    return profit;
}
```

**追问**：
- 含冷冻期？→ 状态：持有、不持有（冷冻）、不持有（可买）
- 含手续费？→ 卖出时扣手续费
- 最多K次交易？→ dp[i][k][0/1]

---

## 六、双指针/滑动窗口

### 19. 盛最多水的容器（Medium）

**题目**：找两条线，使得它们与x轴共同的容器可以容纳最多的水。

```
输入：height = [1,8,6,2,5,4,8,3,7]
输出：49
```

**解题思路**：双指针，移动较短的那条线

```java
public int maxArea(int[] height) {
    int left = 0, right = height.length - 1;
    int maxArea = 0;
    
    while (left < right) {
        int area = Math.min(height[left], height[right]) * (right - left);
        maxArea = Math.max(maxArea, area);
        
        if (height[left] < height[right]) {
            left++;
        } else {
            right--;
        }
    }
    return maxArea;
}
```

**复杂度**：时间 O(n)，空间 O(1)

**追问**：
- 为什么移动较短的？→ 移动较长的不可能得到更大面积
- 接雨水？→ 类似思路
- 柱状图最大矩形？→ 单调栈

---

### 20. 最小覆盖子串（Hard）

**题目**：找出包含t所有字符的最小子串。

```
输入：s = "ADOBECODEBANC", t = "ABC"
输出："BANC"
```

**解题思路**：滑动窗口

```java
public String minWindow(String s, String t) {
    Map<Character, Integer> need = new HashMap<>();
    Map<Character, Integer> window = new HashMap<>();
    
    for (char c : t.toCharArray()) {
        need.put(c, need.getOrDefault(c, 0) + 1);
    }
    
    int left = 0, valid = 0;
    int start = 0, minLen = Integer.MAX_VALUE;
    
    for (int right = 0; right < s.length(); right++) {
        char c = s.charAt(right);
        
        if (need.containsKey(c)) {
            window.put(c, window.getOrDefault(c, 0) + 1);
            if (window.get(c).equals(need.get(c))) {
                valid++;
            }
        }
        
        // 收缩左边界
        while (valid == need.size()) {
            if (right - left + 1 < minLen) {
                start = left;
                minLen = right - left + 1;
            }
            
            char d = s.charAt(left);
            if (need.containsKey(d)) {
                if (window.get(d).equals(need.get(d))) {
                    valid--;
                }
                window.put(d, window.get(d) - 1);
            }
            left++;
        }
    }
    
    return minLen == Integer.MAX_VALUE ? "" : s.substring(start, start + minLen);
}
```

**复杂度**：时间 O(n)，空间 O(字符集大小)

**追问**：
- 如何判断窗口内是否包含所有字符？→ valid计数
- 最长无重复子串？→ 窗口内无重复
- 找所有字母异位词？→ 固定窗口大小

---

## 七、排序/搜索

### 21. 二分查找（Easy）

**题目**：在有序数组中查找目标值。

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

**追问**：
- 查找第一个/最后一个位置？→ 收缩边界时保留mid
- 旋转数组查找？→ 判断哪半边有序
- 山脉数组查找？→ 先找峰值，再二分

---

### 22. 快速排序（Medium）

```java
public void quickSort(int[] arr, int left, int right) {
    if (left >= right) return;
    
    int pivotIndex = partition(arr, left, right);
    quickSort(arr, left, pivotIndex - 1);
    quickSort(arr, pivotIndex + 1, right);
}

private int partition(int[] arr, int left, int right) {
    int pivot = arr[right];
    int i = left;
    
    for (int j = left; j < right; j++) {
        if (arr[j] < pivot) {
            swap(arr, i, j);
            i++;
        }
    }
    
    swap(arr, i, right);
    return i;
}
```

**追问**：
- 时间复杂度？→ 平均O(n log n)，最差O(n²)
- 为什么最差是O(n²)？→ 每次选到最大/最小值
- 如何优化？→ 三数取中、随机化
- 归并排序的区别？→ 归并稳定，快排不稳定

---

### 23. 堆排序（Medium）

```java
public void heapSort(int[] arr) {
    int n = arr.length;
    
    // 建堆
    for (int i = n / 2 - 1; i >= 0; i--) {
        heapify(arr, n, i);
    }
    
    // 排序
    for (int i = n - 1; i > 0; i--) {
        swap(arr, 0, i);
        heapify(arr, i, 0);
    }
}

private void heapify(int[] arr, int n, int i) {
    int largest = i;
    int left = 2 * i + 1;
    int right = 2 * i + 2;
    
    if (left < n && arr[left] > arr[largest]) largest = left;
    if (right < n && arr[right] > arr[largest]) largest = right;
    
    if (largest != i) {
        swap(arr, i, largest);
        heapify(arr, n, largest);
    }
}
```

**追问**：
- 建堆的时间复杂度？→ O(n)
- 堆排序的时间复杂度？→ O(n log n)
- TopK问题？→ 小顶堆找前K大
- 优先队列的实现？→ 堆

---

## 八、回溯

### 24. 全排列（Medium）

**题目**：返回所有可能的排列。

```
输入：[1,2,3]
输出：[[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]
```

```java
public List<List<Integer>> permute(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    backtrack(nums, new ArrayList<>(), result);
    return result;
}

private void backtrack(int[] nums, List<Integer> path, List<List<Integer>> result) {
    if (path.size() == nums.length) {
        result.add(new ArrayList<>(path));
        return;
    }
    
    for (int num : nums) {
        if (path.contains(num)) continue;
        path.add(num);
        backtrack(nums, path, result);
        path.remove(path.size() - 1);
    }
}
```

**追问**：
- 如何去重？→ 排序 + 跳过相同元素
- 下一个排列？→ 从右找第一个递减，交换，反转
- 第K个排列？→ 数学方法

---

### 25. 子集（Medium）

**题目**：返回所有可能的子集。

```
输入：[1,2,3]
输出：[[], [1], [2], [3], [1,2], [1,3], [2,3], [1,2,3]]
```

```java
public List<List<Integer>> subsets(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    backtrack(nums, 0, new ArrayList<>(), result);
    return result;
}

private void backtrack(int[] nums, int start, List<Integer> path, List<List<Integer>> result) {
    result.add(new ArrayList<>(path));
    
    for (int i = start; i < nums.length; i++) {
        path.add(nums[i]);
        backtrack(nums, i + 1, path, result);
        path.remove(path.size() - 1);
    }
}
```

**追问**：
- 如何去重？→ 排序 + 跳过相同元素
- 子集II（有重复元素）？→ 排序 + 跳过
- 组合总和？→ 回溯 + 剪枝

---

## 九、图

### 26. 岛屿数量（Medium）

**题目**：计算网格中岛屿数量（1是陆地，0是水）。

```
输入：
[
  ["1","1","0","0","0"],
  ["1","1","0","0","0"],
  ["0","0","1","0","0"],
  ["0","0","0","1","1"]
]
输出：3
```

**解题思路**：DFS或BFS

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
    
    grid[i][j] = '0'; // 标记访问
    dfs(grid, i + 1, j);
    dfs(grid, i - 1, j);
    dfs(grid, i, j + 1);
    dfs(grid, i, j - 1);
}
```

**追问**：
- 最大岛屿面积？→ DFS返回面积
- 岛屿周长？→ 计算边界
- 被围绕的区域？→ 从边界DFS

---

### 27. 课程表（Medium）

**题目**：判断是否能完成所有课程（有向图判环）。

```
输入：numCourses = 2, prerequisites = [[1,0]]
输出：true（先修0，再修1）

输入：numCourses = 2, prerequisites = [[1,0],[0,1]]
输出：false（循环依赖）
```

**解题思路**：拓扑排序（BFS）

```java
public boolean canFinish(int numCourses, int[][] prerequisites) {
    List<List<Integer>> graph = new ArrayList<>();
    int[] inDegree = new int[numCourses];
    
    for (int i = 0; i < numCourses; i++) {
        graph.add(new ArrayList<>());
    }
    
    for (int[] pre : prerequisites) {
        graph.get(pre[1]).add(pre[0]);
        inDegree[pre[0]]++;
    }
    
    Queue<Integer> queue = new LinkedList<>();
    for (int i = 0; i < numCourses; i++) {
        if (inDegree[i] == 0) queue.offer(i);
    }
    
    int count = 0;
    while (!queue.isEmpty()) {
        int course = queue.poll();
        count++;
        
        for (int next : graph.get(course)) {
            inDegree[next]--;
            if (inDegree[next] == 0) queue.offer(next);
        }
    }
    
    return count == numCourses;
}
```

**追问**：
- 如何输出课程顺序？→ 拓扑排序结果
- DFS怎么做？→ 三色标记法判环
- 并查集怎么做？→ 合并集合

---

## 十、高频面试手撕代码

### 28. LRU缓存（Medium）

**题目**：设计LRU缓存，支持get和put操作O(1)。

```java
class LRUCache {
    int capacity;
    Map<Integer, Node> map;
    Node head, tail;
    
    public LRUCache(int capacity) {
        this.capacity = capacity;
        map = new HashMap<>();
        head = new Node(0, 0);
        tail = new Node(0, 0);
        head.next = tail;
        tail.prev = head;
    }
    
    public int get(int key) {
        if (!map.containsKey(key)) return -1;
        Node node = map.get(key);
        remove(node);
        addToHead(node);
        return node.value;
    }
    
    public void put(int key, int value) {
        if (map.containsKey(key)) {
            Node node = map.get(key);
            node.value = value;
            remove(node);
            addToHead(node);
        } else {
            if (map.size() == capacity) {
                Node last = tail.prev;
                remove(last);
                map.remove(last.key);
            }
            Node node = new Node(key, value);
            map.put(key, node);
            addToHead(node);
        }
    }
    
    private void remove(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
    
    private void addToHead(Node node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }
    
    class Node {
        int key, value;
        Node prev, next;
        Node(int k, int v) { key = k; value = v; }
    }
}
```

**追问**：
- LinkedHashMap怎么实现？→ 继承HashMap + 双向链表
- LFU怎么实现？→ 频率 + 频率链表
- 线程安全怎么做？→ ConcurrentHashMap + 锁

---

### 29. Trie前缀树（Medium）

**题目**：实现Trie，支持insert、search、startsWith。

```java
class Trie {
    private Trie[] children;
    private boolean isEnd;
    
    public Trie() {
        children = new Trie[26];
        isEnd = false;
    }
    
    public void insert(String word) {
        Trie node = this;
        for (char c : word.toCharArray()) {
            int index = c - 'a';
            if (node.children[index] == null) {
                node.children[index] = new Trie();
            }
            node = node.children[index];
        }
        node.isEnd = true;
    }
    
    public boolean search(String word) {
        Trie node = searchPrefix(word);
        return node != null && node.isEnd;
    }
    
    public boolean startsWith(String prefix) {
        return searchPrefix(prefix) != null;
    }
    
    private Trie searchPrefix(String prefix) {
        Trie node = this;
        for (char c : prefix.toCharArray()) {
            int index = c - 'a';
            if (node.children[index] == null) return null;
            node = node.children[index];
        }
        return node;
    }
}
```

**追问**：
- 时间复杂度？→ O(L)，L是字符串长度
- 空间复杂度？→ O(N*L*26)
- 应用场景？→ 自动补全、拼写检查、IP路由

---

### 30. 设计哈希表（Medium）

**题目**：不使用内置哈希表，设计一个哈希表。

```java
class MyHashMap {
    private static final int SIZE = 10007;
    private List<int[]>[] buckets;
    
    public MyHashMap() {
        buckets = new List[SIZE];
        for (int i = 0; i < SIZE; i++) {
            buckets[i] = new ArrayList<>();
        }
    }
    
    public void put(int key, int value) {
        int index = hash(key);
        List<int[]> bucket = buckets[index];
        
        for (int[] pair : bucket) {
            if (pair[0] == key) {
                pair[1] = value;
                return;
            }
        }
        bucket.add(new int[]{key, value});
    }
    
    public int get(int key) {
        int index = hash(key);
        List<int[]> bucket = buckets[index];
        
        for (int[] pair : bucket) {
            if (pair[0] == key) return pair[1];
        }
        return -1;
    }
    
    public void remove(int key) {
        int index = hash(key);
        List<int[]> bucket = buckets[index];
        
        for (int i = 0; i < bucket.size(); i++) {
            if (bucket.get(i)[0] == key) {
                bucket.remove(i);
                return;
            }
        }
    }
    
    private int hash(int key) {
        return key % SIZE;
    }
}
```

**追问**：
- 如何处理哈希冲突？→ 链地址法、开放寻址法
- 如何扩容？→ 负载因子 > 0.75时扩容
- 为什么选质数作为SIZE？→ 减少冲突

---

## 算法题总结

### 按难度分类

| 难度 | 题数 | 题目 |
|------|------|------|
| Easy | 8 | 两数之和、反转链表、环形链表、有效括号、最小栈、爬楼梯、二分查找、最大深度 |
| Medium | 17 | 三数之和、最长无重复子串、合并K个有序链表、层序遍历、验证BST、LCA、LIS、LCS、零钱兑换、股票、盛水、快速排序、堆排序、全排列、子集、岛屿、课程表 |
| Hard | 5 | 接雨水、合并K个有序链表、最小覆盖子串、LRU缓存、设计哈希表 |

### 按类型分类

| 类型 | 题数 | 核心技巧 |
|------|------|---------|
| 数组/字符串 | 4 | 双指针、滑动窗口、HashMap |
| 链表 | 3 | 快慢指针、虚拟头节点、递归 |
| 栈/队列 | 2 | 单调栈、辅助栈 |
| 树 | 4 | 递归、BFS、中序遍历 |
| 动态规划 | 5 | 状态定义、转移方程、滚动数组 |
| 双指针 | 2 | 左右指针、快慢指针 |
| 排序/搜索 | 3 | 二分、快排、堆排 |
| 回溯 | 2 | 路径选择、剪枝 |
| 图 | 2 | DFS、BFS、拓扑排序 |
| 设计 | 3 | LRU、Trie、HashMap |

### 面试必备

1. **手写代码**：能流畅写出代码
2. **复杂度分析**：时间O(?)，空间O(?)
3. **多种解法**：暴力 → 优化 → 最优
4. **追问应对**：变体、优化、应用

---

文档生成时间：2026-07-04
