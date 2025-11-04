package io.leavesfly.jimi.llm;

import io.leavesfly.jimi.llm.message.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Chat完成结果
 * 包含生成的消息和使用统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatCompletionResult {
    
    /**
     * 生成的消息
     */
    private Message message;
    
    /**
     * Token使用统计
     */
    private Usage usage;
    
    /**
     * Token使用统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        /**
         * 提示词Token数
         */
        private int promptTokens;
        
        /**
         * 完成Token数
         */
        private int completionTokens;
        
        /**
         * 总Token数
         */
        private int totalTokens;
    }
}
