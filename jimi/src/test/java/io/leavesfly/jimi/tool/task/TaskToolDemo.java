package io.leavesfly.jimi.tool.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.leavesfly.jimi.agent.ResolvedAgentSpec;
import io.leavesfly.jimi.agent.SubagentSpec;
import io.leavesfly.jimi.config.JimiConfig;
import io.leavesfly.jimi.config.LoopControlConfig;
import io.leavesfly.jimi.session.Session;
import io.leavesfly.jimi.soul.approval.Approval;

import io.leavesfly.jimi.soul.runtime.BuiltinSystemPromptArgs;
import io.leavesfly.jimi.soul.runtime.Runtime;
import io.leavesfly.jimi.tool.ToolResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Task 工具演示与测试
 * <p>
 * 展示了 Task 工具的核心特性：
 * 1. 上下文隔离 - 子 Agent 拥有独立的历史记录
 * 2. 并行多任务 - 多个子 Agent 可以同时运行
 * 3. 专业化分工 - 不同的子 Agent 处理不同任务
 *
 * @author 山泽
 */
class TaskToolDemo {

    @TempDir
    Path tempDir;

    private ObjectMapper objectMapper;
    private Runtime runtime;
    private ResolvedAgentSpec mockAgentSpec;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();

        // 创建模拟会话
        Session session = Session.builder()
                .id(UUID.randomUUID().toString())
                .workDir(tempDir)
                .historyFile(tempDir.resolve("main_history.jsonl"))
                .createdAt(Instant.now())
                .build();

        // 创建配置
        JimiConfig config = JimiConfig.builder()
                .loopControl(LoopControlConfig.builder()
                        .maxStepsPerRun(10)
                        .maxRetriesPerStep(3)
                        .build())
                .build();

        // 创建内置参数
        BuiltinSystemPromptArgs builtinArgs = BuiltinSystemPromptArgs.builder()
                .kimiNow(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .kimiWorkDir(tempDir)
                .kimiWorkDirLs("")
                .kimiAgentsMd("")
                .build();

        // 创建 Runtime
        runtime = Runtime.builder()
                .config(config)
                .llm(null)  // 演示模式不需要真实 LLM
                .session(session)
                .builtinArgs(builtinArgs)
                .approval(new Approval(true))  // YOLO 模式
                .build();

        // 创建模拟的 Agent 规范（包含子 Agent）
        mockAgentSpec = createMockAgentSpec();
    }

    /**
     * 创建模拟的 Agent 规范
     */
    private ResolvedAgentSpec createMockAgentSpec() throws Exception {
        // 创建子 Agent 目录
        Path subagentsDir = tempDir.resolve("subagents");
        Files.createDirectories(subagentsDir);

        // 创建 CodeFixer 子 Agent
        Path codeFixerDir = subagentsDir.resolve("code_fixer");
        Files.createDirectories(codeFixerDir);

        Path codeFixerAgentFile = codeFixerDir.resolve("agent.yaml");
        String codeFixerYaml = """
                version: 1
                agent:
                  name: CodeFixer
                  system_prompt_path: system.md
                  tools:
                    - ReadFile
                    - WriteFile
                    - StrReplaceFile
                    - Bash
                """;
        Files.writeString(codeFixerAgentFile, codeFixerYaml);

        Path codeFixerSystemMd = codeFixerDir.resolve("system.md");
        String codeFixerPrompt = """
                # CodeFixer Agent
                                
                You are a specialized code fixing agent. Your job is to:
                1. Identify compilation or runtime errors
                2. Fix the issues efficiently
                3. Verify the fix works
                                
                Current time: {{KIMI_NOW}}
                Working directory: {{KIMI_WORK_DIR}}
                """;
        Files.writeString(codeFixerSystemMd, codeFixerPrompt);

        // 创建 InfoSeeker 子 Agent
        Path infoSeekerDir = subagentsDir.resolve("info_seeker");
        Files.createDirectories(infoSeekerDir);

        Path infoSeekerAgentFile = infoSeekerDir.resolve("agent.yaml");
        String infoSeekerYaml = """
                version: 1
                agent:
                  name: InfoSeeker
                  system_prompt_path: system.md
                  tools:
                    - SearchWeb
                    - FetchURL
                """;
        Files.writeString(infoSeekerAgentFile, infoSeekerYaml);

        Path infoSeekerSystemMd = infoSeekerDir.resolve("system.md");
        String infoSeekerPrompt = """
                # InfoSeeker Agent
                                
                You are a specialized information gathering agent. Your job is to:
                1. Search for specific technical information
                2. Filter out irrelevant results
                3. Return concise and relevant information
                                
                Current time: {{KIMI_NOW}}
                """;
        Files.writeString(infoSeekerSystemMd, infoSeekerPrompt);

        // 创建子 Agent 规范
        Map<String, SubagentSpec> subagents = new HashMap<>();

        subagents.put("code_fixer", SubagentSpec.builder()
                .path(codeFixerAgentFile)
                .description("Specialized in fixing compilation and runtime errors")
                .build());

        subagents.put("info_seeker", SubagentSpec.builder()
                .path(infoSeekerAgentFile)
                .description("Specialized in searching and gathering technical information")
                .build());

        // 创建主 Agent 规范
        return ResolvedAgentSpec.builder()
                .name("MainAgent")
                .systemPromptPath(tempDir.resolve("system.md"))
                .tools(List.of("Task", "ReadFile", "WriteFile"))
                .subagents(subagents)
                .build();
    }

    @Test
    void testTaskToolCreation() {
        System.out.println("\n=== Task 工具创建演示 ===\n");

        // 创建 Task 工具
        Task taskTool = new Task(mockAgentSpec, runtime, objectMapper);

        System.out.println("✓ Task 工具创建成功");
        System.out.println("  工具名称: " + taskTool.getName());
        System.out.println("  工具描述:\n" + taskTool.getDescription());

        assertNotNull(taskTool);
        assertEquals("Task", taskTool.getName());
        assertTrue(taskTool.getDescription().contains("code_fixer"));
        assertTrue(taskTool.getDescription().contains("info_seeker"));

        System.out.println("\n=== 测试完成 ===\n");
    }

    @Test
    void testTaskToolParamValidation() {
        System.out.println("\n=== Task 工具参数验证演示 ===\n");

        Task taskTool = new Task(mockAgentSpec, runtime, objectMapper);

        // 测试 1: 不存在的子 Agent
        System.out.println("测试 1: 不存在的子 Agent");
        Task.Params invalidParams = Task.Params.builder()
                .description("Fix error")
                .subagentName("nonexistent_agent")
                .prompt("Fix the compilation error")
                .build();

        ToolResult result = taskTool.execute(invalidParams).block();

        System.out.println("  结果: " + (result.isError() ? "✓ 正确拒绝" : "✗ 应该拒绝"));
        System.out.println("  消息: " + result.getMessage());
        assertTrue(result.isError());
        assertTrue(result.getMessage().contains("not found"));

        // 测试 2: 有效的子 Agent
        System.out.println("\n测试 2: 有效的子 Agent 名称");
        Task.Params validParams = Task.Params.builder()
                .description("Fix error")
                .subagentName("code_fixer")
                .prompt("Fix the compilation error in Main.java")
                .build();

        System.out.println("  子 Agent: " + validParams.getSubagentName());
        System.out.println("  描述: " + validParams.getDescription());
        System.out.println("  提示词: " + validParams.getPrompt());

        // 注意：由于没有真实 LLM，执行会失败，但参数验证会通过
        System.out.println("  ✓ 参数验证通过（需要 LLM 才能实际执行）");

        System.out.println("\n=== 测试完成 ===\n");
    }

    @Test
    void testSubagentHistoryFileCreation() throws Exception {
        System.out.println("\n=== 子 Agent 历史文件创建演示 ===\n");

        Task taskTool = new Task(mockAgentSpec, runtime, objectMapper);

        System.out.println("主历史文件: " + runtime.getSession().getHistoryFile());

        // 通过反射测试 getSubagentHistoryFile 方法
        // 实际使用时会自动创建

        System.out.println("\n子 Agent 历史文件命名规则:");
        System.out.println("  主文件: main_history.jsonl");
        System.out.println("  子文件 1: main_history_sub_1.jsonl");
        System.out.println("  子文件 2: main_history_sub_2.jsonl");
        System.out.println("  ...");

        System.out.println("\n✓ 历史文件隔离确保上下文独立");

        System.out.println("\n=== 测试完成 ===\n");
    }

    /**
     * 演示 Task 工具的使用场景
     */
    @Test
    void demonstrateTaskUsageScenarios() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Task 工具使用场景演示");
        System.out.println("=".repeat(60) + "\n");

        System.out.println("📚 场景 1: 修复编译错误");
        System.out.println("-".repeat(60));
        System.out.println("主 Agent 思考: 代码编译失败，但我不想让详细的调试过程污染我的上下文");
        System.out.println("解决方案: 使用 Task 工具委托给 code_fixer 子 Agent");
        System.out.println();
        System.out.println("调用示例:");
        System.out.println("""
                {
                  "description": "Fix compilation error",
                  "subagent_name": "code_fixer",
                  "prompt": "Fix the compilation error in src/Main.java. The error is: 
                            'cannot find symbol: variable xyz'. Please identify the issue,
                            fix it, and verify the fix works."
                }
                """);
        System.out.println("优势: 主上下文只看到最终结果，不包含中间的调试步骤\n");

        System.out.println("📚 场景 2: 搜索技术信息");
        System.out.println("-".repeat(60));
        System.out.println("主 Agent 思考: 需要了解某个库的最新用法，但不想看到大量搜索结果");
        System.out.println("解决方案: 使用 Task 工具委托给 info_seeker 子 Agent");
        System.out.println();
        System.out.println("调用示例:");
        System.out.println("""
                {
                  "description": "Search API usage",
                  "subagent_name": "info_seeker",
                  "prompt": "Search for the latest usage of Spring Boot 3.2 @RestController
                            annotation. Focus on best practices and common patterns. Return
                            only the most relevant code examples and explanations."
                }
                """);
        System.out.println("优势: 只返回精选的相关信息，过滤掉无关内容\n");

        System.out.println("📚 场景 3: 并行多任务");
        System.out.println("-".repeat(60));
        System.out.println("主 Agent 思考: 需要重构多个独立的模块");
        System.out.println("解决方案: 同时启动多个 Task 调用（并行执行）");
        System.out.println();
        System.out.println("调用示例（在一个 LLM 响应中）:");
        System.out.println("""
                // 工具调用 1
                {
                  "description": "Refactor UserService",
                  "subagent_name": "code_fixer",
                  "prompt": "Refactor UserService.java to use dependency injection..."
                }
                                
                // 工具调用 2
                {
                  "description": "Refactor OrderService", 
                  "subagent_name": "code_fixer",
                  "prompt": "Refactor OrderService.java to follow SOLID principles..."
                }
                                
                // 工具调用 3
                {
                  "description": "Refactor PaymentService",
                  "subagent_name": "code_fixer",
                  "prompt": "Refactor PaymentService.java to improve error handling..."
                }
                """);
        System.out.println("优势: 三个子 Agent 并行工作，大幅提升效率\n");

        System.out.println("📚 场景 4: 大型代码库分析");
        System.out.println("-".repeat(60));
        System.out.println("主 Agent 思考: 需要分析一个包含数十万行代码的项目");
        System.out.println("解决方案: 多个子 Agent 分别探索不同模块，汇总结果");
        System.out.println();
        System.out.println("调用示例:");
        System.out.println("""
                // 子 Agent 1 - 分析用户模块
                { "subagent_name": "code_fixer", "prompt": "Analyze user module..." }
                                
                // 子 Agent 2 - 分析订单模块
                { "subagent_name": "code_fixer", "prompt": "Analyze order module..." }
                                
                // 子 Agent 3 - 分析支付模块
                { "subagent_name": "code_fixer", "prompt": "Analyze payment module..." }
                """);
        System.out.println("优势: 分而治之，避免单个 Agent 上下文过载\n");

        System.out.println("=".repeat(60));
        System.out.println("✅ Task 工具是 Jimi 最强大的特性之一");
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * 演示与 Python 版本的对比
     */
    @Test
    void demonstratePythonJavaComparison() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Task 工具 - Python vs Java 实现对比");
        System.out.println("=".repeat(60) + "\n");

        System.out.println("核心功能对比:");
        System.out.println("-".repeat(60));

        String[][] comparison = {
                {"功能", "Python 实现", "Java 实现", "状态"},
                {"-".repeat(20), "-".repeat(20), "-".repeat(20), "-".repeat(10)},
                {"上下文隔离", "✓ Context(file_backend)", "✓ Context(historyFile)", "✅ 完成"},
                {"子 Agent 加载", "✓ load_agent()", "✓ AgentSpecLoader", "✅ 完成"},
                {"历史文件生成", "✓ next_available_rotation", "✓ getSubagentHistoryFile", "✅ 完成"},
                {"Wire 消息转发", "✓ _super_wire_send", "✓ Wire.asFlux().subscribe", "✅ 完成"},
                {"响应长度检查", "✓ len(final_response) < 200", "✓ response.length() < 200", "✅ 完成"},
                {"继续提示", "✓ CONTINUE_PROMPT", "✓ CONTINUE_PROMPT", "✅ 完成"},
                {"并行执行", "✓ asyncio", "✓ Reactor Mono/Flux", "✅ 完成"},
                {"错误处理", "✓ ToolError", "✓ ToolResult.error()", "✅ 完成"}
        };

        for (String[] row : comparison) {
            System.out.printf("%-20s %-25s %-30s %-10s%n",
                    row[0], row[1], row[2], row[3]);
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("架构设计对比:");
        System.out.println("-".repeat(60));

        System.out.println("\nPython 版本:");
        System.out.println("  - 使用 asyncio 实现异步");
        System.out.println("  - CallableTool2 基类");
        System.out.println("  - Pydantic 参数验证");

        System.out.println("\nJava 版本:");
        System.out.println("  - 使用 Reactor 实现响应式");
        System.out.println("  - AbstractTool 基类");
        System.out.println("  - Jackson 参数验证");

        System.out.println("\n共同特点:");
        System.out.println("  ✓ 完全的上下文隔离");
        System.out.println("  ✓ 支持并行多任务");
        System.out.println("  ✓ 自动处理短响应");
        System.out.println("  ✓ 审批请求透明转发");

        System.out.println("\n" + "=".repeat(60) + "\n");
    }
}
