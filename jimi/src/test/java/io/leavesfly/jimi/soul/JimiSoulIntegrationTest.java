package io.leavesfly.jimi.soul;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.leavesfly.jimi.config.JimiConfig;
import io.leavesfly.jimi.config.LoopControlConfig;
import io.leavesfly.jimi.llm.LLM;
import io.leavesfly.jimi.llm.MockChatProvider;
import io.leavesfly.jimi.session.Session;
import io.leavesfly.jimi.agent.Agent;
import io.leavesfly.jimi.soul.runtime.BuiltinSystemPromptArgs;
import io.leavesfly.jimi.soul.runtime.Runtime;
import io.leavesfly.jimi.tool.ToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JimiSoul 集成测试
 * 测试完整的 Agent 运行流程
 */
class JimiSoulIntegrationTest {

    private ObjectMapper objectMapper;
    private Path tempDir;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        this.tempDir = tempDir;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    void testSimpleConversation() throws Exception {
        System.out.println("\n=== 测试1：简单对话（无工具调用） ===\n");

        // 创建模拟的 LLM
        MockChatProvider mockProvider = new MockChatProvider("test-model");
        mockProvider.addTextResponse("你好！我是Jimi，很高兴为你服务。");

        LLM llm = LLM.builder()
                .chatProvider(mockProvider)
                .maxContextSize(100000)
                .build();

        // 创建配置
        JimiConfig config = JimiConfig.builder()
                .loopControl(LoopControlConfig.builder()
                        .maxStepsPerRun(10)
                        .build())
                .build();

        // 创建会话
        Session session = Session.builder()
                .id("test-session")
                .workDir(tempDir)
                .historyFile(tempDir.resolve("history.jsonl"))
                .build();

        // 创建 Runtime
        Runtime runtime = createRuntime(config, llm, session);

        // 创建 Agent
        Agent agent = Agent.builder()
                .name("TestAgent")
                .systemPrompt("你是一个友好的助手。")
                .tools(new ArrayList<>())
                .build();

        // 创建上下文
        Context context = new Context(session.getHistoryFile(), objectMapper);

        // 创建工具注册表
        ToolRegistry toolRegistry = new ToolRegistry(objectMapper);

        // 创建 JimiSoul
        JimiSoul soul = new JimiSoul(agent, runtime, context, toolRegistry, objectMapper);

        // 运行
        soul.run("你好").block();

        // 验证
        System.out.println("消息历史数量: " + context.getHistory().size());
        assertTrue(context.getHistory().size() >= 2, "应该至少有用户消息和助手响应");
        assertEquals("你好！我是Jimi，很高兴为你服务。", context.getHistory().get(1).getTextContent());

        System.out.println("✅ 测试1完成\n");
    }

    @Test
    void testWithToolCall() throws Exception {
        System.out.println("\n=== 测试2：带工具调用的对话 ===\n");

        // 创建模拟的 LLM
        MockChatProvider mockProvider = new MockChatProvider("test-model");

        // 第一次响应：调用Think工具
        mockProvider.addToolCallResponse("Think", "{\"thought\": \"我需要思考一下\"}");

        // 第二次响应：文本回复
        mockProvider.addTextResponse("我已经思考完毕，可以帮你了。");

        LLM llm = LLM.builder()
                .chatProvider(mockProvider)
                .maxContextSize(100000)
                .build();

        // 创建配置
        JimiConfig config = JimiConfig.builder()
                .loopControl(LoopControlConfig.builder()
                        .maxStepsPerRun(10)
                        .build())
                .build();

        // 创建会话
        Session session = Session.builder()
                .id("test-session-2")
                .workDir(tempDir)
                .historyFile(tempDir.resolve("history2.jsonl"))
                .build();

        // 创建 Runtime
        Runtime runtime = createRuntime(config, llm, session);

        // 创建 Agent（带Think工具）
        Agent agent = Agent.builder()
                .name("TestAgent")
                .systemPrompt("你是一个会思考的助手。")
                .tools(List.of("Think"))
                .build();

        // 创建上下文
        Context context = new Context(session.getHistoryFile(), objectMapper);

        // 创建工具注册表（带Think工具）
        ToolRegistry toolRegistry = ToolRegistry.createStandardRegistry(
                runtime.getBuiltinArgs(),
                runtime.getApproval(),
                objectMapper
        );

        // 创建 JimiSoul
        JimiSoul soul = new JimiSoul(agent, runtime, context, toolRegistry, objectMapper);

        // 运行
        soul.run("帮我分析一个问题").block();

        // 验证
        System.out.println("消息历史数量: " + context.getHistory().size());

        // 应该有：用户消息、带工具调用的助手消息、工具结果消息、最终助手响应
        assertTrue(context.getHistory().size() >= 4, "应该有完整的对话流程");

        System.out.println("✅ 测试2完成\n");
    }

    @Test
    void testMaxStepsLimit() throws Exception {
        System.out.println("\n=== 测试3：最大步数限制 ===\n");

        // 创建模拟的 LLM（总是返回工具调用，触发循环）
        MockChatProvider mockProvider = new MockChatProvider("test-model");

        // 添加10个工具调用响应
        for (int i = 0; i < 10; i++) {
            mockProvider.addToolCallResponse("Think", "{\"thought\": \"思考中...\"}");
        }

        LLM llm = LLM.builder()
                .chatProvider(mockProvider)
                .maxContextSize(100000)
                .build();

        // 创建配置（最大步数=5）
        JimiConfig config = JimiConfig.builder()
                .loopControl(LoopControlConfig.builder()
                        .maxStepsPerRun(5)
                        .build())
                .build();

        // 创建会话
        Session session = Session.builder()
                .id("test-session-3")
                .workDir(tempDir)
                .historyFile(tempDir.resolve("history3.jsonl"))
                .build();

        // 创建 Runtime
        Runtime runtime = createRuntime(config, llm, session);

        // 创建 Agent
        Agent agent = Agent.builder()
                .name("TestAgent")
                .systemPrompt("你是一个助手。")
                .tools(List.of("Think"))
                .build();

        // 创建上下文
        Context context = new Context(session.getHistoryFile(), objectMapper);

        // 创建工具注册表
        ToolRegistry toolRegistry = ToolRegistry.createStandardRegistry(
                runtime.getBuiltinArgs(),
                runtime.getApproval(),
                objectMapper
        );

        // 创建 JimiSoul
        JimiSoul soul = new JimiSoul(agent, runtime, context, toolRegistry, objectMapper);

        // 运行应该抛出异常
        try {
            soul.run("测试").block();
            fail("应该抛出 MaxStepsReachedException");
        } catch (Exception e) {
            System.out.println("捕获到异常: " + e.getClass().getSimpleName());
            assertTrue(e.getMessage().contains("MaxStepsReachedException") ||
                            e.getCause() != null && e.getCause().getClass().getSimpleName().contains("MaxStepsReachedException"),
                    "应该是最大步数异常");
        }

        System.out.println("✅ 测试3完成\n");
    }

    /**
     * 创建 Runtime
     */
    private Runtime createRuntime(JimiConfig config, LLM llm, Session session) {
        BuiltinSystemPromptArgs builtinArgs = BuiltinSystemPromptArgs.builder()
                .kimiNow(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .kimiWorkDir(session.getWorkDir())
                .kimiWorkDirLs("")
                .kimiAgentsMd("")
                .build();

        return Runtime.builder()
                .config(config)
                .llm(llm)
                .session(session)
                .builtinArgs(builtinArgs)
                .approval(new io.leavesfly.jimi.soul.approval.Approval(true)) // YOLO模式
                .build();
    }
}
