package io.leavesfly.jimi.exception;

/**
 * 运行取消异常
 * 当用户取消 Agent 运行时抛出
 */
public class RunCancelledException extends JimiException {
    
    public RunCancelledException() {
        super("Run cancelled by user");
    }
    
    public RunCancelledException(String message) {
        super(message);
    }
}
