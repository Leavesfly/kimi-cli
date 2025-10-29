package io.leavesfly.jimi.soul.denwarenji;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * D-Mail 消息
 * 时间旅行机制的消息载体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DMail {
    
    /**
     * 目标检查点 ID
     */
    private int checkpointId;
    
    /**
     * D-Mail 消息内容
     */
    private String message;
}
