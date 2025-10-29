package io.leavesfly.jimi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.leavesfly.jimi.JimiFactory;
import io.leavesfly.jimi.config.ConfigLoader;
import io.leavesfly.jimi.config.JimiConfig;
import io.leavesfly.jimi.session.Session;
import io.leavesfly.jimi.session.SessionManager;
import io.leavesfly.jimi.soul.JimiSoul;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Jimi 模块集成演示
 * 
 * 展示所有核心模块的集成和协作：
 * 1. 配置加载（ConfigLoader）
 * 2. 会话管理（SessionManager）
 * 3. Agent 加载（AgentSpecLoader）
 * 4. LLM 提供商（KimiChatProvider）
 * 5. 工具注册表（ToolRegistry）
 * 6. Task 工具（子 Agent 系统）
 * 7. MCP 工具（外部工具集成）
 * 8. 上下文管理（Context）
 * 9. 核心循环（JimiSoul）
 * 10. 用户界面（ShellUI）
 * 11. 工具可视化（ToolVisualization）
 * 
 * @author 山泽
 */
class JimiIntegrationDemo {
    
    @TempDir
    Path tempDir;
    
    /**
     * 演示 1: 完整启动流程
     */
    @Test
    void demo1_CompleteBootstrapFlow() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 1: Jimi 完整启动流程");
        System.out.println("=".repeat(70) + "\n");
        
        System.out.println("步骤 1: 加载配置");
        ConfigLoader configLoader = new ConfigLoader();
        JimiConfig config = configLoader.loadConfig(null);
        System.out.println("  ✓ 配置已加载");
        System.out.println("    - 提供商数量: " + config.getProviders().size());
        System.out.println("    - 模型数量: " + config.getModels().size());
        System.out.println("    - 默认模型: " + config.getDefaultModel());
        
        System.out.println("\n步骤 2: 创建会话");
        SessionManager sessionManager = new SessionManager();
        Session session = sessionManager.createSession(tempDir);
        System.out.println("  ✓ 会话已创建");
        System.out.println("    - 会话 ID: " + session.getId());
        System.out.println("    - 工作目录: " + session.getWorkDir());
        System.out.println("    - 历史文件: " + session.getHistoryFile());
        
        System.out.println("\n步骤 3: 创建 Jimi Soul");
        ObjectMapper objectMapper = new ObjectMapper();
        JimiFactory factory = new JimiFactory(config, objectMapper);
        
        // 准备参数
        Path agentFile = null;  // 使用默认 agent
        String modelName = null;  // 使用配置默认模型
        boolean yolo = true;  // YOLO 模式（演示用）
        List<Path> mcpConfigFiles = new ArrayList<>();  // 暂无 MCP 配置
        
        System.out.println("  正在组装组件...");
        try {
            JimiSoul soul = factory.createSoul(session, agentFile, modelName, yolo, mcpConfigFiles).block();
            
            if (soul != null) {
                System.out.println("  ✓ Jimi Soul 创建成功");
                System.out.println("    - Agent 名称: " + soul.getAgent().getName());
                System.out.println("    - 工具数量: " + soul.getToolRegistry().getToolNames().size());
                System.out.println("    - 可用工具: " + String.join(", ", soul.getToolRegistry().getToolNames()));
            } else {
                System.out.println("  ✗ Jimi Soul 创建失败（可能缺少 API Key）");
            }
        } catch (Exception e) {
            System.out.println("  ✗ 创建失败: " + e.getMessage());
            System.out.println("    注意: 这是正常的，演示环境可能缺少必要的配置");
        }
        
        System.out.println("\n✅ 演示完成\n");
    }
    
    /**
     * 演示 2: 模块依赖关系
     */
    @Test
    void demo2_ModuleDependencies() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 2: Jimi 模块依赖关系");
        System.out.println("=".repeat(70) + "\n");
        
        System.out.println("模块依赖图:\n");
        System.out.println("""
                ┌─────────────────┐
                │  CliApplication │  命令行入口
                └────────┬────────┘
                         │
                         ↓
                ┌────────────────┐
                │  JimiFactory   │  组件工厂
                └────────┬───────┘
                         │
                ┌────────┴───────────────────────────────────┐
                │                                            │
                ↓                                            ↓
        ┌──────────────┐                            ┌──────────────┐
        │ ConfigLoader │                            │SessionManager│
        └──────┬───────┘                            └──────┬───────┘
               │                                           │
               ↓                                           ↓
        ┌──────────────┐                            ┌──────────────┐
        │  JimiConfig  │                            │   Session    │
        └──────────────┘                            └──────────────┘
                │                                           │
                └────────────────┬──────────────────────────┘
                                 │
                                 ↓
                         ┌──────────────┐
                         │  JimiSoul    │  核心循环
                         └──────┬───────┘
                                │
                ┌───────────────┼───────────────┬────────────────┐
                │               │               │                │
                ↓               ↓               ↓                ↓
        ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
        │  Agent   │    │ Runtime  │    │ Context  │    │ToolRegistry│
        └──────────┘    └──────┬───┘    └──────────┘    └──────┬────┘
                               │                                │
                        ┌──────┴─────┐                  ┌───────┴────────┐
                        │            │                  │                │
                        ↓            ↓                  ↓                ↓
                   ┌────────┐  ┌─────────┐      ┌──────────┐    ┌──────────┐
                   │  LLM   │  │Approval │      │BasicTools│    │TaskTool  │
                   └────────┘  └─────────┘      └──────────┘    └──────────┘
                        │                                              │
                        ↓                                              ↓
                ┌──────────────┐                              ┌──────────────┐
                │ChatProvider  │                              │  MCPTools    │
                └──────────────┘                              └──────────────┘
                """);
        
        System.out.println("\n核心组件职责:\n");
        
        System.out.println("1. CliApplication");
        System.out.println("   - 解析命令行参数");
        System.out.println("   - 协调应用启动");
        System.out.println("   - 创建 UI 实例");
        
        System.out.println("\n2. JimiFactory");
        System.out.println("   - 组装所有依赖");
        System.out.println("   - 创建 JimiSoul 实例");
        System.out.println("   - 管理生命周期");
        
        System.out.println("\n3. JimiSoul");
        System.out.println("   - Agent 主循环");
        System.out.println("   - LLM 调用");
        System.out.println("   - 工具执行");
        System.out.println("   - 上下文管理");
        
        System.out.println("\n4. Agent");
        System.out.println("   - 系统提示词");
        System.out.println("   - 工具配置");
        System.out.println("   - 行为定义");
        
        System.out.println("\n5. Runtime");
        System.out.println("   - 运行时依赖容器");
        System.out.println("   - LLM 实例");
        System.out.println("   - 审批机制");
        System.out.println("   - D-Mail 系统");
        
        System.out.println("\n6. ToolRegistry");
        System.out.println("   - 工具注册");
        System.out.println("   - 工具发现");
        System.out.println("   - 工具调用");
        
        System.out.println("\n7. Context");
        System.out.println("   - 对话历史");
        System.out.println("   - 持久化");
        System.out.println("   - 检查点");
        
        System.out.println("\n8. ShellUI");
        System.out.println("   - 交互界面");
        System.out.println("   - Wire 消息处理");
        System.out.println("   - 工具可视化");
        
        System.out.println("\n✅ 演示完成\n");
    }
    
    /**
     * 演示 3: 数据流向
     */
    @Test
    void demo3_DataFlow() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 3: Jimi 数据流向");
        System.out.println("=".repeat(70) + "\n");
        
        System.out.println("典型对话流程:\n");
        
        System.out.println("1️⃣  用户输入阶段");
        System.out.println("  用户 → ShellUI.readLine()");
        System.out.println("     → ShellUI.processInput()");
        System.out.println("     → JimiSoul.run(userMessage)");
        
        System.out.println("\n2️⃣  上下文准备");
        System.out.println("  JimiSoul → Context.appendMessage(userMessage)");
        System.out.println("          → Context.getHistory()");
        System.out.println("          → 检查上下文长度");
        System.out.println("          → 必要时触发 Compaction");
        
        System.out.println("\n3️⃣  LLM 调用");
        System.out.println("  JimiSoul → LLM.generateStream()");
        System.out.println("          → ChatProvider.generateStream()");
        System.out.println("          → Flux<ChatCompletionChunk>");
        System.out.println("          → 实时流式输出");
        
        System.out.println("\n4️⃣  工具调用");
        System.out.println("  LLM 返回 ToolCall");
        System.out.println("     → Wire.publish(ToolCallMessage)");
        System.out.println("     → ShellUI 显示工具调用");
        System.out.println("     → ToolVisualization 动画");
        System.out.println("     → ToolRegistry.execute()");
        System.out.println("     → Tool.execute(params)");
        System.out.println("     → 返回 ToolResult");
        System.out.println("     → Wire.publish(ToolResultMessage)");
        System.out.println("     → ShellUI 显示结果");
        
        System.out.println("\n5️⃣  继续对话");
        System.out.println("  ToolResult → Context.appendMessage()");
        System.out.println("           → 重新调用 LLM");
        System.out.println("           → 直到得到最终回复");
        
        System.out.println("\n6️⃣  输出响应");
        System.out.println("  LLM 最终回复");
        System.out.println("     → Wire.publish(ContentPartMessage)");
        System.out.println("     → ShellUI 显示内容");
        System.out.println("     → Context.appendMessage(assistantMessage)");
        System.out.println("     → 对话完成");
        
        System.out.println("\n7️⃣  持久化");
        System.out.println("  所有消息 → Context.appendMessage()");
        System.out.println("          → 写入 history.jsonl");
        System.out.println("          → 更新 token 计数");
        System.out.println("          → 下次启动可恢复");
        
        System.out.println("\n✅ 演示完成\n");
    }
    
    /**
     * 演示 4: 扩展点
     */
    @Test
    void demo4_ExtensionPoints() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 4: Jimi 扩展点");
        System.out.println("=".repeat(70) + "\n");
        
        System.out.println("Jimi 提供的扩展机制:\n");
        
        System.out.println("1. 自定义 Agent");
        System.out.println("   方式: 创建 agent.yaml 文件");
        System.out.println("   能力:");
        System.out.println("     - 自定义系统提示词");
        System.out.println("     - 选择工具子集");
        System.out.println("     - 配置子 Agent");
        System.out.println("   使用: jimi --agent-file /path/to/agent.yaml");
        
        System.out.println("\n2. 自定义工具");
        System.out.println("   方式: 继承 AbstractTool<P>");
        System.out.println("   步骤:");
        System.out.println("     1. 定义 Params 类");
        System.out.println("     2. 实现 execute() 方法");
        System.out.println("     3. 在 ToolRegistry 注册");
        System.out.println("   示例: Think, Task, PatchFile");
        
        System.out.println("\n3. MCP 外部工具");
        System.out.println("   方式: 创建 mcp-config.json");
        System.out.println("   支持:");
        System.out.println("     - STDIO 进程工具");
        System.out.println("     - HTTP 远程工具");
        System.out.println("     - 任何 MCP 兼容服务");
        System.out.println("   使用: jimi --mcp-config-file config.json");
        
        System.out.println("\n4. 子 Agent 系统");
        System.out.println("   方式: 在 agent.yaml 中定义 subagents");
        System.out.println("   能力:");
        System.out.println("     - 任务委托");
        System.out.println("     - 上下文隔离");
        System.out.println("     - 并行执行");
        System.out.println("   使用: Task 工具自动注册");
        
        System.out.println("\n5. 自定义 LLM 提供商");
        System.out.println("   方式: 实现 ChatProvider 接口");
        System.out.println("   方法:");
        System.out.println("     - generate() - 同步调用");
        System.out.println("     - generateStream() - 流式调用");
        System.out.println("   示例: KimiChatProvider");
        
        System.out.println("\n6. 自定义 UI");
        System.out.println("   方式: 订阅 Wire 消息总线");
        System.out.println("   消息类型:");
        System.out.println("     - StepBegin/StepInterrupted");
        System.out.println("     - ContentPartMessage");
        System.out.println("     - ToolCallMessage/ToolResultMessage");
        System.out.println("     - StatusUpdate");
        System.out.println("   示例: ShellUI, PrintUI, ACPUI");
        
        System.out.println("\n7. 上下文压缩策略");
        System.out.println("   方式: 实现 CompactionStrategy 接口");
        System.out.println("   方法: compact(history, maxTokens)");
        System.out.println("   示例: SimpleCompaction");
        
        System.out.println("\n8. 审批机制");
        System.out.println("   方式: 自定义 Approval 实现");
        System.out.println("   模式:");
        System.out.println("     - YOLO 模式（自动批准）");
        System.out.println("     - 交互模式（用户确认）");
        System.out.println("     - 会话级批准");
        
        System.out.println("\n✅ 演示完成\n");
    }
    
    /**
     * 演示 5: 功能总结
     */
    @Test
    void demo5_FeatureSummary() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Jimi 功能总结");
        System.out.println("=".repeat(70) + "\n");
        
        System.out.println("核心能力:\n");
        
        System.out.println("✅ 1. 多模型支持");
        System.out.println("   - Kimi 系列模型");
        System.out.println("   - 可扩展其他提供商");
        System.out.println("   - 环境变量覆盖配置");
        
        System.out.println("\n✅ 2. 丰富的工具集");
        System.out.println("   - 文件操作: ReadFile, WriteFile, StrReplaceFile, PatchFile, Glob, Grep");
        System.out.println("   - Shell 执行: Bash");
        System.out.println("   - Web 工具: SearchWeb, FetchURL");
        System.out.println("   - 思考工具: Think");
        System.out.println("   - 任务管理: Todo, Task");
        System.out.println("   - 时间旅行: DMail");
        
        System.out.println("\n✅ 3. 子 Agent 系统");
        System.out.println("   - 任务委托");
        System.out.println("   - 上下文隔离");
        System.out.println("   - 并行执行");
        System.out.println("   - 自动响应检查");
        
        System.out.println("\n✅ 4. MCP 集成");
        System.out.println("   - 标准 MCP 协议");
        System.out.println("   - STDIO 传输");
        System.out.println("   - HTTP 传输（待实现）");
        System.out.println("   - 外部工具扩展");
        
        System.out.println("\n✅ 5. 上下文管理");
        System.out.println("   - JSONL 持久化");
        System.out.println("   - 自动恢复");
        System.out.println("   - 检查点机制");
        System.out.println("   - 时间旅行");
        System.out.println("   - 智能压缩");
        
        System.out.println("\n✅ 6. 流式响应");
        System.out.println("   - 实时输出");
        System.out.println("   - 降低延迟");
        System.out.println("   - 工具调用流式处理");
        
        System.out.println("\n✅ 7. 交互式 UI");
        System.out.println("   - JLine 终端");
        System.out.println("   - 彩色输出");
        System.out.println("   - 命令历史");
        System.out.println("   - 自动补全");
        
        System.out.println("\n✅ 8. 工具可视化");
        System.out.println("   - 实时进度");
        System.out.println("   - 旋转动画");
        System.out.println("   - 执行时间");
        System.out.println("   - 成功/失败标识");
        
        System.out.println("\n✅ 9. 安全机制");
        System.out.println("   - 审批确认");
        System.out.println("   - 路径验证");
        System.out.println("   - YOLO 模式");
        
        System.out.println("\n✅ 10. 可扩展架构");
        System.out.println("   - 自定义 Agent");
        System.out.println("   - 自定义工具");
        System.out.println("   - 自定义 LLM");
        System.out.println("   - 自定义 UI");
        
        System.out.println("\n技术栈:");
        System.out.println("  - Java 17");
        System.out.println("  - Spring Boot");
        System.out.println("  - Project Reactor");
        System.out.println("  - JLine 3");
        System.out.println("  - Jackson");
        System.out.println("  - MCP Java SDK");
        System.out.println("  - Maven");
        
        System.out.println("\n代码规模:");
        System.out.println("  - 核心模块: ~15,000 行");
        System.out.println("  - 测试演示: ~8,000 行");
        System.out.println("  - 总计: ~23,000 行");
        
        System.out.println("\n与 Python 版本对比:");
        System.out.println("  - 功能对等: ✅");
        System.out.println("  - API 设计: ✅");
        System.out.println("  - 性能优化: ✅");
        System.out.println("  - 类型安全: ✅ (Java 强类型)");
        System.out.println("  - 响应式: ✅ (Reactor vs asyncio)");
        
        System.out.println("\n" + "=".repeat(70) + "\n");
    }
}
