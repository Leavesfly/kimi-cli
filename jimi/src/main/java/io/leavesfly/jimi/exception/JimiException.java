package io.leavesfly.jimi.exception;

/**
 * Jimi 基础异常类
 * 所有 Jimi 相关异常的基类
 */
public class JimiException extends RuntimeException {
    
    public JimiException(String message) {
        super(message);
    }
    
    public JimiException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public JimiException(Throwable cause) {
        super(cause);
    }
}
