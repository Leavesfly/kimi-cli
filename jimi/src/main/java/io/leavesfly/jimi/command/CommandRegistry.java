package io.leavesfly.jimi.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 命令注册表
 * 负责管理所有命令处理器的注册、查找和执行
 * 
 * Spring Bean，自动注入所有 CommandHandler 实现
 */
@Slf4j
@Component
public class CommandRegistry {
    
    private final Map<String, CommandHandler> handlers = new ConcurrentHashMap<>();
    private final Map<String, String> aliases = new ConcurrentHashMap<>();
    
    /**
     * 构造函数，自动注入所有 CommandHandler 实现
     * Spring 会自动发现所有标注了 @Component 的 CommandHandler 实现类
     * 
     * @param commandHandlers Spring 自动注入的 CommandHandler 列表
     */
    @Autowired
    public CommandRegistry(List<CommandHandler> commandHandlers) {
        // 自动注册所有注入的命令处理器
        if (commandHandlers != null) {
            commandHandlers.forEach(this::register);
            log.info("Auto-registered {} command handlers via Spring dependency injection", handlers.size());
        }
    }
    
    /**
     * 注册命令处理器
     * 
     * @param handler 命令处理器
     */
    public void register(CommandHandler handler) {
        String name = handler.getName().toLowerCase();
        
        if (handlers.containsKey(name)) {
            log.warn("Command handler already registered: {}, overwriting", name);
        }
        
        handlers.put(name, handler);
        log.debug("Registered command handler: {}", name);
        
        // 注册别名
        for (String alias : handler.getAliases()) {
            String aliasLower = alias.toLowerCase();
            aliases.put(aliasLower, name);
            log.debug("Registered alias: {} -> {}", aliasLower, name);
        }
    }
    
    /**
     * 批量注册命令处理器
     * 
     * @param handlers 命令处理器列表
     */
    public void registerAll(List<CommandHandler> handlers) {
        handlers.forEach(this::register);
    }
    
    /**
     * 注销命令处理器
     * 
     * @param commandName 命令名称
     */
    public void unregister(String commandName) {
        String name = commandName.toLowerCase();
        CommandHandler handler = handlers.remove(name);
        
        if (handler != null) {
            // 移除别名
            for (String alias : handler.getAliases()) {
                aliases.remove(alias.toLowerCase());
            }
            log.debug("Unregistered command handler: {}", name);
        }
    }
    
    /**
     * 获取命令处理器
     * 
     * @param commandName 命令名称或别名
     * @return 命令处理器，如果不存在则返回 null
     */
    public CommandHandler getHandler(String commandName) {
        String name = commandName.toLowerCase();
        
        // 首先检查是否为别名
        String actualName = aliases.get(name);
        if (actualName != null) {
            return handlers.get(actualName);
        }
        
        // 直接查找命令名称
        return handlers.get(name);
    }
    
    /**
     * 检查命令是否存在
     * 
     * @param commandName 命令名称或别名
     * @return 如果命令存在返回 true
     */
    public boolean hasCommand(String commandName) {
        return getHandler(commandName) != null;
    }
    
    /**
     * 执行命令
     * 
     * @param commandName 命令名称
     * @param context 命令上下文
     * @throws Exception 执行过程中的异常
     */
    public void execute(String commandName, CommandContext context) throws Exception {
        CommandHandler handler = getHandler(commandName);
        
        if (handler == null) {
            throw new IllegalArgumentException("Unknown command: " + commandName);
        }
        
        if (!handler.isAvailable(context)) {
            throw new IllegalStateException("Command not available: " + commandName);
        }
        
        log.debug("Executing command: {}", commandName);
        handler.execute(context);
    }
    
    /**
     * 获取所有已注册的命令名称
     * 
     * @return 命令名称列表（已排序）
     */
    public List<String> getCommandNames() {
        List<String> names = new ArrayList<>(handlers.keySet());
        Collections.sort(names);
        return names;
    }
    
    /**
     * 获取所有命令处理器
     * 
     * @return 命令处理器列表
     */
    public List<CommandHandler> getAllHandlers() {
        return new ArrayList<>(handlers.values());
    }
    
    /**
     * 清空所有注册的命令
     */
    public void clear() {
        handlers.clear();
        aliases.clear();
        log.debug("Cleared all command handlers");
    }
    
    /**
     * 获取已注册命令的数量
     * 
     * @return 命令数量
     */
    public int size() {
        return handlers.size();
    }
}
