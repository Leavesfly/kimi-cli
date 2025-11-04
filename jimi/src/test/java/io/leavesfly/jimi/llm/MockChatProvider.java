package io.leavesfly.jimi.llm;

import io.leavesfly.jimi.llm.message.FunctionCall;
import io.leavesfly.jimi.llm.message.Message;
import io.leavesfly.jimi.llm.message.ToolCall;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟的 ChatProvider
 * 用于测试，返回预设的响应
 */
public class MockChatProvider implements ChatProvider {
    
    private final String modelName;
    private final List<Message> responses;
    private int currentIndex = 0;
    
    public MockChatProvider(String modelName) {
        this.modelName = modelName;
        this.responses = new ArrayList<>();
    }
    
    /**
     * 添加预设响应
     */
    public void addResponse(Message message) {
        responses.add(message);
    }
    
    /**
     * 添加简单文本响应
     */
    public void addTextResponse(String text) {
        responses.add(Message.assistant(text));
    }
    
    /**
     * 添加带工具调用的响应
     */
    public void addToolCallResponse(String toolName, String arguments) {
        ToolCall toolCall = ToolCall.builder()
            .id("call_" + System.currentTimeMillis())
            .type("function")
            .function(FunctionCall.builder()
                .name(toolName)
                .arguments(arguments)
                .build())
            .build();
        
        responses.add(Message.assistant("", List.of(toolCall)));
    }
    
    @Override
    public String getModelName() {
        return modelName;
    }
    
    @Override
    public Mono<ChatCompletionResult> generate(
        String systemPrompt,
        List<Message> history,
        List<Object> tools
    ) {
        if (currentIndex >= responses.size()) {
            // 默认返回结束响应
            return Mono.just(ChatCompletionResult.builder()
                .message(Message.assistant("对话已完成。"))
                .usage(ChatCompletionResult.Usage.builder()
                    .promptTokens(100)
                    .completionTokens(50)
                    .totalTokens(150)
                    .build())
                .build());
        }
        
        Message response = responses.get(currentIndex++);
        
        return Mono.just(ChatCompletionResult.builder()
            .message(response)
            .usage(ChatCompletionResult.Usage.builder()
                .promptTokens(100)
                .completionTokens(50)
                .totalTokens(150)
                .build())
            .build());
    }
    
    @Override
    public Flux<ChatCompletionChunk> generateStream(
        String systemPrompt,
        List<Message> history,
        List<Object> tools
    ) {
        // 简化实现：不支持流式
        return Flux.empty();
    }
    
    /**
     * 重置响应索引
     */
    public void reset() {
        currentIndex = 0;
    }
}
