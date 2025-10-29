package io.leavesfly.jimi.soul.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.leavesfly.jimi.config.*;
import io.leavesfly.jimi.session.Session;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Runtime（运行时上下文）演示测试
 * 展示Runtime的创建和使用
 */
@DisplayName("Runtime运行时上下文演示")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RuntimeDemo {
    
    @TempDir
    static Path tempDir;
    
    private static JimiConfig createTestConfig() {
        // 创建测试配置
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
    @DisplayName("演示1：创建基础Runtime")
    void demo1_createBasicRuntime() {
        System.out.println("\n=== 演示1：创建基础Runtime ===\n");
        
        // 准备配置和会话
        JimiConfig config = createTestConfig();
        Session session = Session.builder()
                .id("test-session-001")
                .workDir(tempDir)
                .build();
        
        // 创建Runtime
        Runtime runtime = Runtime.create(config, session, false).block();
        
        Assertions.assertNotNull(runtime);
        System.out.println("✓ Runtime创建成功");
        
        // 验证基本信息
        System.out.println("\nRuntime基本信息：");
        System.out.println("  会话ID: " + runtime.getSessionId());
        System.out.println("  工作目录: " + runtime.getWorkDir());
        System.out.println("  YOLO模式: " + runtime.isYoloMode());
        System.out.println("  最大步数: " + runtime.getConfig().getLoopControl().getMaxStepsPerRun());
        
        Assertions.assertEquals("test-session-001", runtime.getSessionId());
        Assertions.assertEquals(tempDir, runtime.getWorkDir());
        Assertions.assertFalse(runtime.isYoloMode());
        
        System.out.println("\n✅ 演示1完成\n");
    }
    
    @Test
    @Order(2)
    @DisplayName("演示2：内置系统提示词参数")
    void demo2_builtinSystemPromptArgs() {
        System.out.println("\n=== 演示2：内置系统提示词参数 ===\n");
        
        JimiConfig config = createTestConfig();
        Session session = Session.builder()
                .id("test-session-002")
                .workDir(tempDir)
                .build();
        
        Runtime runtime = Runtime.create(config, session, false).block();
        BuiltinSystemPromptArgs args = runtime.getBuiltinArgs();
        
        System.out.println("内置参数：");
        System.out.println("  KIMI_NOW: " + args.getKimiNow());
        System.out.println("  KIMI_WORK_DIR: " + args.getKimiWorkDir());
        System.out.println("  KIMI_AGENTS_MD长度: " + args.getKimiAgentsMd().length() + " 字符");
        
        // 工作目录列表
        String lsOutput = args.getKimiWorkDirLs();
        System.out.println("\n  KIMI_WORK_DIR_LS预览:");
        String[] lines = lsOutput.split("\n");
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            System.out.println("    " + lines[i]);
        }
        if (lines.length > 5) {
            System.out.println("    ... (" + (lines.length - 5) + " 行省略)");
        }
        
        // 验证时间格式
        Assertions.assertNotNull(args.getKimiNow());
        Assertions.assertTrue(args.getKimiNow().contains("T"));  // ISO 8601格式
        
        System.out.println("\n✅ 演示2完成\n");
    }
    
    @Test
    @Order(3)
    @DisplayName("演示3：YOLO模式")
    void demo3_yoloMode() {
        System.out.println("\n=== 演示3：YOLO模式 ===\n");
        
        JimiConfig config = createTestConfig();
        
        // 创建两个Runtime：一个YOLO模式，一个普通模式
        Session session1 = Session.builder()
                .id("session-yolo")
                .workDir(tempDir)
                .build();
        
        Session session2 = Session.builder()
                .id("session-normal")
                .workDir(tempDir)
                .build();
        
        Runtime yoloRuntime = Runtime.create(config, session1, true).block();
        Runtime normalRuntime = Runtime.create(config, session2, false).block();
        
        System.out.println("YOLO Runtime:");
        System.out.println("  YOLO模式: " + yoloRuntime.isYoloMode());
        System.out.println("  审批服务: " + (yoloRuntime.getApproval() != null ? "已创建" : "未创建"));
        
        System.out.println("\n普通 Runtime:");
        System.out.println("  YOLO模式: " + normalRuntime.isYoloMode());
        System.out.println("  审批服务: " + (normalRuntime.getApproval() != null ? "已创建" : "未创建"));
        
        Assertions.assertTrue(yoloRuntime.isYoloMode());
        Assertions.assertFalse(normalRuntime.isYoloMode());
        
        System.out.println("\n✅ 演示3完成\n");
    }
    
    @Test
    @Order(4)
    @DisplayName("演示4：加载AGENTS.md文件")
    void demo4_loadAgentsMd() throws Exception {
        System.out.println("\n=== 演示4：加载AGENTS.md文件 ===\n");
        
        // 创建AGENTS.md文件
        Path agentsMdPath = tempDir.resolve("AGENTS.md");
        String agentsMdContent = """
                # Agent规范
                
                ## 工具使用规范
                1. 文件操作前需要先读取
                2. 执行命令前需要确认
                
                ## 代码规范
                - 使用中文注释
                - 遵循最佳实践
                """;
        Files.writeString(agentsMdPath, agentsMdContent);
        System.out.println("✓ 创建AGENTS.md文件");
        
        // 创建Runtime
        JimiConfig config = createTestConfig();
        Session session = Session.builder()
                .id("test-session-004")
                .workDir(tempDir)
                .build();
        
        Runtime runtime = Runtime.create(config, session, false).block();
        String loadedContent = runtime.getBuiltinArgs().getKimiAgentsMd();
        
        System.out.println("\n加载的AGENTS.md内容：");
        System.out.println("─".repeat(60));
        System.out.println(loadedContent);
        System.out.println("─".repeat(60));
        
        Assertions.assertFalse(loadedContent.isEmpty());
        Assertions.assertTrue(loadedContent.contains("Agent规范"));
        Assertions.assertTrue(loadedContent.contains("工具使用规范"));
        
        System.out.println("\n✅ 演示4完成\n");
    }
    
    @Test
    @Order(5)
    @DisplayName("演示5：重新加载工作目录信息")
    void demo5_reloadWorkDirInfo() throws Exception {
        System.out.println("\n=== 演示5：重新加载工作目录信息 ===\n");
        
        JimiConfig config = createTestConfig();
        Session session = Session.builder()
                .id("test-session-005")
                .workDir(tempDir)
                .build();
        
        Runtime runtime = Runtime.create(config, session, false).block();
        
        // 获取初始信息
        String initialNow = runtime.getBuiltinArgs().getKimiNow();
        String initialAgentsMd = runtime.getBuiltinArgs().getKimiAgentsMd();
        
        System.out.println("初始状态：");
        System.out.println("  KIMI_NOW: " + initialNow);
        System.out.println("  AGENTS.md长度: " + initialAgentsMd.length());
        
        // 等待一下，确保时间不同
        Thread.sleep(100);
        
        // 创建新的AGENTS.md
        Path newAgentsMd = tempDir.resolve("AGENTS.md");
        Files.writeString(newAgentsMd, "# 新的Agent规范\n这是更新后的内容");
        System.out.println("\n✓ 创建新的AGENTS.md文件");
        
        // 重新加载
        runtime.reloadWorkDirInfo().block();
        System.out.println("✓ 重新加载工作目录信息");
        
        // 获取更新后的信息
        String updatedNow = runtime.getBuiltinArgs().getKimiNow();
        String updatedAgentsMd = runtime.getBuiltinArgs().getKimiAgentsMd();
        
        System.out.println("\n更新后状态：");
        System.out.println("  KIMI_NOW: " + updatedNow);
        System.out.println("  AGENTS.md长度: " + updatedAgentsMd.length());
        System.out.println("  AGENTS.md内容: " + updatedAgentsMd);
        
        // 验证时间已更新
        Assertions.assertNotEquals(initialNow, updatedNow);
        // 验证AGENTS.md已更新
        Assertions.assertTrue(updatedAgentsMd.contains("新的Agent规范"));
        
        System.out.println("\n✅ 演示5完成\n");
    }
    
    @Test
    @Order(6)
    @DisplayName("演示6：D-Mail和审批服务集成")
    void demo6_denwaRenjiAndApproval() {
        System.out.println("\n=== 演示6：D-Mail和审批服务集成 ===\n");
        
        JimiConfig config = createTestConfig();
        Session session = Session.builder()
                .id("test-session-006")
                .workDir(tempDir)
                .build();
        
        Runtime runtime = Runtime.create(config, session, false).block();
        
        // 检查组件
        System.out.println("Runtime组件检查：");
        System.out.println("  DenwaRenji: " + (runtime.getDenwaRenji() != null ? "✓ 已创建" : "✗ 未创建"));
        System.out.println("  Approval: " + (runtime.getApproval() != null ? "✓ 已创建" : "✗ 未创建"));
        
        // 验证D-Mail
        Assertions.assertNotNull(runtime.getDenwaRenji());
        
        // 验证审批服务
        Assertions.assertNotNull(runtime.getApproval());
        Assertions.assertEquals(0, runtime.getApproval().getSessionApprovalCount());
        
        System.out.println("\n审批服务状态：");
        System.out.println("  YOLO模式: " + runtime.getApproval().isYolo());
        System.out.println("  会话级批准数: " + runtime.getApproval().getSessionApprovalCount());
        
        System.out.println("\n✅ 演示6完成\n");
    }
    
    @Test
    @Order(7)
    @DisplayName("演示7：配置访问")
    void demo7_configAccess() {
        System.out.println("\n=== 演示7：配置访问 ===\n");
        
        JimiConfig config = createTestConfig();
        Session session = Session.builder()
                .id("test-session-007")
                .workDir(tempDir)
                .build();
        
        Runtime runtime = Runtime.create(config, session, false).block();
        
        // 访问各种配置
        System.out.println("配置信息：");
        System.out.println("  LLM提供商数量: " + runtime.getConfig().getProviders().size());
        System.out.println("  LLM模型数量: " + runtime.getConfig().getModels().size());
        
        LoopControlConfig loopControl = runtime.getConfig().getLoopControl();
        System.out.println("\n循环控制配置：");
        System.out.println("  最大步数: " + loopControl.getMaxStepsPerRun());
        
        LLMModelConfig model = runtime.getConfig().getModels().get("default");
        System.out.println("\n默认LLM模型：");
        System.out.println("  提供商: " + model.getProvider());
        System.out.println("  模型名: " + model.getModel());
        System.out.println("  上下文大小: " + model.getMaxContextSize());
        
        Assertions.assertEquals(50, loopControl.getMaxStepsPerRun());
        Assertions.assertEquals("moonshot-v1-8k", model.getModel());
        
        System.out.println("\n✅ 演示7完成\n");
    }
    
    @Test
    @Order(8)
    @DisplayName("演示8：完整Runtime生命周期")
    void demo8_completeLifecycle() throws Exception {
        System.out.println("\n=== 演示8：完整Runtime生命周期 ===\n");
        
        // 1. 准备阶段
        System.out.println("1. 准备阶段");
        JimiConfig config = createTestConfig();
        Session session = Session.builder()
                .id("lifecycle-session")
                .workDir(tempDir)
                .build();
        
        Path agentsMd = tempDir.resolve("AGENTS.md");
        Files.writeString(agentsMd, "# Initial Agents");
        System.out.println("   ✓ 配置准备完成");
        System.out.println("   ✓ 会话创建完成");
        System.out.println("   ✓ AGENTS.md创建完成");
        
        // 2. 创建Runtime
        System.out.println("\n2. 创建Runtime");
        Runtime runtime = Runtime.create(config, session, false).block();
        System.out.println("   ✓ Runtime创建成功");
        System.out.println("   - 会话ID: " + runtime.getSessionId());
        System.out.println("   - 工作目录: " + runtime.getWorkDir());
        
        // 3. 使用阶段
        System.out.println("\n3. 使用阶段");
        
        // 检查初始状态
        String initialTime = runtime.getBuiltinArgs().getKimiNow();
        System.out.println("   初始时间: " + initialTime);
        
        // 模拟工作目录变化
        Thread.sleep(100);
        Files.writeString(tempDir.resolve("new_file.txt"), "New content");
        
        // 重新加载
        runtime.reloadWorkDirInfo().block();
        String updatedTime = runtime.getBuiltinArgs().getKimiNow();
        System.out.println("   更新时间: " + updatedTime);
        System.out.println("   ✓ 工作目录信息已更新");
        
        // 4. 状态检查
        System.out.println("\n4. 最终状态检查");
        System.out.println("   - YOLO模式: " + runtime.isYoloMode());
        System.out.println("   - DenwaRenji: " + (runtime.getDenwaRenji() != null ? "有效" : "无效"));
        System.out.println("   - Approval: " + (runtime.getApproval() != null ? "有效" : "无效"));
        System.out.println("   - 配置: " + (runtime.getConfig() != null ? "有效" : "无效"));
        
        // 验证
        Assertions.assertNotNull(runtime);
        Assertions.assertNotEquals(initialTime, updatedTime);
        Assertions.assertNotNull(runtime.getDenwaRenji());
        Assertions.assertNotNull(runtime.getApproval());
        
        System.out.println("\n✅ 演示8完成 - Runtime生命周期验证通过\n");
    }
}
