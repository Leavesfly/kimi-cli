package io.leavesfly.jimi.wire;

import io.leavesfly.jimi.wire.message.WireMessage;
import reactor.core.publisher.Flux;

/**
 * Wire 消息总线接口
 * 用于 Soul 和 UI 之间的解耦通信
 */
public interface Wire {
    
    /**
     * 发送消息
     */
    void send(WireMessage message);
    
    /**
     * 获取消息流
     */
    Flux<WireMessage> asFlux();
    
    /**
     * 完成消息发送
     */
    void complete();
}
