package io.leavesfly.jimi.wire.message;

import io.leavesfly.jimi.llm.message.ContentPart;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 内容部分消息包装
 * 用于在 Wire 中传递 ContentPart（如 TextPart）
 */
@Data
@AllArgsConstructor
public class ContentPartMessage implements WireMessage {
    
    /**
     * 内容部分（可以是 TextPart、ImagePart 等）
     */
    private ContentPart contentPart;
    
    @Override
    public String getMessageType() {
        return "content_part";
    }
}
