package io.leavesfly.jimi.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM 模型配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMModelConfig {
    
    /**
     * 提供商名称
     */
    @JsonProperty("provider")
    private String provider;
    
    /**
     * 模型名称
     */
    @JsonProperty("model")
    private String model;
    
    /**
     * 最大上下文大小（Token 数）
     */
    @JsonProperty("max_context_size")
    private int maxContextSize;
}
