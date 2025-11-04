package io.leavesfly.jimi.soul.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.leavesfly.jimi.llm.message.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 消息模型演示测试
 * 展示如何使用消息模型的各种功能
 */
@DisplayName("消息模型演示")
public class MessageModelDemo {
    
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    
    @Test
    @DisplayName("演示1：创建简单的用户消息")
    void demo1_simpleUserMessage() throws Exception {
        // 创建一个简单的用户文本消息
        Message message = Message.user("你好，请帮我分析一下这段代码");
        
        System.out.println("=== 演示1：简单用户消息 ===");
        System.out.println(objectMapper.writeValueAsString(message));
        System.out.println();
    }
    
    @Test
    @DisplayName("演示2：创建包含多部分内容的用户消息")
    void demo2_multiPartUserMessage() throws Exception {
        // 创建包含文本和图片的复杂消息
        Message message = Message.user(List.of(
            TextPart.of("这是什么动物？"),
            ImagePart.of("https://example.com/cat.jpg")
        ));
        
        System.out.println("=== 演示2：多部分用户消息 ===");
        System.out.println(objectMapper.writeValueAsString(message));
        System.out.println();
    }
    
    @Test
    @DisplayName("演示3：创建助手消息")
    void demo3_assistantMessage() throws Exception {
        // 创建助手回复消息
        Message message = Message.assistant("我可以帮你分析代码。请粘贴代码片段。");
        
        System.out.println("=== 演示3：助手消息 ===");
        System.out.println(objectMapper.writeValueAsString(message));
        System.out.println();
    }
    
    @Test
    @DisplayName("演示4：创建带工具调用的助手消息")
    void demo4_assistantWithToolCall() throws Exception {
        // 创建工具调用
        FunctionCall functionCall = FunctionCall.builder()
                .name("read_file")
                .arguments("{\"path\": \"/home/user/code.py\"}")
                .build();
        
        ToolCall toolCall = ToolCall.builder()
                .id("call_123")
                .function(functionCall)
                .build();
        
        // 创建带工具调用的助手消息
        Message message = Message.assistant(
            "让我先读取这个文件",
            List.of(toolCall)
        );
        
        System.out.println("=== 演示4：带工具调用的助手消息 ===");
        System.out.println(objectMapper.writeValueAsString(message));
        System.out.println();
    }
    
    @Test
    @DisplayName("演示5：创建工具返回消息")
    void demo5_toolMessage() throws Exception {
        // 创建工具执行结果消息
        Message message = Message.tool(
            "call_123",
            "文件内容：\ndef hello():\n    print('Hello, World!')"
        );
        
        System.out.println("=== 演示5：工具返回消息 ===");
        System.out.println(objectMapper.writeValueAsString(message));
        System.out.println();
    }
    
    @Test
    @DisplayName("演示6：创建系统消息")
    void demo6_systemMessage() throws Exception {
        // 创建系统提示消息
        Message message = Message.system(
            "你是一个专业的代码分析助手。请仔细分析用户提供的代码，" +
            "给出详细的解释和改进建议。"
        );
        
        System.out.println("=== 演示6：系统消息 ===");
        System.out.println(objectMapper.writeValueAsString(message));
        System.out.println();
    }
    
    @Test
    @DisplayName("演示7：完整对话流程")
    void demo7_completeConversation() throws Exception {
        System.out.println("=== 演示7：完整对话流程 ===\n");
        
        // 1. 系统提示
        Message systemMsg = Message.system("你是一个有帮助的AI助手");
        System.out.println("1. 系统消息：");
        System.out.println(objectMapper.writeValueAsString(systemMsg));
        System.out.println();
        
        // 2. 用户请求
        Message userMsg = Message.user("帮我创建一个Python文件");
        System.out.println("2. 用户消息：");
        System.out.println(objectMapper.writeValueAsString(userMsg));
        System.out.println();
        
        // 3. 助手调用工具
        FunctionCall writeFileCall = FunctionCall.builder()
                .name("write_file")
                .arguments("{\"path\": \"hello.py\", \"content\": \"print('Hello!')\"}")
                .build();
        
        Message assistantMsg1 = Message.assistant(
            "好的，我来创建文件",
            List.of(ToolCall.builder()
                    .id("call_write_1")
                    .function(writeFileCall)
                    .build())
        );
        System.out.println("3. 助手工具调用：");
        System.out.println(objectMapper.writeValueAsString(assistantMsg1));
        System.out.println();
        
        // 4. 工具执行结果
        Message toolMsg = Message.tool("call_write_1", "文件创建成功: hello.py");
        System.out.println("4. 工具执行结果：");
        System.out.println(objectMapper.writeValueAsString(toolMsg));
        System.out.println();
        
        // 5. 助手最终回复
        Message assistantMsg2 = Message.assistant("已成功创建文件 hello.py！");
        System.out.println("5. 助手最终回复：");
        System.out.println(objectMapper.writeValueAsString(assistantMsg2));
        System.out.println();
    }
    
    @Test
    @DisplayName("演示8：使用Builder模式自定义消息")
    void demo8_builderPattern() throws Exception {
        // 使用Builder模式构建复杂消息
        Message message = Message.builder()
                .role(MessageRole.ASSISTANT)
                .content(List.of(
                    TextPart.of("分析结果："),
                    TextPart.of("这段代码实现了一个简单的问候函数")
                ))
                .name("code_analyzer")
                .build();
        
        System.out.println("=== 演示8：Builder模式构建消息 ===");
        System.out.println(objectMapper.writeValueAsString(message));
        System.out.println();
    }
    
    @Test
    @DisplayName("演示9：提取消息中的文本内容")
    void demo9_extractTextContent() throws Exception {
        // 创建混合内容消息
        Message message = Message.user(List.of(
            TextPart.of("第一部分文本，"),
            ImagePart.of("https://example.com/image.png"),
            TextPart.of("第二部分文本")
        ));
        
        System.out.println("=== 演示9：提取文本内容 ===");
        System.out.println("完整消息：");
        System.out.println(objectMapper.writeValueAsString(message));
        System.out.println("\n提取的纯文本内容：");
        System.out.println(message.getTextContent());
        System.out.println();
    }
    
    @Test
    @DisplayName("演示10：JSON序列化和反序列化")
    void demo10_jsonSerialization() throws Exception {
        // 创建原始消息
        Message original = Message.assistant(
            "让我读取文件",
            List.of(ToolCall.builder()
                    .id("call_001")
                    .function(FunctionCall.builder()
                            .name("read_file")
                            .arguments("{\"path\": \"test.txt\"}")
                            .build())
                    .build())
        );
        
        System.out.println("=== 演示10：JSON序列化和反序列化 ===");
        
        // 序列化为JSON
        String json = objectMapper.writeValueAsString(original);
        System.out.println("序列化后的JSON：");
        System.out.println(json);
        System.out.println();
        
        // 从JSON反序列化
        Message deserialized = objectMapper.readValue(json, Message.class);
        System.out.println("反序列化后的对象：");
        System.out.println(objectMapper.writeValueAsString(deserialized));
        System.out.println();
        
        // 验证内容一致
        System.out.println("验证：角色 = " + deserialized.getRole());
        System.out.println("验证：内容 = " + deserialized.getContent());
        System.out.println("验证：工具调用数量 = " + 
            (deserialized.getToolCalls() != null ? deserialized.getToolCalls().size() : 0));
        System.out.println();
    }
}
