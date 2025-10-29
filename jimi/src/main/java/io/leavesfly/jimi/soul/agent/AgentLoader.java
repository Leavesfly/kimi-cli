package io.leavesfly.jimi.soul.agent;

import io.leavesfly.jimi.agentspec.AgentSpecLoader;
import io.leavesfly.jimi.agentspec.ResolvedAgentSpec;
import io.leavesfly.jimi.exception.AgentSpecException;
import io.leavesfly.jimi.soul.runtime.BuiltinSystemPromptArgs;
import io.leavesfly.jimi.soul.runtime.Runtime;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent加载器
 * 负责从配置文件加载Agent实例
 */
@Slf4j
public class AgentLoader {
    
    /**
     * 加载Agent
     * 
     * @param agentFile Agent配置文件路径
     * @param runtime 运行时上下文
     * @return 加载完成的Agent实例
     */
    public static Mono<Agent> loadAgent(Path agentFile, Runtime runtime) {
        return AgentSpecLoader.loadAgentSpec(agentFile)
                .flatMap(spec -> {
                    log.info("加载Agent: {} (from {})", spec.getName(), agentFile);
                    
                    // 加载系统提示词
                    String systemPrompt = loadSystemPrompt(
                            spec.getSystemPromptPath(),
                            spec.getSystemPromptArgs(),
                            runtime.getBuiltinArgs()
                    );
                    
                    // 处理工具列表
                    List<String> tools = spec.getTools();
                    if (spec.getExcludeTools() != null && !spec.getExcludeTools().isEmpty()) {
                        log.debug("排除工具: {}", spec.getExcludeTools());
                        tools = tools.stream()
                                .filter(tool -> !spec.getExcludeTools().contains(tool))
                                .collect(Collectors.toList());
                    }
                    
                    // 构建Agent实例
                    Agent agent = Agent.builder()
                            .name(spec.getName())
                            .systemPrompt(systemPrompt)
                            .tools(tools)
                            .build();
                    
                    log.info("Agent加载完成: {}, 工具数量: {}", 
                            agent.getName(), agent.getTools().size());
                    
                    return Mono.just(agent);
                });
    }
    
    /**
     * 加载系统提示词
     * 
     * @param promptPath 提示词文件路径
     * @param args 自定义参数
     * @param builtinArgs 内置参数
     * @return 替换后的系统提示词
     */
    private static String loadSystemPrompt(
            Path promptPath,
            Map<String, String> args,
            BuiltinSystemPromptArgs builtinArgs
    ) {
        log.info("加载系统提示词: {}", promptPath);
        
        try {
            // 读取提示词文件
            String template = Files.readString(promptPath).strip();
            
            // 准备替换参数
            Map<String, String> substitutionMap = new HashMap<>();
            
            // 添加内置参数
            substitutionMap.put("KIMI_NOW", builtinArgs.getKimiNow());
            substitutionMap.put("KIMI_WORK_DIR", builtinArgs.getKimiWorkDir().toString());
            substitutionMap.put("KIMI_WORK_DIR_LS", builtinArgs.getKimiWorkDirLs());
            substitutionMap.put("KIMI_AGENTS_MD", builtinArgs.getKimiAgentsMd());
            
            // 添加自定义参数（覆盖内置参数）
            if (args != null) {
                substitutionMap.putAll(args);
            }
            
            log.debug("替换系统提示词参数 - 内置参数: {}, 自定义参数: {}", 
                    builtinArgs, args);
            
            // 执行字符串替换
            StringSubstitutor substitutor = new StringSubstitutor(substitutionMap);
            return substitutor.replace(template);
            
        } catch (IOException e) {
            throw new AgentSpecException("加载系统提示词失败: " + e.getMessage(), e);
        }
    }
}
