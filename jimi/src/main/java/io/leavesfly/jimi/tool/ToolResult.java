package io.leavesfly.jimi.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具执行结果
 * 表示工具调用的返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {
    
    /**
     * 结果类型
     */
    private ResultType type;
    
    /**
     * 输出内容（工具执行的详细输出）
     */
    @Builder.Default
    private String output = "";
    
    /**
     * 结果消息（对结果的描述）
     */
    @Builder.Default
    private String message = "";
    
    /**
     * 简要描述（可选，用于UI显示）
     */
    @Builder.Default
    private String brief = "";
    
    /**
     * 结果类型枚举
     */
    public enum ResultType {
        /**
         * 成功
         */
        OK,
        
        /**
         * 错误
         */
        ERROR,
        
        /**
         * 被拒绝
         */
        REJECTED
    }
    
    /**
     * 创建成功结果
     */
    public static ToolResult ok(String output, String message) {
        return ToolResult.builder()
                .type(ResultType.OK)
                .output(output)
                .message(message)
                .build();
    }
    
    /**
     * 创建成功结果（带简要描述）
     */
    public static ToolResult ok(String output, String message, String brief) {
        return ToolResult.builder()
                .type(ResultType.OK)
                .output(output)
                .message(message)
                .brief(brief)
                .build();
    }
    
    /**
     * 创建错误结果
     */
    public static ToolResult error(String message, String brief) {
        return error("", message, brief);
    }
    
    /**
     * 创建错误结果（带输出）
     */
    public static ToolResult error(String output, String message, String brief) {
        return ToolResult.builder()
                .type(ResultType.ERROR)
                .output(output)
                .message(message)
                .brief(brief)
                .build();
    }
    
    /**
     * 创建被拒绝结果
     */
    public static ToolResult rejected() {
        return ToolResult.builder()
                .type(ResultType.REJECTED)
                .message("工具调用被用户拒绝。请遵循用户的新指示。")
                .brief("被用户拒绝")
                .build();
    }
    
    /**
     * 检查是否成功
     */
    public boolean isOk() {
        return type == ResultType.OK;
    }
    
    /**
     * 检查是否错误
     */
    public boolean isError() {
        return type == ResultType.ERROR;
    }
    
    /**
     * 检查是否被拒绝
     */
    public boolean isRejected() {
        return type == ResultType.REJECTED;
    }
}
