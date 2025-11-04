package io.leavesfly.jimi.llm.message;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 消息角色枚举
 */
public enum MessageRole {
    /**
     * 用户消息
     */
    USER("user"),
    
    /**
     * 助手消息
     */
    ASSISTANT("assistant"),
    
    /**
     * 系统消息
     */
    SYSTEM("system"),
    
    /**
     * 工具消息
     */
    TOOL("tool");
    
    private final String value;
    
    MessageRole(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    public static MessageRole fromValue(String value) {
        for (MessageRole role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown message role: " + value);
    }
}
