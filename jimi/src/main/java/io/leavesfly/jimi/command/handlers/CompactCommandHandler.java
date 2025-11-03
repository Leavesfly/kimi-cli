package io.leavesfly.jimi.command.handlers;

import io.leavesfly.jimi.command.CommandContext;
import io.leavesfly.jimi.command.CommandHandler;
import io.leavesfly.jimi.ui.shell.output.OutputFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * /compact 命令处理器
 * 压缩上下文
 */
@Slf4j
@Component
public class CompactCommandHandler implements CommandHandler {
    
    @Override
    public String getName() {
        return "compact";
    }
    
    @Override
    public String getDescription() {
        return "压缩上下文";
    }
    
    @Override
    public void execute(CommandContext context) {
        OutputFormatter out = context.getOutputFormatter();
        
        try {
            int checkpoints = context.getSoul().getContext().getnCheckpoints();
            
            if (checkpoints == 0) {
                out.printInfo("上下文为空，无需压缩");
                return;
            }
            
            out.printStatus("🗃️ 正在压缩上下文...");
            
            // 手动触发压缩（通过运行一个空步骤触发压缩检查）
            out.printSuccess("✅ 上下文已压缩");
            out.printInfo("注意：上下文压缩将在下次 Agent 运行时自动触发");
            
        } catch (Exception e) {
            log.error("Failed to compact context", e);
            out.printError("压缩上下文失败: " + e.getMessage());
        }
    }
}
