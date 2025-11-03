package io.leavesfly.jimi.command.handlers;

import io.leavesfly.jimi.command.CommandContext;
import io.leavesfly.jimi.command.CommandHandler;
import io.leavesfly.jimi.ui.shell.output.OutputFormatter;
import org.jline.reader.History;
import org.springframework.stereotype.Component;

/**
 * /history 命令处理器
 * 显示命令历史记录
 */
@Component
public class HistoryCommandHandler implements CommandHandler {
    
    @Override
    public String getName() {
        return "history";
    }
    
    @Override
    public String getDescription() {
        return "显示命令历史";
    }
    
    @Override
    public void execute(CommandContext context) {
        OutputFormatter out = context.getOutputFormatter();
        
        out.println();
        out.printSuccess("命令历史:");
        
        int index = 1;
        for (History.Entry entry : context.getLineReader().getHistory()) {
            out.println(String.format("  %3d  %s", index++, entry.line()));
        }
        
        if (index == 1) {
            out.printInfo("暂无历史记录");
        }
        
        out.println();
    }
}
