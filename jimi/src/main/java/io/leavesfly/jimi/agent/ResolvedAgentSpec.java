package io.leavesfly.jimi.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 已解析的Agent规范
 * 所有可选字段都已确定值，继承关系已展开
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolvedAgentSpec {
    
    /**
     * Agent名称（必填）
     */
    private String name;
    
    /**
     * 系统提示词文件路径（绝对路径）
     */
    private Path systemPromptPath;
    
    /**
     * 系统提示词参数
     */
    @Builder.Default
    private Map<String, String> systemPromptArgs = new HashMap<>();
    
    /**
     * 工具列表
     */
    @Builder.Default
    private List<String> tools = new ArrayList<>();
    
    /**
     * 排除的工具列表
     */
    @Builder.Default
    private List<String> excludeTools = new ArrayList<>();
    
    /**
     * 子Agent配置
     */
    @Builder.Default
    private Map<String, SubagentSpec> subagents = new HashMap<>();
}
