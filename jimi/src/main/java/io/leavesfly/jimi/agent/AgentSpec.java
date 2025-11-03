package io.leavesfly.jimi.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent规范配置
 * 对应agent.yaml中的配置结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentSpec {
    
    /**
     * 继承的基础Agent文件路径
     * 可以是"default"或相对路径
     */
    @JsonProperty("extend")
    private String extend;
    
    /**
     * Agent名称
     */
    @JsonProperty("name")
    private String name;
    
    /**
     * 系统提示词文件路径（相对于agent.yaml的路径）
     */
    @JsonProperty("system_prompt_path")
    private Path systemPromptPath;
    
    /**
     * 系统提示词参数（用于模板替换）
     */
    @JsonProperty("system_prompt_args")
    @Builder.Default
    private Map<String, String> systemPromptArgs = new HashMap<>();
    
    /**
     * 工具列表（格式：module:ClassName）
     */
    @JsonProperty("tools")
    private List<String> tools;
    
    /**
     * 排除的工具列表
     */
    @JsonProperty("exclude_tools")
    private List<String> excludeTools;
    
    /**
     * 子Agent配置
     */
    @JsonProperty("subagents")
    @Builder.Default
    private Map<String, SubagentSpec> subagents = new HashMap<>();
}
