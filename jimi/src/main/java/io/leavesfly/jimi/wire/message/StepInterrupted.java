package io.leavesfly.jimi.wire.message;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 步骤中断消息
 */
@Data
@NoArgsConstructor
public class StepInterrupted implements WireMessage {
    
    @Override
    public String getMessageType() {
        return "StepInterrupted";
    }
}
