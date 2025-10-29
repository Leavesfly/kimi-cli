package io.leavesfly.jimi.exception;

/**
 * LLM 未设置异常
 * 当需要 LLM 但未配置时抛出
 */
public class LLMNotSetException extends JimiException {
    
    public LLMNotSetException() {
        super("LLM not set. Please configure your API key and model.");
    }
    
    public LLMNotSetException(String message) {
        super(message);
    }
}
