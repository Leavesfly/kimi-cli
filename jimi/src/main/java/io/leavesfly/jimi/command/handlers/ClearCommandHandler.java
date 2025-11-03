package io.leavesfly.jimi.command.handlers;

import io.leavesfly.jimi.command.CommandContext;
import io.leavesfly.jimi.command.CommandHandler;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * /clear 命令处理器
 * 清屏
 */
@Component
public class ClearCommandHandler implements CommandHandler {
    
    @Override
    public String getName() {
        return "clear";
    }
    
    @Override
    public String getDescription() {
        return "清屏";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("cls");
    }
    
    @Override
    public void execute(CommandContext context) {
        context.getOutputFormatter().clearScreen();
    }
}
