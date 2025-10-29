package io.leavesfly.jimi.tool.task;

/**
 * Task 工具使用示例
 * 
 * Task 工具是 Jimi 的核心特性，允许主 Agent 委托子 Agent 处理特定任务。
 * 这个示例展示了如何在实际场景中使用 Task 工具。
 * 
 * @author 山泽
 */
public class TaskUsageExamples {
    
    /**
     * 示例 1: 修复编译错误
     * 
     * 场景：
     * 主 Agent 检测到代码编译失败，但不想让详细的调试过程污染主上下文。
     * 
     * 解决方案：
     * 使用 Task 工具委托给 code_fixer 子 Agent，只接收最终的修复结果。
     */
    public static final String EXAMPLE_1_FIX_COMPILATION_ERROR = """
            {
              "description": "Fix compilation error",
              "subagent_name": "code_fixer",
              "prompt": "\\
            I have a compilation error in src/main/java/com/example/UserService.java:
            
            Error: cannot find symbol
              symbol:   variable userRepository
              location: class UserService
            
            Please:
            1. Read the file to understand the context
            2. Identify what's missing (likely a field declaration or import)
            3. Fix the issue using StrReplaceFile
            4. Verify the fix by reading the file again
            5. Return a summary of what you fixed
            "
            }
            """;
    
    /**
     * 示例 2: 搜索技术信息
     * 
     * 场景：
     * 主 Agent 需要了解某个库的最新用法，但不想看到大量的搜索结果。
     * 
     * 解决方案：
     * 使用 Task 工具委托给 info_seeker 子 Agent，只返回精选的相关信息。
     */
    public static final String EXAMPLE_2_SEARCH_TECHNICAL_INFO = """
            {
              "description": "Search Spring Security usage",
              "subagent_name": "info_seeker",
              "prompt": "\\
            I need to implement JWT authentication in a Spring Boot 3.2 application.
            
            Please search for:
            1. How to configure JWT authentication with Spring Security 6
            2. Best practices for token generation and validation
            3. Example code for a JWT filter
            
            Return only:
            - A concise summary of the approach
            - 2-3 most relevant code examples
            - Key configuration points
            
            Do not include:
            - General Spring Security introduction
            - Unrelated authentication methods
            - Deprecated approaches
            "
            }
            """;
    
    /**
     * 示例 3: 并行多任务
     * 
     * 场景：
     * 主 Agent 需要重构 3 个独立的服务类。
     * 
     * 解决方案：
     * 在一次 LLM 响应中同时调用 3 次 Task 工具，让 3 个子 Agent 并行工作。
     * 
     * 注意：这些调用会并行执行，大幅提升效率。
     */
    public static final String EXAMPLE_3_PARALLEL_TASKS = """
            // LLM 的一次响应中包含多个工具调用
            
            // 工具调用 1
            {
              "description": "Refactor UserService",
              "subagent_name": "code_fixer",
              "prompt": "Refactor src/main/java/com/example/UserService.java to use constructor injection instead of field injection. Ensure all tests still pass."
            }
            
            // 工具调用 2
            {
              "description": "Refactor OrderService",
              "subagent_name": "code_fixer",
              "prompt": "Refactor src/main/java/com/example/OrderService.java to follow the Single Responsibility Principle. Extract order validation logic to a separate class."
            }
            
            // 工具调用 3
            {
              "description": "Refactor PaymentService",
              "subagent_name": "code_fixer",
              "prompt": "Refactor src/main/java/com/example/PaymentService.java to add proper error handling and logging. Use try-with-resources where appropriate."
            }
            """;
    
    /**
     * 示例 4: 大型代码库分析
     * 
     * 场景：
     * 主 Agent 需要分析一个包含 50 万行代码的项目。
     * 
     * 解决方案：
     * 将代码库按模块划分，每个子 Agent 分析一个模块，然后主 Agent 汇总结果。
     */
    public static final String EXAMPLE_4_ANALYZE_LARGE_CODEBASE = """
            // 步骤 1: 主 Agent 识别主要模块
            // 模块: user, order, payment, notification, analytics
            
            // 步骤 2: 并行分析每个模块
            
            // 工具调用 1: 分析用户模块
            {
              "description": "Analyze user module",
              "subagent_name": "code_fixer",
              "prompt": "\\
            Analyze the user module in src/main/java/com/example/user/*.java
            
            Focus on:
            1. Architecture pattern used
            2. Key classes and their responsibilities
            3. Database schema (from entity classes)
            4. API endpoints
            5. Potential issues or improvements
            
            Return a concise summary (max 500 words) with bullet points.
            "
            }
            
            // 工具调用 2: 分析订单模块
            {
              "description": "Analyze order module",
              "subagent_name": "code_fixer",
              "prompt": "Analyze the order module... (similar structure)"
            }
            
            // 工具调用 3-5: 分析其他模块...
            
            // 步骤 3: 主 Agent 汇总所有子 Agent 的分析结果
            // 生成整体架构图和改进建议
            """;
    
    /**
     * 示例 5: 渐进式问题解决
     * 
     * 场景：
     * 代码修复后仍然有问题，需要多次迭代。
     * 
     * 解决方案：
     * 使用子 Agent 进行迭代式修复，主 Agent 只关注最终结果。
     */
    public static final String EXAMPLE_5_ITERATIVE_FIXING = """
            {
              "description": "Fix integration test",
              "subagent_name": "code_fixer",
              "prompt": "\\
            The integration test UserServiceIntegrationTest is failing with:
            
            Expected: 200 OK
            Actual: 500 Internal Server Error
            Message: NullPointerException in UserService.createUser()
            
            Please:
            1. Read the test file to understand what it's testing
            2. Read UserService.java to find the NPE source
            3. Fix the issue
            4. Run the test again using Bash tool
            5. If still failing, iterate until it passes
            6. Return a summary of what was fixed
            
            Important: Keep iterating until the test passes. The subagent context is isolated, so detailed debugging won't clutter my main context.
            "
            }
            """;
    
    /**
     * 最佳实践
     */
    public static final String BEST_PRACTICES = """
            Task 工具使用最佳实践：
            
            ✅ DO:
            1. 提供完整的背景信息
               - 子 Agent 看不到主 Agent 的上下文
               - 必须在 prompt 中包含所有必要信息
            
            2. 明确任务范围
               - 具体说明要做什么
               - 设定清晰的成功标准
            
            3. 利用并行执行
               - 独立任务可以同时调用
               - 避免串行等待
            
            4. 合理选择子 Agent
               - code_fixer: 代码相关任务
               - info_seeker: 信息搜索任务
               - 根据任务特点选择专业化的子 Agent
            
            ❌ DON'T:
            1. 不要直接转发用户提示
               - 用户看不到子 Agent 的过程
               - 只有主 Agent 能看到子 Agent 的响应
            
            2. 不要用于简单任务
               - Task 工具有启动开销
               - 简单任务直接执行即可
            
            3. 不要为每个 TODO 项创建 Task
               - 这会让用户困惑
               - 只用于特定、狭窄的任务
            
            4. 不要依赖子 Agent 之间的协作
               - 每个子 Agent 都是独立的
               - 子 Agent 之间不能直接通信
            
            💡 高级技巧：
            1. 响应长度检查
               - 系统会自动检测过短的响应
               - 自动发送 CONTINUE_PROMPT 要求详细说明
            
            2. 历史文件命名
               - 主文件: session_id.jsonl
               - 子文件: session_id_sub_1.jsonl, session_id_sub_2.jsonl...
               - 便于事后审查和调试
            
            3. 审批请求转发
               - 子 Agent 的审批请求会自动转发给主 Agent
               - 用户体验一致
            """;
    
    /**
     * 架构设计说明
     */
    public static final String ARCHITECTURE_NOTES = """
            Task 工具架构设计：
            
            1. 上下文隔离
               ┌─────────────┐
               │ Main Agent  │
               │ Context     │
               └─────────────┘
                     │
                     ├── Task Call 1
                     │   ┌────────────────┐
                     │   │ Subagent #1    │
                     │   │ Fresh Context  │
                     │   └────────────────┘
                     │
                     └── Task Call 2
                         ┌────────────────┐
                         │ Subagent #2    │
                         │ Fresh Context  │
                         └────────────────┘
            
            2. 消息流转
               User Input
                  │
                  ▼
               Main Agent ──Task──▶ Subagent
                  │                    │
                  │◀────Result────────┘
                  │
                  ▼
               User Output
            
            3. 生命周期
               a. 创建阶段
                  - 预加载所有子 Agent 规范
                  - 缓存在内存中
               
               b. 执行阶段
                  - 创建独立的历史文件
                  - 创建独立的 Context
                  - 创建独立的 ToolRegistry
                  - 创建子 JimiSoul
                  - 运行并等待完成
               
               c. 清理阶段
                  - 提取最终响应
                  - 历史文件保留（用于调试）
                  - 子 Agent 对象被 GC
            
            4. 与 Python 版本对等
               Python                  Java
               ─────────────────────────────────────
               CallableTool2          AbstractTool
               asyncio                Reactor Mono
               Pydantic Model         @Data class
               file_backend           historyFile
               run_soul()             soul.run()
            """;
}
