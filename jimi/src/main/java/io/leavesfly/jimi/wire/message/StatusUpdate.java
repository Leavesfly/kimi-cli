package io.leavesfly.jimi.wire.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 状态更新消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdate implements WireMessage {
    
    /**
     * 状态信息
     */
    private Map<String, Object> status;
    
    @Override
    public String getMessageType() {
        return "StatusUpdate";
    }
}
