package io.leavesfly.jimi.ui.shell;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.leavesfly.jimi.config.JimiConfig;
import io.leavesfly.jimi.config.LoopControlConfig;
import io.leavesfly.jimi.llm.LLM;
import io.leavesfly.jimi.llm.MockChatProvider;
import io.leavesfly.jimi.session.Session;
import io.leavesfly.jimi.soul.JimiSoul;
import io.leavesfly.jimi.soul.agent.Agent;
import io.leavesfly.jimi.soul.context.Context;
import io.leavesfly.jimi.soul.runtime.Runtime;
import io.leavesfly.jimi.tool.ToolRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Shell UI 演示程序
 * 展示 JLine 交互式界面的功能
 */
public class ShellUIDemo {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("Shell UI 演示程序");
        System.out.println("=".repeat(80));
        System.out.println();
        
        try {
            // 运行演示
            demo1_BasicShell();
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("演示完成！");
            System.out.println("=".repeat(80));
            
        } catch (Exception e) {
            System.err.println("演示失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 演示1：基础 Shell UI
     */
    private static void demo1_BasicShell() throws Exception {
        System.out.println("\n=== 演示1：基础 Shell UI ===\n");
        
        // 创建临时目录
        Path tempDir = Files.createTempDirectory("jimi-shell-demo");
        Path historyFile = tempDir.resolve("history.jsonl");
        
        try {
            // 创建配置
            JimiConfig config = JimiConfig.builder()
                .workDir(tempDir.toString())
                .defaultModel("moonshot-v1-8k")
                .loopControl(LoopControlConfig.builder()
                    .maxStepsPerRun(10)
                    .build())
                .build();
            
            // 创建会话
            Session session = Session.builder()
                .id("demo-session")
                .workDir(tempDir)
                .historyFile(historyFile)
                .build();
            
            // 创建模拟 LLM
            MockChatProvider chatProvider = new MockChatProvider("test-model");
            chatProvider.addTextResponse("你好！我是 Jimi，很高兴为你服务。");
            
            LLM llm = LLM.builder()
                .chatProvider(chatProvider)
                .maxContextSize(100000)
                .build();
            
            // 创建 Runtime
            Runtime runtime = Runtime.builder()
                .config(config)
                .llm(llm)
                .session(session)
                .yolo(false)
                .workDir(tempDir)
                .sessionId("demo-session")
                .build();
            
            // 创建 Agent
            Agent agent = Agent.builder()
                .name("Demo Agent")
                .systemPrompt("You are a helpful assistant.")
                .tools(new ArrayList<>())
                .build();
            
            // 创建 Context
            ObjectMapper objectMapper = new ObjectMapper();
            Context context = Context.builder()
                .historyFile(historyFile)
                .objectMapper(objectMapper)
                .history(new ArrayList<>())
                .checkpoints(new ArrayList<>())
                .tokenCount(0)
                .build();
            
            // 创建 ToolRegistry
            ToolRegistry toolRegistry = new ToolRegistry();
            
            // 创建 JimiSoul
            JimiSoul soul = new JimiSoul(
                agent,
                runtime,
                context,
                toolRegistry,
                objectMapper
            );
            
            System.out.println("创建 Shell UI...");
            
            // 创建 Shell UI
            try (ShellUI shell = new ShellUI(soul)) {
                System.out.println("✓ Shell UI 创建成功");
                System.out.println();
                
                printFeatures();
                printUsage();
                
                System.out.println("\n启动交互式 Shell（按 Ctrl-D 退出）：");
                System.out.println("-".repeat(80));
                
                // 运行 Shell（这会阻塞直到用户退出）
                Boolean result = shell.run().block();
                
                System.out.println("-".repeat(80));
                System.out.println("\n✓ Shell 运行完成，结果: " + result);
            }
            
        } finally {
            // 清理临时文件
            deleteDirectory(tempDir);
        }
        
        System.out.println("\n✅ 演示1完成\n");
    }
    
    /**
     * 打印功能特性
     */
    private static void printFeatures() {
        System.out.println("Shell UI 功能特性：");
        System.out.println("  • 富文本彩色输出");
        System.out.println("  • 命令历史记录（上下箭头）");
        System.out.println("  • Tab 自动补全");
        System.out.println("  • 语法高亮");
        System.out.println("  • Wire 消息实时显示");
        System.out.println("  • 元命令支持 (/help, /status, etc.)");
        System.out.println("  • Ctrl-C 中断，Ctrl-D 退出");
    }
    
    /**
     * 打印使用说明
     */
    private static void printUsage() {
        System.out.println("\n可用命令：");
        System.out.println("  /help     - 显示帮助信息");
        System.out.println("  /status   - 显示当前状态");
        System.out.println("  /clear    - 清屏");
        System.out.println("  /history  - 显示命令历史");
        System.out.println("  exit/quit - 退出程序");
        System.out.println("\n或者直接输入消息与 Jimi 对话！");
    }
    
    /**
     * 删除目录（递归）
     */
    private static void deleteDirectory(Path directory) {
        try {
            if (Files.exists(directory)) {
                Files.walk(directory)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            // Ignore
                        }
                    });
            }
        } catch (Exception e) {
            // Ignore
        }
    }
}
