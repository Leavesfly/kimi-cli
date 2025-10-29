package io.leavesfly.jimi.wire.message;

/**
 * Wire 消息基类
 * 所有通过 Wire 传递的消息都是此接口的实现
 */
public interface WireMessage {
    
    /**
     * 获取消息类型
     */
    String getMessageType();
}
