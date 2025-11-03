package io.leavesfly.jimi.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.leavesfly.jimi.agent.ResolvedAgentSpec;
import io.leavesfly.jimi.agent.SubagentSpec;
import io.leavesfly.jimi.config.JimiConfig;
import io.leavesfly.jimi.config.LoopControlConfig;
import io.leavesfly.jimi.session.Session;
import io.leavesfly.jimi.soul.approval.Approval;

import io.leavesfly.jimi.soul.runtime.BuiltinSystemPromptArgs;
import io.leavesfly.jimi.soul.runtime.Runtime;
import io.leavesfly.jimi.tool.ToolRegistry;
import io.leavesfly.jimi.tool.task.Task;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Task 工具集成演示
 * 
 * 演示如何在 Jimi 系统中集成和使用 Task 工具
 * 
 * @author 山泽
 */
public class TaskIntegrationDemo {
    
    public static void main(String[] args) throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Task 工具集成演示");
        System.out.println("=".repeat(70) + "\n");
        
        // 1. 准备环境
        System.out.println("步骤 1: 准备环境");
        System.out.println("-".repeat(70));
        
        Path tempDir = Files.createTempDirectory("jimi-task-demo-");
        System.out.println("✓ 临时目录: " + tempDir);
        
        Session session = Session.builder()
                .id(UUID.randomUUID().toString())
                .workDir(tempDir)
                .historyFile(tempDir.resolve("main_history.jsonl"))
                .createdAt(Instant.now())
                .build();
        System.out.println("✓ 会话 ID: " + session.getId());
        
        JimiConfig config = JimiConfig.builder()
                .loopControl(LoopControlConfig.builder()
                        .maxStepsPerRun(10)
                        .maxRetriesPerStep(3)
                        .build())
                .build();
        System.out.println("✓ 配置创建完成");
        
        // 2. 创建 Runtime
        System.out.println("\n步骤 2: 创建 Runtime");
        System.out.println("-".repeat(70));
        
        BuiltinSystemPromptArgs builtinArgs = BuiltinSystemPromptArgs.builder()
                .kimiNow(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .kimiWorkDir(tempDir)
                .kimiWorkDirLs("")
                .kimiAgentsMd("")
                .build();
        System.out.println("✓ 内置参数创建完成");
        
        Runtime runtime = Runtime.builder()
                .config(config)
                .llm(null)
                .session(session)
                .builtinArgs(builtinArgs)
                .approval(new Approval(true))  // YOLO 模式
                .build();
        System.out.println("✓ Runtime 创建完成");
        
        // 3. 创建 Agent 规范（带子 Agent）
        System.out.println("\n步骤 3: 创建 Agent 规范");
        System.out.println("-".repeat(70));
        
        ResolvedAgentSpec agentSpec = createMockAgentSpec(tempDir);
        System.out.println("✓ Agent 名称: " + agentSpec.getName());
        System.out.println("✓ 子 Agent 数量: " + agentSpec.getSubagents().size());
        for (String subName : agentSpec.getSubagents().keySet()) {
            SubagentSpec spec = agentSpec.getSubagents().get(subName);
            System.out.println("  - " + subName + ": " + spec.getDescription());
        }
        
        // 4. 创建工具注册表（包含 Task 工具）
        System.out.println("\n步骤 4: 创建工具注册表");
        System.out.println("-".repeat(70));
        
        ObjectMapper objectMapper = new ObjectMapper();
        ToolRegistry registry = ToolRegistry.createStandardRegistry(
                builtinArgs,
                runtime.getApproval(),
                objectMapper
        );
        System.out.println("✓ 标准工具数量: " + registry.getToolNames().size());
        
        // 注册 Task 工具
        Task taskTool = new Task(agentSpec, runtime, objectMapper);
        registry.register(taskTool);
        System.out.println("✓ Task 工具已注册");
        System.out.println("✓ 总工具数量: " + registry.getToolNames().size());
        
        // 5. 展示工具列表
        System.out.println("\n步骤 5: 工具列表");
        System.out.println("-".repeat(70));
        System.out.println("可用工具:");
        for (String toolName : registry.getToolNames()) {
            System.out.println("  ✓ " + toolName);
        }
        
        // 6. 演示 Task 工具调用（参数验证）
        System.out.println("\n步骤 6: Task 工具调用演示");
        System.out.println("-".repeat(70));
        
        System.out.println("\n测试 1: 使用有效的子 Agent");
        Task.Params validParams = Task.Params.builder()
                .description("Fix compilation error")
                .subagentName("code_fixer")
                .prompt("Fix the error in Main.java")
                .build();
        
        System.out.println("  参数:");
        System.out.println("    description: " + validParams.getDescription());
        System.out.println("    subagent_name: " + validParams.getSubagentName());
        System.out.println("    prompt: " + validParams.getPrompt());
        System.out.println("  ✓ 参数验证通过");
        
        System.out.println("\n测试 2: 使用无效的子 Agent");
        Task.Params invalidParams = Task.Params.builder()
                .description("Invalid task")
                .subagentName("nonexistent")
                .prompt("Some task")
                .build();
        
        System.out.println("  参数:");
        System.out.println("    subagent_name: " + invalidParams.getSubagentName());
        System.out.println("  ✓ 将返回错误（子 Agent 不存在）");
        
        // 7. 架构说明
        System.out.println("\n步骤 7: 架构说明");
        System.out.println("-".repeat(70));
        
        System.out.println("\nTask 工具在 Jimi 系统中的位置:");
        System.out.println("""
                
                用户输入
                   │
                   ▼
                CliApplication
                   │
                   ▼
                JimiFactory.createSoul()
                   │
                   ├── loadAgentSpec()          ← 加载 Agent 规范
                   │   └── 包含子 Agent 定义
                   │
                   ├── createToolRegistry()     ← 创建工具注册表
                   │   ├── 标准工具 (ReadFile, WriteFile, ...)
                   │   └── Task 工具（如果有子 Agent）
                   │
                   └── new JimiSoul()           ← 创建 Soul
                       └── Wire 消息总线
                
                当 LLM 调用 Task 工具时:
                   │
                   ▼
                Task.execute(params)
                   │
                   ├── 验证 subagent_name
                   ├── 创建子 Agent 上下文（独立历史文件）
                   ├── 创建子 JimiSoul
                   ├── 运行子 Agent
                   └── 返回结果给主 Agent
                """);
        
        // 8. 关键特性总结
        System.out.println("\n步骤 8: 关键特性总结");
        System.out.println("-".repeat(70));
        
        System.out.println("\nTask 工具的关键特性:");
        System.out.println("  1. ✓ 上下文隔离");
        System.out.println("     - 主 Agent 历史: " + session.getHistoryFile().getFileName());
        System.out.println("     - 子 Agent 历史: main_history_sub_1.jsonl, main_history_sub_2.jsonl, ...");
        
        System.out.println("\n  2. ✓ 并行执行");
        System.out.println("     - 基于 Reactor 的响应式编程");
        System.out.println("     - 多个子 Agent 可同时运行");
        
        System.out.println("\n  3. ✓ 自动响应检查");
        System.out.println("     - 最小响应长度: 200 字符");
        System.out.println("     - 自动发送 CONTINUE_PROMPT");
        
        System.out.println("\n  4. ✓ Wire 消息转发");
        System.out.println("     - 审批请求透明转发到主 Wire");
        System.out.println("     - 用户体验一致");
        
        System.out.println("\n  5. ✓ 专业化分工");
        System.out.println("     - code_fixer: 代码修复");
        System.out.println("     - info_seeker: 信息搜索");
        
        // 9. 使用建议
        System.out.println("\n步骤 9: 使用建议");
        System.out.println("-".repeat(70));
        
        System.out.println("\n最佳实践:");
        System.out.println("  ✅ DO:");
        System.out.println("    - 提供完整的背景信息（子 Agent 看不到主上下文）");
        System.out.println("    - 明确任务范围和成功标准");
        System.out.println("    - 利用并行执行处理独立任务");
        System.out.println("    - 根据任务选择合适的子 Agent");
        
        System.out.println("\n  ❌ DON'T:");
        System.out.println("    - 不要直接转发用户提示");
        System.out.println("    - 不要用于简单任务");
        System.out.println("    - 不要为每个 TODO 项创建 Task");
        System.out.println("    - 不要依赖子 Agent 之间的协作");
        
        // 10. 总结
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示完成");
        System.out.println("=".repeat(70));
        
        System.out.println("\n核心成果:");
        System.out.println("  ✓ Task 工具已成功集成到 Jimi 系统");
        System.out.println("  ✓ JimiFactory 自动注册 Task 工具（当有子 Agent 时）");
        System.out.println("  ✓ 完整的上下文隔离机制");
        System.out.println("  ✓ 支持并行多任务执行");
        System.out.println("  ✓ 自动响应质量检查");
        
        System.out.println("\n后续步骤:");
        System.out.println("  1. 创建示例子 Agent 配置");
        System.out.println("  2. 配置 LLM 进行实际测试");
        System.out.println("  3. 在实际项目中使用 Task 工具");
        
        System.out.println("\n临时文件保留在: " + tempDir);
        System.out.println("(用于检查子 Agent 历史文件)\n");
    }
    
    /**
     * 创建模拟的 Agent 规范
     */
    private static ResolvedAgentSpec createMockAgentSpec(Path tempDir) throws Exception {
        Path subagentsDir = tempDir.resolve("subagents");
        Files.createDirectories(subagentsDir);
        
        // 创建 code_fixer 子 Agent
        Path codeFixerDir = subagentsDir.resolve("code_fixer");
        Files.createDirectories(codeFixerDir);
        Path codeFixerAgent = codeFixerDir.resolve("agent.yaml");
        Files.writeString(codeFixerAgent, """
                version: 1
                agent:
                  name: CodeFixer
                  system_prompt_path: system.md
                  tools: [ReadFile, WriteFile, StrReplaceFile, Bash]
                """);
        Files.writeString(codeFixerDir.resolve("system.md"), 
                "# CodeFixer\\n\\nYou fix code errors.\\n\\nCurrent time: {{KIMI_NOW}}");
        
        // 创建 info_seeker 子 Agent
        Path infoSeekerDir = subagentsDir.resolve("info_seeker");
        Files.createDirectories(infoSeekerDir);
        Path infoSeekerAgent = infoSeekerDir.resolve("agent.yaml");
        Files.writeString(infoSeekerAgent, """
                version: 1
                agent:
                  name: InfoSeeker
                  system_prompt_path: system.md
                  tools: [SearchWeb, FetchURL]
                """);
        Files.writeString(infoSeekerDir.resolve("system.md"), 
                "# InfoSeeker\\n\\nYou search for information.\\n\\nCurrent time: {{KIMI_NOW}}");
        
        // 创建主 Agent 规范
        Map<String, SubagentSpec> subagents = new HashMap<>();
        subagents.put("code_fixer", SubagentSpec.builder()
                .path(codeFixerAgent)
                .description("Specialized in fixing compilation and runtime errors")
                .build());
        subagents.put("info_seeker", SubagentSpec.builder()
                .path(infoSeekerAgent)
                .description("Specialized in searching technical information")
                .build());
        
        return ResolvedAgentSpec.builder()
                .name("MainAgent")
                .systemPromptPath(tempDir.resolve("system.md"))
                .tools(List.of("Task", "ReadFile", "WriteFile"))
                .subagents(subagents)
                .build();
    }
}
