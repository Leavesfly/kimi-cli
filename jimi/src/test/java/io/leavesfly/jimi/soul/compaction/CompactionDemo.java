package io.leavesfly.jimi.soul.compaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.leavesfly.jimi.llm.LLM;
import io.leavesfly.jimi.llm.MockChatProvider;
import io.leavesfly.jimi.llm.message.Message;
import io.leavesfly.jimi.llm.message.MessageRole;

import java.util.ArrayList;
import java.util.List;

/**
 * 上下文压缩演示
 * 展示 SimpleCompaction 的工作原理和效果
 */
public class CompactionDemo {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("上下文压缩机制演示");
        System.out.println("=".repeat(80));
        System.out.println();
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        // 运行所有演示
        demo1_BasicCompaction();
        demo2_PreserveRecentMessages();
        demo3_LongConversation();
        demo4_CompactionWithToolCalls();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("所有演示完成！");
        System.out.println("=".repeat(80));
    }
    
    /**
     * 演示1：基本压缩功能
     */
    private static void demo1_BasicCompaction() {
        System.out.println("\n=== 演示1：基本压缩功能 ===\n");
        
        // 创建模拟的历史消息
        List<Message> messages = new ArrayList<>();
        messages.add(Message.user("你好，我想学习Java"));
        messages.add(Message.assistant("你好！我很乐意帮助你学习Java。Java是一门强大的编程语言..."));
        messages.add(Message.user("Java有哪些特点？"));
        messages.add(Message.assistant("Java的主要特点包括：1. 面向对象..."));
        messages.add(Message.user("能举个例子吗？"));
        messages.add(Message.assistant("当然可以！这里是一个简单的例子..."));
        
        System.out.println("原始消息数量: " + messages.size());
        
        // 创建模拟的 LLM
        MockChatProvider provider = new MockChatProvider("test-model");
        provider.addTextResponse("用户想学习Java。已经介绍了Java的特点和示例代码。");
        
        LLM llm = LLM.builder()
            .chatProvider(provider)
            .maxContextSize(100000)
            .build();
        
        // 创建压缩器
        SimpleCompaction compaction = new SimpleCompaction();
        
        // 执行压缩
        List<Message> compacted = compaction.compact(messages, llm).block();
        
        System.out.println("压缩后消息数量: " + compacted.size());
        System.out.println("\n压缩后的消息:");
        for (int i = 0; i < compacted.size(); i++) {
            Message msg = compacted.get(i);
            System.out.println(String.format("[%d] %s: %s", 
                i + 1, 
                msg.getRole().getValue(), 
                truncate(msg.getTextContent(), 80)));
        }
        
        System.out.println("\n✅ 演示1完成\n");
    }
    
    /**
     * 演示2：保留最近消息
     */
    private static void demo2_PreserveRecentMessages() {
        System.out.println("\n=== 演示2：保留最近消息 ===\n");
        
        // 创建历史消息（10条）
        List<Message> messages = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            messages.add(Message.user("用户问题 " + i));
            messages.add(Message.assistant("助手回答 " + i));
        }
        
        System.out.println("总消息数: " + messages.size());
        
        // 模拟 LLM
        MockChatProvider provider = new MockChatProvider("test-model");
        provider.addTextResponse("前面的对话涉及多个问题和回答");
        
        LLM llm = LLM.builder()
            .chatProvider(provider)
            .maxContextSize(100000)
            .build();
        
        // 压缩
        SimpleCompaction compaction = new SimpleCompaction();
        List<Message> compacted = compaction.compact(messages, llm).block();
        
        System.out.println("压缩后消息数: " + compacted.size());
        System.out.println("\n保留的最近消息:");
        // 最近2条应该被保留（问题5和回答5）
        for (int i = Math.max(0, compacted.size() - 2); i < compacted.size(); i++) {
            Message msg = compacted.get(i);
            System.out.println(String.format("  %s: %s", 
                msg.getRole().getValue(), 
                msg.getTextContent()));
        }
        
        System.out.println("\n✅ 演示2完成\n");
    }
    
    /**
     * 演示3：长对话压缩
     */
    private static void demo3_LongConversation() {
        System.out.println("\n=== 演示3：长对话压缩 ===\n");
        
        // 创建长对话（20轮）
        List<Message> messages = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            messages.add(Message.user("这是第" + i + "个问题"));
            messages.add(Message.assistant("这是第" + i + "个回答"));
        }
        
        System.out.println("原始消息: " + messages.size() + "条");
        
        // 计算压缩比例
        int originalSize = messages.size();
        
        // 模拟 LLM
        MockChatProvider provider = new MockChatProvider("test-model");
        provider.addTextResponse("用户进行了20轮对话，涉及多个主题");
        
        LLM llm = LLM.builder()
            .chatProvider(provider)
            .maxContextSize(100000)
            .build();
        
        // 压缩
        SimpleCompaction compaction = new SimpleCompaction();
        List<Message> compacted = compaction.compact(messages, llm).block();
        
        int compactedSize = compacted.size();
        double ratio = (1.0 - (double)compactedSize / originalSize) * 100;
        
        System.out.println("压缩后消息: " + compactedSize + "条");
        System.out.println(String.format("压缩比例: %.1f%%", ratio));
        
        System.out.println("\n✅ 演示3完成\n");
    }
    
    /**
     * 演示4：包含工具调用的压缩
     */
    private static void demo4_CompactionWithToolCalls() {
        System.out.println("\n=== 演示4：包含工具调用的压缩 ===\n");
        
        // 创建包含工具调用的消息
        List<Message> messages = new ArrayList<>();
        messages.add(Message.user("帮我读取文件内容"));
        messages.add(Message.assistant("好的，我来读取文件"));
        messages.add(Message.tool("文件内容：Hello World", "tool-1"));
        messages.add(Message.assistant("文件内容是：Hello World"));
        messages.add(Message.user("现在帮我分析这个内容"));
        messages.add(Message.assistant("这是一个简单的问候语"));
        
        System.out.println("消息类型统计:");
        long userCount = messages.stream().filter(m -> m.getRole() == MessageRole.USER).count();
        long assistantCount = messages.stream().filter(m -> m.getRole() == MessageRole.ASSISTANT).count();
        long toolCount = messages.stream().filter(m -> m.getRole() == MessageRole.TOOL).count();
        
        System.out.println("  用户消息: " + userCount);
        System.out.println("  助手消息: " + assistantCount);
        System.out.println("  工具消息: " + toolCount);
        
        // 模拟 LLM
        MockChatProvider provider = new MockChatProvider("test-model");
        provider.addTextResponse("用户请求读取文件，内容为Hello World，然后分析了内容");
        
        LLM llm = LLM.builder()
            .chatProvider(provider)
            .maxContextSize(100000)
            .build();
        
        // 压缩
        SimpleCompaction compaction = new SimpleCompaction();
        List<Message> compacted = compaction.compact(messages, llm).block();
        
        System.out.println("\n压缩后消息数: " + compacted.size());
        System.out.println("压缩说明: 工具消息被包含在上下文总结中，保留最近的用户-助手对话");
        
        System.out.println("\n✅ 演示4完成\n");
    }
    
    /**
     * 截断文本用于显示
     */
    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}
