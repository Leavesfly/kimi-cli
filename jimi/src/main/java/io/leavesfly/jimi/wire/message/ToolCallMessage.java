package io.leavesfly.jimi.wire.message;

import io.leavesfly.jimi.llm.message.ToolCall;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 工具调用消息
 * 用于在 Wire 中传递工具调用信息
 */
@Data
@AllArgsConstructor
public class ToolCallMessage implements WireMessage {
    
    /**
     * 工具调用对象
     */
    private ToolCall toolCall;
    
    @Override
    public String getMessageType() {
        return "tool_call";
    }
}
