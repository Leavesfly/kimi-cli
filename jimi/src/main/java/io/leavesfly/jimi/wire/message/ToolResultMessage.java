package io.leavesfly.jimi.wire.message;

import io.leavesfly.jimi.tool.ToolResult;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 工具结果消息
 * 用于在 Wire 中传递工具执行结果
 */
@Data
@AllArgsConstructor
public class ToolResultMessage implements WireMessage {
    
    /**
     * 工具调用 ID
     */
    private String toolCallId;
    
    /**
     * 工具执行结果
     */
    private ToolResult toolResult;
    
    @Override
    public String getMessageType() {
        return "tool_result";
    }
}
