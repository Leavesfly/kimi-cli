package io.leavesfly.jimi.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

/**
 * 子Agent规范配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubagentSpec {
    
    /**
     * 子Agent文件路径（相对路径）
     */
    @JsonProperty("path")
    private Path path;
    
    /**
     * 子Agent描述
     */
    @JsonProperty("description")
    private String description;
}
