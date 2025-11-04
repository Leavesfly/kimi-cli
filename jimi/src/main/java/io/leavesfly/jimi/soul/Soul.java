package io.leavesfly.jimi.soul;

import io.leavesfly.jimi.llm.message.ContentPart;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Soul 接口
 * 定义 Agent 的核心行为
 */
public interface Soul {
    
    /**
     * 获取 Agent 名称
     */
    String getName();
    
    /**
     * 获取使用的模型名称
     */
    String getModel();
    
    /**
     * 获取当前状态快照
     */
    Map<String, Object> getStatus();
    
    /**
     * 运行 Agent（文本输入）
     * 
     * @param userInput 用户输入文本
     * @return 完成的 Mono
     */
    Mono<Void> run(String userInput);
    
    /**
     * 运行 Agent（多部分内容输入）
     * 
     * @param userInput 用户输入内容部分列表
     * @return 完成的 Mono
     */
    Mono<Void> run(List<ContentPart> userInput);
}
