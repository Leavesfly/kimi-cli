package io.leavesfly.jimi.soul.compaction;

import io.leavesfly.jimi.llm.LLM;
import io.leavesfly.jimi.soul.message.Message;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 上下文压缩接口
 * 定义消息压缩的标准行为
 */
public interface Compaction {
    
    /**
     * 压缩消息序列
     * 
     * @param messages 要压缩的消息列表
     * @param llm LLM 实例用于执行压缩
     * @return 压缩后的消息列表
     */
    Mono<List<Message>> compact(List<Message> messages, LLM llm);
}
