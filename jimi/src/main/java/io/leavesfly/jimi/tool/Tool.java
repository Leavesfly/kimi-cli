package io.leavesfly.jimi.tool;

import reactor.core.publisher.Mono;

/**
 * 工具接口
 * 所有工具必须实现此接口
 * 
 * @param <P> 参数类型
 */
public interface Tool<P> {
    
    /**
     * 获取工具名称
     */
    String getName();
    
    /**
     * 获取工具描述
     */
    String getDescription();
    
    /**
     * 获取参数类型
     */
    Class<P> getParamsType();
    
    /**
     * 执行工具调用
     * 
     * @param params 工具参数
     * @return 工具执行结果的Mono
     */
    Mono<ToolResult> execute(P params);
    
    /**
     * 验证参数（可选，默认不验证）
     * 
     * @param params 工具参数
     * @return 验证是否通过
     */
    default boolean validateParams(P params) {
        return true;
    }
}
