package io.leavesfly.jimi.wire.message;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上下文压缩结束消息
 */
@Data
@NoArgsConstructor
public class CompactionEnd implements WireMessage {
    
    @Override
    public String getMessageType() {
        return "CompactionEnd";
    }
}
