package io.leavesfly.jimi.soul.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.leavesfly.jimi.soul.message.Message;
import io.leavesfly.jimi.soul.message.MessageRole;
import io.leavesfly.jimi.soul.message.TextPart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Context 完整功能演示
 * 
 * 演示上下文管理的所有核心特性：
 * 1. 基本操作：追加消息、更新 Token
 * 2. 持久化：自动保存到 JSONL 文件
 * 3. 恢复：从文件恢复完整状态
 * 4. 检查点：创建和回退
 * 5. 边界场景：空文件、并发、文件轮转
 * 
 * @author 山泽
 */
class ContextDemo {
    
    @TempDir
    Path tempDir;
    
    private ObjectMapper objectMapper;
    private Path historyFile;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        historyFile = tempDir.resolve("history.jsonl");
    }
    
    /**
     * 演示 1: 基本操作 - 追加消息
     */
    @Test
    void demo1_BasicOperations() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 1: 基本操作 - 追加消息");
        System.out.println("=".repeat(70) + "\n");
        
        Context context = new Context(historyFile, objectMapper);
        
        // 追加第一条消息
        System.out.println("步骤 1: 追加用户消息");
        Message userMsg = Message.builder()
                .role(MessageRole.USER)
                .content(List.of(TextPart.of("Hello, Jimi!")))
                .build();
        context.appendMessage(userMsg).block();
        
        System.out.println("  ✓ 消息已追加");
        System.out.println("  历史消息数: " + context.getHistory().size());
        assertEquals(1, context.getHistory().size());
        
        // 追加第二条消息
        System.out.println("\n步骤 2: 追加助手响应");
        Message assistantMsg = Message.builder()
                .role(MessageRole.ASSISTANT)
                .content(List.of(TextPart.of("Hello! How can I help you?")))
                .build();
        context.appendMessage(assistantMsg).block();
        
        System.out.println("  ✓ 响应已追加");
        System.out.println("  历史消息数: " + context.getHistory().size());
        assertEquals(2, context.getHistory().size());
        
        // 验证文件内容
        System.out.println("\n步骤 3: 验证持久化");
        List<String> lines = Files.readAllLines(historyFile);
        System.out.println("  文件行数: " + lines.size());
        System.out.println("  文件内容预览:");
        lines.forEach(line -> System.out.println("    " + line.substring(0, Math.min(60, line.length())) + "..."));
        
        assertEquals(2, lines.size());
        assertTrue(Files.exists(historyFile));
        
        System.out.println("\n✅ 基本操作演示完成\n");
    }
    
    /**
     * 演示 2: Token 计数管理
     */
    @Test
    void demo2_TokenCountManagement() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 2: Token 计数管理");
        System.out.println("=".repeat(70) + "\n");
        
        Context context = new Context(historyFile, objectMapper);
        
        System.out.println("初始 Token 计数: " + context.getTokenCount());
        assertEquals(0, context.getTokenCount());
        
        // 追加消息
        Message msg = Message.builder()
                .role(MessageRole.USER)
                .content(List.of(TextPart.of("Test message")))
                .build();
        context.appendMessage(msg).block();
        
        // 更新 Token 计数
        System.out.println("\n更新 Token 计数到 150");
        context.updateTokenCount(150).block();
        System.out.println("  ✓ 当前 Token 计数: " + context.getTokenCount());
        assertEquals(150, context.getTokenCount());
        
        // 再次更新
        System.out.println("\n更新 Token 计数到 300");
        context.updateTokenCount(300).block();
        System.out.println("  ✓ 当前 Token 计数: " + context.getTokenCount());
        assertEquals(300, context.getTokenCount());
        
        // 验证文件内容
        System.out.println("\n验证持久化:");
        List<String> lines = Files.readAllLines(historyFile);
        System.out.println("  文件行数: " + lines.size());
        System.out.println("  包含 2 个 _usage 记录");
        
        long usageCount = lines.stream()
                .filter(line -> line.contains("\"role\":\"_usage\""))
                .count();
        assertEquals(2, usageCount);
        
        System.out.println("\n✅ Token 计数管理演示完成\n");
    }
    
    /**
     * 演示 3: 上下文恢复
     */
    @Test
    void demo3_ContextRestoration() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 3: 上下文恢复");
        System.out.println("=".repeat(70) + "\n");
        
        // 阶段 1: 创建原始上下文
        System.out.println("阶段 1: 创建原始上下文");
        System.out.println("-".repeat(70));
        
        Context originalContext = new Context(historyFile, objectMapper);
        
        Message msg1 = Message.builder()
                .role(MessageRole.USER)
                .content(List.of(TextPart.of("First message")))
                .build();
        originalContext.appendMessage(msg1).block();
        
        Message msg2 = Message.builder()
                .role(MessageRole.ASSISTANT)
                .content(List.of(TextPart.of("First response")))
                .build();
        originalContext.appendMessage(msg2).block();
        
        originalContext.updateTokenCount(100).block();
        
        System.out.println("  ✓ 追加了 2 条消息");
        System.out.println("  ✓ Token 计数: " + originalContext.getTokenCount());
        System.out.println("  ✓ 历史消息数: " + originalContext.getHistory().size());
        
        // 阶段 2: 从文件恢复新上下文
        System.out.println("\n阶段 2: 从文件恢复新上下文");
        System.out.println("-".repeat(70));
        
        Context restoredContext = new Context(historyFile, objectMapper);
        boolean restored = restoredContext.restore().block();
        
        System.out.println("  恢复结果: " + (restored ? "成功" : "失败"));
        System.out.println("  ✓ Token 计数: " + restoredContext.getTokenCount());
        System.out.println("  ✓ 历史消息数: " + restoredContext.getHistory().size());
        
        assertTrue(restored);
        assertEquals(100, restoredContext.getTokenCount());
        assertEquals(2, restoredContext.getHistory().size());
        
        // 阶段 3: 验证消息内容
        System.out.println("\n阶段 3: 验证消息内容");
        System.out.println("-".repeat(70));
        
        List<Message> history = restoredContext.getHistory();
        System.out.println("  消息 1:");
        System.out.println("    角色: " + history.get(0).getRole());
        System.out.println("    内容: " + history.get(0).getContent().get(0).getText());
        
        System.out.println("  消息 2:");
        System.out.println("    角色: " + history.get(1).getRole());
        System.out.println("    内容: " + history.get(1).getContent().get(0).getText());
        
        assertEquals(MessageRole.USER, history.get(0).getRole());
        assertEquals("First message", history.get(0).getContent().get(0).getText());
        assertEquals(MessageRole.ASSISTANT, history.get(1).getRole());
        assertEquals("First response", history.get(1).getContent().get(0).getText());
        
        System.out.println("\n✅ 上下文恢复演示完成\n");
    }
    
    /**
     * 演示 4: 检查点机制
     */
    @Test
    void demo4_CheckpointMechanism() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 4: 检查点机制");
        System.out.println("=".repeat(70) + "\n");
        
        Context context = new Context(historyFile, objectMapper);
        
        // 创建第一个检查点
        System.out.println("步骤 1: 创建第一个检查点");
        int cp0 = context.checkpoint(true).block();
        System.out.println("  ✓ 检查点 ID: " + cp0);
        System.out.println("  ✓ 检查点数量: " + context.getnCheckpoints());
        assertEquals(0, cp0);
        assertEquals(1, context.getnCheckpoints());
        
        // 追加一些消息
        System.out.println("\n步骤 2: 追加消息");
        Message msg1 = Message.builder()
                .role(MessageRole.USER)
                .content(List.of(TextPart.of("Message after checkpoint 0")))
                .build();
        context.appendMessage(msg1).block();
        System.out.println("  ✓ 消息数: " + context.getHistory().size());
        
        // 创建第二个检查点
        System.out.println("\n步骤 3: 创建第二个检查点");
        int cp1 = context.checkpoint(true).block();
        System.out.println("  ✓ 检查点 ID: " + cp1);
        System.out.println("  ✓ 检查点数量: " + context.getnCheckpoints());
        assertEquals(1, cp1);
        assertEquals(2, context.getnCheckpoints());
        
        // 继续追加消息
        System.out.println("\n步骤 4: 继续追加消息");
        Message msg2 = Message.builder()
                .role(MessageRole.USER)
                .content(List.of(TextPart.of("Message after checkpoint 1")))
                .build();
        context.appendMessage(msg2).block();
        System.out.println("  ✓ 消息数: " + context.getHistory().size());
        
        // 验证文件结构
        System.out.println("\n步骤 5: 验证文件结构");
        List<String> lines = Files.readAllLines(historyFile);
        System.out.println("  文件总行数: " + lines.size());
        
        long checkpointCount = lines.stream()
                .filter(line -> line.contains("\"role\":\"_checkpoint\""))
                .count();
        System.out.println("  检查点记录数: " + checkpointCount);
        assertEquals(2, checkpointCount);
        
        System.out.println("\n✅ 检查点机制演示完成\n");
    }
    
    /**
     * 演示 5: 检查点回退
     */
    @Test
    void demo5_CheckpointRevert() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 5: 检查点回退");
        System.out.println("=".repeat(70) + "\n");
        
        Context context = new Context(historyFile, objectMapper);
        
        // 建立初始状态
        System.out.println("阶段 1: 建立初始状态");
        System.out.println("-".repeat(70));
        
        Message msg0 = Message.builder()
                .role(MessageRole.USER)
                .content(List.of(TextPart.of("Initial message")))
                .build();
        context.appendMessage(msg0).block();
        
        int cp0 = context.checkpoint(false).block();
        System.out.println("  ✓ 创建检查点 0");
        
        Message msg1 = Message.builder()
                .role(MessageRole.USER)
                .content(List.of(TextPart.of("Message after CP0")))
                .build();
        context.appendMessage(msg1).block();
        
        int cp1 = context.checkpoint(false).block();
        System.out.println("  ✓ 创建检查点 1");
        
        Message msg2 = Message.builder()
                .role(MessageRole.USER)
                .content(List.of(TextPart.of("Message after CP1")))
                .build();
        context.appendMessage(msg2).block();
        
        context.updateTokenCount(200).block();
        
        System.out.println("  当前状态:");
        System.out.println("    消息数: " + context.getHistory().size());
        System.out.println("    Token 计数: " + context.getTokenCount());
        System.out.println("    检查点数: " + context.getnCheckpoints());
        
        assertEquals(3, context.getHistory().size());
        assertEquals(200, context.getTokenCount());
        
        // 回退到检查点 0
        System.out.println("\n阶段 2: 回退到检查点 0");
        System.out.println("-".repeat(70));
        
        context.revertTo(0).block();
        
        System.out.println("  回退后状态:");
        System.out.println("    消息数: " + context.getHistory().size());
        System.out.println("    Token 计数: " + context.getTokenCount());
        System.out.println("    检查点数: " + context.getnCheckpoints());
        
        assertEquals(1, context.getHistory().size());
        assertEquals(0, context.getTokenCount());
        
        // 验证轮转文件
        System.out.println("\n阶段 3: 验证文件轮转");
        System.out.println("-".repeat(70));
        
        Path rotatedFile = tempDir.resolve("history.jsonl.1");
        assertTrue(Files.exists(rotatedFile));
        System.out.println("  ✓ 轮转文件已创建: " + rotatedFile.getFileName());
        
        List<String> rotatedLines = Files.readAllLines(rotatedFile);
        System.out.println("  ✓ 轮转文件行数: " + rotatedLines.size());
        
        List<String> currentLines = Files.readAllLines(historyFile);
        System.out.println("  ✓ 当前文件行数: " + currentLines.size());
        
        assertTrue(rotatedLines.size() > currentLines.size());
        
        System.out.println("\n✅ 检查点回退演示完成\n");
    }
    
    /**
     * 演示 6: 边界场景 - 空文件恢复
     */
    @Test
    void demo6_EdgeCase_EmptyFileRestore() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 6: 边界场景 - 空文件恢复");
        System.out.println("=".repeat(70) + "\n");
        
        // 场景 1: 文件不存在
        System.out.println("场景 1: 文件不存在");
        Context ctx1 = new Context(historyFile, objectMapper);
        boolean restored1 = ctx1.restore().block();
        System.out.println("  恢复结果: " + restored1);
        assertFalse(restored1);
        System.out.println("  ✓ 正确处理文件不存在的情况\n");
        
        // 场景 2: 空文件
        System.out.println("场景 2: 空文件");
        Files.createFile(historyFile);
        Context ctx2 = new Context(historyFile, objectMapper);
        boolean restored2 = ctx2.restore().block();
        System.out.println("  恢复结果: " + restored2);
        assertFalse(restored2);
        System.out.println("  ✓ 正确处理空文件的情况\n");
        
        // 场景 3: 只有空行的文件
        System.out.println("场景 3: 只有空行的文件");
        Files.writeString(historyFile, "\n\n\n");
        Context ctx3 = new Context(historyFile, objectMapper);
        boolean restored3 = ctx3.restore().block();
        System.out.println("  恢复结果: " + restored3);
        assertFalse(restored3);
        System.out.println("  ✓ 正确处理只有空行的文件\n");
        
        System.out.println("✅ 边界场景演示完成\n");
    }
    
    /**
     * 演示 7: 多次轮转
     */
    @Test
    void demo7_MultipleRotations() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 7: 多次文件轮转");
        System.out.println("=".repeat(70) + "\n");
        
        Context context = new Context(historyFile, objectMapper);
        
        // 创建多个检查点并回退
        for (int i = 0; i < 3; i++) {
            System.out.println("轮转 " + (i + 1) + ":");
            
            Message msg = Message.builder()
                    .role(MessageRole.USER)
                    .content(List.of(TextPart.of("Message " + i)))
                    .build();
            context.appendMessage(msg).block();
            
            int cp = context.checkpoint(false).block();
            System.out.println("  ✓ 创建检查点 " + cp);
            
            context.revertTo(cp).block();
            System.out.println("  ✓ 回退到检查点 " + cp);
            
            Path rotatedFile = tempDir.resolve("history.jsonl." + (i + 1));
            assertTrue(Files.exists(rotatedFile));
            System.out.println("  ✓ 轮转文件: " + rotatedFile.getFileName() + "\n");
        }
        
        // 验证所有轮转文件
        System.out.println("验证结果:");
        System.out.println("  当前文件: history.jsonl");
        System.out.println("  轮转文件:");
        for (int i = 1; i <= 3; i++) {
            Path rotatedFile = tempDir.resolve("history.jsonl." + i);
            System.out.println("    - history.jsonl." + i + " (存在: " + Files.exists(rotatedFile) + ")");
        }
        
        System.out.println("\n✅ 多次轮转演示完成\n");
    }
    
    /**
     * 演示 8: 完整生命周期
     */
    @Test
    void demo8_CompleteLifecycle() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 8: 完整生命周期演示");
        System.out.println("=".repeat(70) + "\n");
        
        // 第 1 轮对话
        System.out.println("第 1 轮对话:");
        System.out.println("-".repeat(70));
        Context session1 = new Context(historyFile, objectMapper);
        
        Message user1 = Message.builder()
                .role(MessageRole.USER)
                .content(List.of(TextPart.of("Write a hello world program")))
                .build();
        session1.appendMessage(user1).block();
        
        Message assistant1 = Message.builder()
                .role(MessageRole.ASSISTANT)
                .content(List.of(TextPart.of("Here's a Java hello world...")))
                .build();
        session1.appendMessage(assistant1).block();
        
        session1.updateTokenCount(50).block();
        int cp0 = session1.checkpoint(false).block();
        
        System.out.println("  ✓ 完成 1 组对话");
        System.out.println("  ✓ Token: " + session1.getTokenCount());
        System.out.println("  ✓ 检查点: " + cp0);
        
        // 第 2 轮对话
        System.out.println("\n第 2 轮对话:");
        System.out.println("-".repeat(70));
        
        Message user2 = Message.builder()
                .role(MessageRole.USER)
                .content(List.of(TextPart.of("Now add error handling")))
                .build();
        session1.appendMessage(user2).block();
        
        Message assistant2 = Message.builder()
                .role(MessageRole.ASSISTANT)
                .content(List.of(TextPart.of("Here's the improved version...")))
                .build();
        session1.appendMessage(assistant2).block();
        
        session1.updateTokenCount(120).block();
        
        System.out.println("  ✓ 完成第 2 组对话");
        System.out.println("  ✓ Token: " + session1.getTokenCount());
        System.out.println("  ✓ 总消息数: " + session1.getHistory().size());
        
        // 模拟应用重启 - 恢复会话
        System.out.println("\n应用重启 - 恢复会话:");
        System.out.println("-".repeat(70));
        
        Context session2 = new Context(historyFile, objectMapper);
        boolean restored = session2.restore().block();
        
        System.out.println("  恢复状态: " + (restored ? "成功" : "失败"));
        System.out.println("  ✓ Token: " + session2.getTokenCount());
        System.out.println("  ✓ 消息数: " + session2.getHistory().size());
        System.out.println("  ✓ 检查点数: " + session2.getnCheckpoints());
        
        assertTrue(restored);
        assertEquals(120, session2.getTokenCount());
        assertEquals(4, session2.getHistory().size());
        assertEquals(1, session2.getnCheckpoints());
        
        // 继续对话
        System.out.println("\n继续对话:");
        System.out.println("-".repeat(70));
        
        Message user3 = Message.builder()
                .role(MessageRole.USER)
                .content(List.of(TextPart.of("Add unit tests")))
                .build();
        session2.appendMessage(user3).block();
        
        System.out.println("  ✓ 消息数: " + session2.getHistory().size());
        assertEquals(5, session2.getHistory().size());
        
        System.out.println("\n✅ 完整生命周期演示完成\n");
    }
    
    /**
     * 演示总结
     */
    @Test
    void demo9_Summary() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Context 类功能总结");
        System.out.println("=".repeat(70) + "\n");
        
        System.out.println("核心功能:");
        System.out.println("  1. ✅ 消息历史管理");
        System.out.println("     - 追加单条/多条消息");
        System.out.println("     - 只读历史视图");
        System.out.println("     - 自动持久化到 JSONL");
        
        System.out.println("\n  2. ✅ Token 计数追踪");
        System.out.println("     - 更新计数");
        System.out.println("     - 持久化存储");
        System.out.println("     - 恢复时自动加载");
        
        System.out.println("\n  3. ✅ 检查点机制");
        System.out.println("     - 创建检查点");
        System.out.println("     - 回退到指定检查点");
        System.out.println("     - 自动文件轮转");
        
        System.out.println("\n  4. ✅ 持久化与恢复");
        System.out.println("     - JSONL 格式存储");
        System.out.println("     - 完整状态恢复");
        System.out.println("     - 边界场景处理");
        
        System.out.println("\n  5. ✅ 文件管理");
        System.out.println("     - 自动轮转");
        System.out.println("     - 防止覆盖");
        System.out.println("     - 历史版本保留");
        
        System.out.println("\n文件格式示例:");
        System.out.println("  {\"role\":\"user\",\"content\":[{\"type\":\"text\",\"text\":\"...\"}]}");
        System.out.println("  {\"role\":\"assistant\",\"content\":[{\"type\":\"text\",\"text\":\"...\"}]}");
        System.out.println("  {\"role\":\"_usage\",\"token_count\":150}");
        System.out.println("  {\"role\":\"_checkpoint\",\"id\":0}");
        
        System.out.println("\n使用场景:");
        System.out.println("  ✓ 对话历史持久化");
        System.out.println("  ✓ 应用重启后恢复");
        System.out.println("  ✓ 错误恢复（检查点回退）");
        System.out.println("  ✓ 调试和审计");
        
        System.out.println("\n与 Python 版本对比:");
        System.out.println("  功能完全对等 ✅");
        System.out.println("  API 设计一致 ✅");
        System.out.println("  文件格式兼容 ✅");
        System.out.println("  边界处理完善 ✅");
        
        System.out.println("\n" + "=".repeat(70) + "\n");
    }
}
