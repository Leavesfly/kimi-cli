package io.leavesfly.jimi.command.handlers;

import io.leavesfly.jimi.command.CommandContext;
import io.leavesfly.jimi.command.CommandHandler;
import io.leavesfly.jimi.ui.shell.output.OutputFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * /init 命令处理器
 * 初始化代码库（分析并生成 AGENTS.md）
 */
@Slf4j
@Component
public class InitCommandHandler implements CommandHandler {
    
    @Override
    public String getName() {
        return "init";
    }
    
    @Override
    public String getDescription() {
        return "分析代码库并生成 AGENTS.md";
    }
    
    @Override
    public void execute(CommandContext context) {
        OutputFormatter out = context.getOutputFormatter();
        
        try {
            out.printStatus("🔍 正在分析代码库...");
            
            // 构建 INIT 提示词
            String initPrompt = buildInitPrompt();
            
            // 直接使用当前 Soul 运行分析任务
            context.getSoul().run(initPrompt).block();
            
            out.printSuccess("✅ 代码库分析完成！");
            out.printInfo("已生成 AGENTS.md 文件");
            
        } catch (Exception e) {
            log.error("Failed to init codebase", e);
            out.printError("代码库分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建 INIT 提示词
     */
    private String buildInitPrompt() {
        return "You are a software engineering expert with many years of programming experience. \n" +
            "Please explore the current project directory to understand the project's architecture and main details.\n" +
            "\n" +
            "Task requirements:\n" +
            "1. Analyze the project structure and identify key configuration files (such as pom.xml, build.gradle, package.json, etc.).\n" +
            "2. Understand the project's technology stack, build process and runtime architecture.\n" +
            "3. Identify how the code is organized and main module divisions.\n" +
            "4. Discover project-specific development conventions, testing strategies, and deployment processes.\n" +
            "\n" +
            "After the exploration, you should do a thorough summary of your findings and overwrite it into `AGENTS.md` file in the project root. \n" +
            "You need to refer to what is already in the file when you do so.\n" +
            "\n" +
            "For your information, `AGENTS.md` is a file intended to be read by AI coding agents. \n" +
            "Expect the reader of this file know nothing about the project.\n" +
            "\n" +
            "You should compose this file according to the actual project content. \n" +
            "Do not make any assumptions or generalizations. Ensure the information is accurate and useful.\n" +
            "\n" +
            "Popular sections that people usually write in `AGENTS.md` are:\n" +
            "- Project overview\n" +
            "- Build and test commands\n" +
            "- Code style guidelines\n" +
            "- Testing instructions\n" +
            "- Security considerations";
    }
}
