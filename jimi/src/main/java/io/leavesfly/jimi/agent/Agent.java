package io.leavesfly.jimi.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 实体
 * 表示一个完整的 Agent，包含名称、系统提示词和工具集
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agent {
    
    /**
     * Agent 名称
     */
    private String name;
    
    /**
     * 系统提示词
     */
    private String systemPrompt;
    
    /**
     * 工具列表（工具类的完整类名）
     */
    @Builder.Default
    private List<String> tools = new ArrayList<>();
}
