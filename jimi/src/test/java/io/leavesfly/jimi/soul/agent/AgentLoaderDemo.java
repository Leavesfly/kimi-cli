package io.leavesfly.jimi.soul.agent;

import io.leavesfly.jimi.agentspec.AgentSpecLoader;
import io.leavesfly.jimi.agentspec.ResolvedAgentSpec;
import io.leavesfly.jimi.config.*;
import io.leavesfly.jimi.session.Session;
import io.leavesfly.jimi.soul.runtime.Runtime;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * AgentLoader（Agent加载器）演示测试
 * 展示Agent从YAML配置文件的加载过程
 */
@DisplayName("AgentLoader Agent加载器演示")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AgentLoaderDemo {
    
    @TempDir
    static Path tempDir;
    
    private static JimiConfig createTestConfig() {
        LLMProviderConfig provider = LLMProviderConfig.builder()
                .type(LLMProviderConfig.ProviderType.KIMI)
                .baseUrl("https://api.moonshot.cn/v1")
                .apiKey("test-key")
                .build();
        
        Map<String, LLMProviderConfig> providers = new HashMap<>();
        providers.put("kimi", provider);
        
        LLMModelConfig model = LLMModelConfig.builder()
                .provider("kimi")
                .model("moonshot-v1-8k")
                .maxContextSize(8000)
                .build();
        
        Map<String, LLMModelConfig> models = new HashMap<>();
        models.put("default", model);
        
        LoopControlConfig loopControl = LoopControlConfig.builder()
                .maxStepsPerRun(50)
                .build();
        
        return JimiConfig.builder()
                .providers(providers)
                .models(models)
                .loopControl(loopControl)
                .build();
    }
    
    @Test
    @Order(1)
    @DisplayName("演示1：加载AgentSpec规范")
    void demo1_loadAgentSpec() {
        System.out.println("\n=== 演示1：加载AgentSpec规范 ===\n");
        
        // 获取测试资源中的agent.yaml
        Path agentFile = Path.of("src/test/resources/agents/test/agent.yaml");
        
        if (!Files.exists(agentFile)) {
            System.out.println("⚠️  测试文件不存在，跳过测试: " + agentFile);
            return;
        }
        
        System.out.println("加载Agent规范: " + agentFile);
        
        // 加载AgentSpec
        ResolvedAgentSpec spec = AgentSpecLoader.loadAgentSpec(agentFile).block();
        
        System.out.println("\nAgent规范信息：");
        System.out.println("  名称: " + spec.getName());
        System.out.println("  系统提示词路径: " + spec.getSystemPromptPath());
        System.out.println("  工具数量: " + spec.getTools().size());
        
        System.out.println("\n工具列表：");
        for (String tool : spec.getTools()) {
            System.out.println("  - " + tool);
        }
        
        System.out.println("\n系统提示词参数：");
        spec.getSystemPromptArgs().forEach((key, value) -> 
            System.out.println("  " + key + ": " + value)
        );
        
        Assertions.assertNotNull(spec);
        Assertions.assertEquals("测试Agent", spec.getName());
        Assertions.assertTrue(spec.getTools().size() > 0);
        
        System.out.println("\n✅ 演示1完成\n");
    }
    
    @Test
    @Order(2)
    @DisplayName("演示2：加载完整的Agent实例")
    void demo2_loadCompleteAgent() throws Exception {
        System.out.println("\n=== 演示2：加载完整的Agent实例 ===\n");
        
        // 准备Runtime
        JimiConfig config = createTestConfig();
        Session session = Session.builder()
                .id("test-session")
                .workDir(tempDir)
                .build();
        
        // 创建AGENTS.md
        Path agentsMd = tempDir.resolve("AGENTS.md");
        Files.writeString(agentsMd, "# 项目规范\n\n这是测试项目的规范文档。");
        
        Runtime runtime = Runtime.create(config, session, false).block();
        System.out.println("✓ Runtime创建完成");
        
        // 加载Agent
        Path agentFile = Path.of("src/test/resources/agents/test/agent.yaml");
        
        if (!Files.exists(agentFile)) {
            System.out.println("⚠️  测试文件不存在，跳过测试");
            return;
        }
        
        Agent agent = AgentLoader.loadAgent(agentFile, runtime).block();
        System.out.println("✓ Agent加载完成");
        
        System.out.println("\nAgent信息：");
        System.out.println("  名称: " + agent.getName());
        System.out.println("  工具数量: " + agent.getTools().size());
        System.out.println("  系统提示词长度: " + agent.getSystemPrompt().length() + " 字符");
        
        System.out.println("\n系统提示词预览（前500字符）：");
        System.out.println("─".repeat(60));
        String preview = agent.getSystemPrompt().substring(
                0, Math.min(500, agent.getSystemPrompt().length())
        );
        System.out.println(preview);
        if (agent.getSystemPrompt().length() > 500) {
            System.out.println("\n... (省略 " + (agent.getSystemPrompt().length() - 500) + " 字符)");
        }
        System.out.println("─".repeat(60));
        
        // 验证
        Assertions.assertNotNull(agent);
        Assertions.assertEquals("测试Agent", agent.getName());
        Assertions.assertTrue(agent.getSystemPrompt().contains("Jimi"));
        Assertions.assertTrue(agent.getSystemPrompt().contains(tempDir.toString()));
        
        System.out.println("\n✅ 演示2完成\n");
    }
    
    @Test
    @Order(3)
    @DisplayName("演示3：系统提示词参数替换")
    void demo3_systemPromptSubstitution() throws Exception {
        System.out.println("\n=== 演示3：系统提示词参数替换 ===\n");
        
        JimiConfig config = createTestConfig();
        Session session = Session.builder()
                .id("test-session-003")
                .workDir(tempDir)
                .build();
        
        Runtime runtime = Runtime.create(config, session, false).block();
        
        Path agentFile = Path.of("src/test/resources/agents/test/agent.yaml");
        if (!Files.exists(agentFile)) {
            System.out.println("⚠️  测试文件不存在，跳过测试");
            return;
        }
        
        Agent agent = AgentLoader.loadAgent(agentFile, runtime).block();
        
        String systemPrompt = agent.getSystemPrompt();
        
        System.out.println("检查参数替换：");
        
        // 检查KIMI_NOW
        boolean hasKimiNow = systemPrompt.contains("2025");
        System.out.println("  ✓ KIMI_NOW 已替换: " + hasKimiNow);
        
        // 检查KIMI_WORK_DIR
        boolean hasWorkDir = systemPrompt.contains(tempDir.toString());
        System.out.println("  ✓ KIMI_WORK_DIR 已替换: " + hasWorkDir);
        
        // 检查ROLE_ADDITIONAL
        boolean hasRoleAdditional = systemPrompt.contains("Java开发助手");
        System.out.println("  ✓ ROLE_ADDITIONAL 已替换: " + hasRoleAdditional);
        
        // 确保没有未替换的变量
        boolean noUnsubstituted = !systemPrompt.contains("${");
        System.out.println("  ✓ 无未替换变量: " + noUnsubstituted);
        
        Assertions.assertTrue(hasKimiNow, "KIMI_NOW应该被替换");
        Assertions.assertTrue(hasWorkDir, "KIMI_WORK_DIR应该被替换");
        Assertions.assertTrue(hasRoleAdditional, "ROLE_ADDITIONAL应该被替换");
        Assertions.assertTrue(noUnsubstituted, "不应该有未替换的变量");
        
        System.out.println("\n✅ 演示3完成\n");
    }
    
    @Test
    @Order(4)
    @DisplayName("演示4：工具列表处理")
    void demo4_toolsProcessing() throws Exception {
        System.out.println("\n=== 演示4：工具列表处理 ===\n");
        
        JimiConfig config = createTestConfig();
        Session session = Session.builder()
                .id("test-session-004")
                .workDir(tempDir)
                .build();
        
        Runtime runtime = Runtime.create(config, session, false).block();
        
        Path agentFile = Path.of("src/test/resources/agents/test/agent.yaml");
        if (!Files.exists(agentFile)) {
            System.out.println("⚠️  测试文件不存在，跳过测试");
            return;
        }
        
        Agent agent = AgentLoader.loadAgent(agentFile, runtime).block();
        
        System.out.println("工具列表分析：");
        System.out.println("  总工具数: " + agent.getTools().size());
        
        System.out.println("\n详细工具列表：");
        for (int i = 0; i < agent.getTools().size(); i++) {
            String tool = agent.getTools().get(i);
            String[] parts = tool.split(":");
            String module = parts.length > 0 ? parts[0] : "unknown";
            String className = parts.length > 1 ? parts[1] : "unknown";
            
            System.out.println(String.format("  %d. %s", i + 1, className));
            System.out.println(String.format("     模块: %s", module));
        }
        
        // 验证工具格式
        for (String tool : agent.getTools()) {
            Assertions.assertTrue(tool.contains(":"), 
                    "工具应该包含模块和类名，格式: module:ClassName");
        }
        
        System.out.println("\n✅ 演示4完成\n");
    }
    
    @Test
    @Order(5)
    @DisplayName("演示5：内置参数验证")
    void demo5_builtinParameters() throws Exception {
        System.out.println("\n=== 演示5：内置参数验证 ===\n");
        
        JimiConfig config = createTestConfig();
        Session session = Session.builder()
                .id("test-session-005")
                .workDir(tempDir)
                .build();
        
        // 创建测试文件
        Files.writeString(tempDir.resolve("test.txt"), "测试文件");
        
        Runtime runtime = Runtime.create(config, session, false).block();
        
        System.out.println("Runtime内置参数：");
        System.out.println("  KIMI_NOW: " + runtime.getBuiltinArgs().getKimiNow());
        System.out.println("  KIMI_WORK_DIR: " + runtime.getBuiltinArgs().getKimiWorkDir());
        System.out.println("  KIMI_WORK_DIR_LS行数: " + 
                runtime.getBuiltinArgs().getKimiWorkDirLs().split("\n").length);
        System.out.println("  KIMI_AGENTS_MD长度: " + 
                runtime.getBuiltinArgs().getKimiAgentsMd().length());
        
        Path agentFile = Path.of("src/test/resources/agents/test/agent.yaml");
        if (!Files.exists(agentFile)) {
            System.out.println("\n⚠️  测试文件不存在，跳过测试");
            return;
        }
        
        Agent agent = AgentLoader.loadAgent(agentFile, runtime).block();
        
        System.out.println("\n系统提示词中的内置参数：");
        System.out.println("  包含时间信息: " + agent.getSystemPrompt().contains("2025"));
        System.out.println("  包含工作目录: " + agent.getSystemPrompt().contains(tempDir.toString()));
        System.out.println("  包含test.txt: " + agent.getSystemPrompt().contains("test.txt"));
        
        System.out.println("\n✅ 演示5完成\n");
    }
    
    @Test
    @Order(6)
    @DisplayName("演示6：完整的Agent加载流程")
    void demo6_completeLoadingFlow() throws Exception {
        System.out.println("\n=== 演示6：完整的Agent加载流程 ===\n");
        
        // 1. 准备阶段
        System.out.println("1. 准备阶段");
        JimiConfig config = createTestConfig();
        Session session = Session.builder()
                .id("complete-session")
                .workDir(tempDir)
                .build();
        
        Files.writeString(tempDir.resolve("README.md"), "# 测试项目");
        Files.writeString(tempDir.resolve("AGENTS.md"), "# Agent规范\n\n请遵循最佳实践。");
        System.out.println("   ✓ 配置准备完成");
        System.out.println("   ✓ 工作目录准备完成");
        
        // 2. 创建Runtime
        System.out.println("\n2. 创建Runtime");
        Runtime runtime = Runtime.create(config, session, false).block();
        System.out.println("   ✓ Runtime创建完成");
        System.out.println("   - 会话ID: " + runtime.getSessionId());
        System.out.println("   - 工作目录: " + runtime.getWorkDir());
        
        // 3. 加载AgentSpec
        System.out.println("\n3. 加载AgentSpec");
        Path agentFile = Path.of("src/test/resources/agents/test/agent.yaml");
        if (!Files.exists(agentFile)) {
            System.out.println("   ⚠️  测试文件不存在，跳过后续步骤");
            return;
        }
        
        ResolvedAgentSpec spec = AgentSpecLoader.loadAgentSpec(agentFile).block();
        System.out.println("   ✓ AgentSpec加载完成");
        System.out.println("   - Agent名称: " + spec.getName());
        System.out.println("   - 工具数量: " + spec.getTools().size());
        
        // 4. 加载Agent
        System.out.println("\n4. 加载Agent实例");
        Agent agent = AgentLoader.loadAgent(agentFile, runtime).block();
        System.out.println("   ✓ Agent加载完成");
        System.out.println("   - 系统提示词已加载");
        System.out.println("   - 参数已替换");
        System.out.println("   - 工具已配置");
        
        // 5. 验证Agent
        System.out.println("\n5. 验证Agent完整性");
        boolean nameValid = agent.getName() != null && !agent.getName().isEmpty();
        boolean promptValid = agent.getSystemPrompt() != null && !agent.getSystemPrompt().isEmpty();
        boolean toolsValid = agent.getTools() != null && !agent.getTools().isEmpty();
        
        System.out.println("   ✓ 名称有效: " + nameValid);
        System.out.println("   ✓ 提示词有效: " + promptValid);
        System.out.println("   ✓ 工具有效: " + toolsValid);
        
        Assertions.assertTrue(nameValid);
        Assertions.assertTrue(promptValid);
        Assertions.assertTrue(toolsValid);
        
        System.out.println("\n✅ 演示6完成 - Agent加载流程验证通过\n");
    }
}
