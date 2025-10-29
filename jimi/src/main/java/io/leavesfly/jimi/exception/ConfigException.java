package io.leavesfly.jimi.exception;

/**
 * 配置异常
 * 当配置文件无效或配置加载失败时抛出
 */
public class ConfigException extends JimiException {
    
    public ConfigException(String message) {
        super(message);
    }
    
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
