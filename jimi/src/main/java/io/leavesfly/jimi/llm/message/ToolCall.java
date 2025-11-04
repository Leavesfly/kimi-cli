package io.leavesfly.jimi.llm.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具调用
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCall {
    
    /**
     * 工具调用唯一 ID
     */
    @JsonProperty("id")
    private String id;
    
    /**
     * 类型（通常是 "function"）
     */
    @JsonProperty("type")
    @Builder.Default
    private String type = "function";
    
    /**
     * 函数调用信息
     */
    @JsonProperty("function")
    private FunctionCall function;
}
