package io.leavesfly.jimi.llm;

import io.leavesfly.jimi.llm.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Chat Provider接口
 * 定义与LLM交互的核心接口
 */
public interface ChatProvider {
    
    /**
     * 获取模型名称
     */
    String getModelName();
    
    /**
     * 生成聊天完成（非流式）
     * 
     * @param systemPrompt 系统提示词
     * @param history 历史消息列表
     * @param tools 可用工具列表（JSON Schema格式）
     * @return ChatCompletionResult的Mono
     */
    Mono<ChatCompletionResult> generate(
        String systemPrompt,
        List<Message> history,
        List<Object> tools
    );
    
    /**
     * 生成聊天完成（流式）
     * 
     * @param systemPrompt 系统提示词
     * @param history 历史消息列表
     * @param tools 可用工具列表
     * @return ChatCompletionChunk的Flux流
     */
    Flux<ChatCompletionChunk> generateStream(
        String systemPrompt,
        List<Message> history,
        List<Object> tools
    );
}
