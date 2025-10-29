package io.leavesfly.jimi.wire.message;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上下文压缩开始消息
 */
@Data
@NoArgsConstructor
public class CompactionBegin implements WireMessage {
    
    @Override
    public String getMessageType() {
        return "CompactionBegin";
    }
}
