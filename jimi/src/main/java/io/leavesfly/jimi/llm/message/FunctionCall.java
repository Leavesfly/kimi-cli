package io.leavesfly.jimi.llm.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 函数调用信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionCall {
    
    /**
     * 工具名称
     */
    @JsonProperty("name")
    private String name;
    
    /**
     * JSON 格式的参数
     */
    @JsonProperty("arguments")
    private String arguments;
}
