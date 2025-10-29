package io.leavesfly.jimi.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM包装类
 * 包含ChatProvider和最大上下文大小
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLM {
    
    /**
     * Chat Provider实例
     */
    private ChatProvider chatProvider;
    
    /**
     * 最大上下文大小（Token数）
     */
    private int maxContextSize;
    
    /**
     * 获取模型名称
     */
    public String getModelName() {
        return chatProvider != null ? chatProvider.getModelName() : "unknown";
    }
}
