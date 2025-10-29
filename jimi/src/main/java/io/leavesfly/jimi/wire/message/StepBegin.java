package io.leavesfly.jimi.wire.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 步骤开始消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepBegin implements WireMessage {
    
    /**
     * 步骤编号
     */
    private int stepNumber;
    
    @Override
    public String getMessageType() {
        return "StepBegin";
    }
}
