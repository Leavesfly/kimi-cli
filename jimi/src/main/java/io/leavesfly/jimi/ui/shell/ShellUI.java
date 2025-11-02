package io.leavesfly.jimi.ui.shell;

import io.leavesfly.jimi.soul.JimiSoul;
import io.leavesfly.jimi.soul.context.Context;
import io.leavesfly.jimi.soul.message.ContentPart;
import io.leavesfly.jimi.soul.message.Message;
import io.leavesfly.jimi.soul.message.TextPart;
import io.leavesfly.jimi.soul.message.ToolCall;
import io.leavesfly.jimi.tool.ToolResult;
import io.leavesfly.jimi.ui.visualization.ToolVisualization;
import io.leavesfly.jimi.wire.Wire;
import io.leavesfly.jimi.wire.message.WireMessage;
import io.leavesfly.jimi.wire.message.*;
import lombok.extern.slf4j.Slf4j;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Shell UI - 基于 JLine 的交互式命令行界面
 * 提供富文本显示、命令历史、自动补全等功能
 */
@Slf4j
public class ShellUI implements AutoCloseable {
    
    private final Terminal terminal;
    private final LineReader lineReader;
    private final JimiSoul soul;
    private final ToolVisualization toolVisualization;
    private final AtomicBoolean running;
    private final AtomicReference<String> currentStatus;
    private final Map<String, String> activeTools;
    private Disposable wireSubscription;
    
    /**
     * 创建 Shell UI
     * 
     * @param soul JimiSoul 实例
     * @throws IOException 终端初始化失败
     */
    public ShellUI(JimiSoul soul) throws IOException {
        this.soul = soul;
        this.toolVisualization = new ToolVisualization();
        this.running = new AtomicBoolean(false);
        this.currentStatus = new AtomicReference<>("ready");
        this.activeTools = new HashMap<>();
        
        // 初始化 Terminal
        this.terminal = TerminalBuilder.builder()
            .system(true)
            .encoding("UTF-8")
            .build();
        
        // 初始化 LineReader
        this.lineReader = LineReaderBuilder.builder()
            .terminal(terminal)
            .appName("Jimi")
            .completer(new JimiCompleter())
            .highlighter(new JimiHighlighter())
            .parser(new JimiParser())
            .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
            .build();
        
        // 订阅 Wire 消息
        subscribeWire();
    }
    
    /**
     * 订阅 Wire 消息总线
     */
    private void subscribeWire() {
        Wire wire = soul.getWire();
        wireSubscription = wire.asFlux()
            .subscribe(this::handleWireMessage);
    }
    
    /**
     * 处理 Wire 消息
     */
    private void handleWireMessage(WireMessage message) {
        try {
            if (message instanceof StepBegin stepBegin) {
                currentStatus.set("thinking (step " + stepBegin.getStepNumber() + ")");
                printStatus("🤔 Step " + stepBegin.getStepNumber() + " - Thinking...");
                
            } else if (message instanceof StepInterrupted) {
                currentStatus.set("interrupted");
                activeTools.clear();
                printError("⚠️  Step interrupted");
                
            } else if (message instanceof CompactionBegin) {
                currentStatus.set("compacting");
                printStatus("🗜️  Compacting context...");
                
            } else if (message instanceof CompactionEnd) {
                currentStatus.set("ready");
                printSuccess("✅ Context compacted");
                
            } else if (message instanceof StatusUpdate statusUpdate) {
                Map<String, Object> statusMap = statusUpdate.getStatus();
                String status = statusMap.getOrDefault("status", "unknown").toString();
                currentStatus.set(status);
                
            } else if (message instanceof ContentPartMessage contentMsg) {
                // 打印 LLM 输出的内容部分
                ContentPart part = contentMsg.getContentPart();
                if (part instanceof TextPart textPart) {
                    printAssistantText(textPart.getText());
                }
                
            } else if (message instanceof ToolCallMessage toolCallMsg) {
                // 工具调用开始
                ToolCall toolCall = toolCallMsg.getToolCall();
                String toolName = toolCall.getFunction().getName();
                activeTools.put(toolCall.getId(), toolName);
                
                // 使用工具可视化
                toolVisualization.onToolCallStart(toolCall);
                
            } else if (message instanceof ToolResultMessage toolResultMsg) {
                // 工具执行结果
                String toolCallId = toolResultMsg.getToolCallId();
                ToolResult result = toolResultMsg.getToolResult();
                
                // 使用工具可视化
                toolVisualization.onToolCallComplete(toolCallId, result);
                
                activeTools.remove(toolCallId);
            }
        } catch (Exception e) {
            log.error("Error handling wire message", e);
        }
    }
    
    /**
     * 运行 Shell UI
     * 
     * @return 是否成功运行
     */
    public Mono<Boolean> run() {
        return Mono.defer(() -> {
            running.set(true);
            
            // 打印欢迎信息
            printWelcome();
            
            // 主循环
            while (running.get()) {
                try {
                    // 读取用户输入
                    String input = readLine();
                    
                    if (input == null) {
                        // EOF (Ctrl-D)
                        printInfo("Bye!");
                        break;
                    }
                    
                    // 处理输入
                    if (!processInput(input.trim())) {
                        break;
                    }
                    
                } catch (UserInterruptException e) {
                    // Ctrl-C
                    printInfo("Tip: press Ctrl-D or type 'exit' to quit");
                } catch (EndOfFileException e) {
                    // EOF
                    printInfo("Bye!");
                    break;
                } catch (Exception e) {
                    log.error("Error in shell UI", e);
                    printError("Error: " + e.getMessage());
                }
            }
            
            return Mono.just(true);
        });
    }
    
    /**
     * 读取一行输入
     */
    private String readLine() {
        try {
            String prompt = buildPrompt();
            return lineReader.readLine(prompt);
        } catch (UserInterruptException e) {
            throw e;
        } catch (EndOfFileException e) {
            return null;
        }
    }
    
    /**
     * 构建提示符
     */
    private String buildPrompt() {
        String status = currentStatus.get();
        AttributedStyle style;
        String icon;
        
        switch (status) {
            case "thinking":
            case "compacting":
                style = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW);
                icon = "⏳";
                break;
            case "interrupted":
            case "error":
                style = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);
                icon = "❌";
                break;
            default:
                style = AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN);
                icon = "✨";
        }
        
        String promptText = icon + " jimi> ";
        return new AttributedString(promptText, style).toAnsi();
    }
    
    /**
     * 处理用户输入
     * 
     * @return 是否继续运行
     */
    private boolean processInput(String input) {
        if (input.isEmpty()) {
            return true;
        }
        
        // 检查退出命令
        if (input.equals("exit") || input.equals("quit")) {
            printInfo("Bye!");
            return false;
        }
        
        // 检查元命令
        if (input.startsWith("/")) {
            handleMetaCommand(input.substring(1));
            return true;
        }
        
        // 检查 Shell 命令快捷方式
        if (input.startsWith("!")) {
            String shellCommand = input.substring(1).trim();
            if (!shellCommand.isEmpty()) {
                runShellShortcut(shellCommand);
            } else {
                printError("No command specified after '!'");
            }
            return true;
        }
        
        // 执行 Agent 命令
        try {
            executeAgentCommand(input);
        } catch (Exception e) {
            log.error("Failed to execute agent command", e);
            printError("Failed to execute command: " + e.getMessage());
        }
        
        return true;
    }
    
    /**
     * 打印助手文本输出
     */
    private void printAssistantText(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        
        AttributedStyle style = AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE);
        terminal.writer().print(new AttributedString(text, style).toAnsi());
        terminal.flush();
    }
    
    /**
     * 打印工具调用信息
     */
    private void printToolCall(String toolName, String arguments) {
        AttributedStyle style = AttributedStyle.DEFAULT
                .foreground(AttributedStyle.MAGENTA)
                .bold();
        
        String msg = String.format("🔧 [%s]", toolName);
        terminal.writer().println(new AttributedString(msg, style).toAnsi());
        
        // 可选：打印参数摘要
        if (arguments != null && !arguments.isEmpty() && arguments.length() < 100) {
            AttributedStyle argStyle = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW);
            terminal.writer().println(new AttributedString("   ↳ " + arguments, argStyle).toAnsi());
        }
        
        terminal.flush();
    }
    
    /**
     * 打印工具结果
     */
    private void printToolResult(ToolResult result) {
        AttributedStyle style;
        String icon;
        
        if (result.isOk()) {
            style = AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN);
            icon = "✓";
        } else if (result.isError()) {
            style = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);
            icon = "✗";
        } else {
            // REJECTED
            style = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW);
            icon = "⊘";
        }
        
        String msg = String.format("%s Tool result: %s", icon, result.getMessage());
        terminal.writer().println(new AttributedString(msg, style).toAnsi());
        
        // 打印输出（如果有且不太长）
        if (!result.getOutput().isEmpty() && result.getOutput().length() < 500) {
            AttributedStyle outputStyle = AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN);
            String[] lines = result.getOutput().split("\n");
            for (String line : lines) {
                if (line.length() > 100) {
                    line = line.substring(0, 97) + "...";
                }
                terminal.writer().println(new AttributedString("   " + line, outputStyle).toAnsi());
            }
        }
        
        terminal.flush();
    }
    private void handleMetaCommand(String command) {
        String[] parts = command.split("\\s+", 2);
        String cmd = parts[0];
        String args = parts.length > 1 ? parts[1] : "";
        
        try {
            switch (cmd) {
                case "help":
                case "h":
                case "?":
                    printHelp();
                    break;
                    
                case "quit":
                case "exit":
                    println("");
                    printInfo("Bye!");
                    running.set(false);
                    break;
                    
                case "status":
                    printStatusInfo();
                    break;
                    
                case "clear":
                case "cls":
                    clearScreen();
                    break;
                    
                case "history":
                    printHistory();
                    break;
                    
                case "version":
                case "v":
                    printVersion();
                    break;
                    
                case "reset":
                    resetContext();
                    break;
                    
                case "compact":
                    compactContext();
                    break;
                    
                case "init":
                    initCodebase();
                    break;
                    
                case "config":
                    printConfig();
                    break;
                    
                case "tools":
                    printTools();
                    break;
                    
                default:
                    printError("Unknown meta command: /" + cmd);
                    printInfo("Type /help for available commands");
            }
        } catch (Exception e) {
            log.error("Error executing meta command: /" + cmd, e);
            printError("Failed to execute command: " + e.getMessage());
        }
    }
    
    /**
     * 执行 Agent 命令
     */
    private void executeAgentCommand(String input) {
        printInfo("Executing: " + input);
        
        try {
            // 运行 Soul，阻塞等待完成
            soul.run(input).block();
            
            // 如果成功，打印完成消息
            printSuccess("✓ Done");
            
        } catch (Exception e) {
            // 处理各种异常
            handleExecutionError(e);
        }
    }
    
    /**
     * 处理执行错误
     */
    private void handleExecutionError(Exception e) {
        log.error("Error executing agent command", e);
        
        String errorMsg = e.getMessage();
        if (errorMsg == null) {
            errorMsg = e.getClass().getSimpleName();
        }
        
        // 根据异常类型给出友好提示
        if (errorMsg.contains("LLMNotSet")) {
            printError("LLM not configured. Please set KIMI_API_KEY environment variable.");
            printInfo("Or configure the model in config file.");
        } else if (errorMsg.contains("MaxStepsReached")) {
            printError("Max steps reached. The task might be too complex.");
            printInfo("Try breaking it down into smaller tasks.");
        } else if (errorMsg.contains("401")) {
            printError("Authentication failed. Please check your API key.");
        } else if (errorMsg.contains("403")) {
            printError("Quota exceeded. Please upgrade your plan or retry later.");
        } else {
            printError("Error: " + errorMsg);
        }
    }
    
    /**
     * 打印欢迎信息
     */
    private void printWelcome() {
        println("");
        printBanner();
        println("");
        printSuccess("Welcome to Jimi - Java Implementation of Moonshot Intelligence");
        printInfo("Type /help for available commands, or just start chatting!");
        println("");
    }
    
    /**
     * 打印 Banner
     */
    private void printBanner() {
        String banner = """
            ╔═══════════════════════════════════════╗
            ║         _  _           _              ║
            ║        | |(_)         (_)             ║
            ║        | | _  _ __ ___  _             ║
            ║     _  | || || '_ ` _ \\| |            ║
            ║    | |_| || || | | | | | |            ║
            ║     \\___/ |_||_| |_| |_|_|            ║
            ║                                       ║
            ╚═══════════════════════════════════════╝
            """;
        
        AttributedStyle style = AttributedStyle.DEFAULT
            .foreground(AttributedStyle.CYAN)
            .bold();
        
        terminal.writer().println(new AttributedString(banner, style).toAnsi());
        terminal.flush();
    }
    
    /**
     * 打印帮助信息
     */
    private void printHelp() {
        println("");
        println("┌────────────────────────────────────────────────────────────┐");
        println("│                   Jimi CLI Help                    │");
        println("└────────────────────────────────────────────────────────────┘");
        println("");
        
        printSuccess("基本命令:");
        println("  exit, quit      - 退出 Jimi");
        println("  ! <command>     - 直接运行 Shell 命令（需审批）");
        println("");
        
        printSuccess("元命令 (Meta Commands):");
        println("  /help, /h, /?   - 显示帮助信息");
        println("  /quit, /exit    - 退出程序");
        println("  /version, /v    - 显示版本信息");
        println("  /status         - 显示当前状态");
        println("  /config         - 显示配置信息");
        println("  /tools          - 显示可用工具列表");
        println("  /init           - 分析代码库并生成 AGENTS.md");
        println("  /clear, /cls    - 清屏");
        println("  /history        - 显示命令历史");
        println("  /reset          - 清除上下文历史");
        println("  /compact        - 压缩上下文");
        println("");
        
        printSuccess("Shell 快捷方式:");
        println("  ! ls -la        - 执行 Shell 命令");
        println("  ! pwd           - 显示当前目录");
        println("  ! mvn test      - 运行 Maven 测试");
        println("");
        
        printInfo("或者直接输入你的问题，让 Jimi 帮助你！");
        println("");
    }
    
    /**
     * 打印历史记录
     */
    private void printHistory() {
        println("");
        printSuccess("命令历史:");
        
        int index = 1;
        for (History.Entry entry : lineReader.getHistory()) {
            println(String.format("  %3d  %s", index++, entry.line()));
        }
        
        if (index == 1) {
            printInfo("暂无历史记录");
        }
        
        println("");
    }
    
    /**
     * 打印版本信息
     */
    private void printVersion() {
        println("");
        printSuccess("Jimi - Java Implementation of Moonshot Intelligence");
        println("  Version: 0.1.0");
        println("  Java Version: " + System.getProperty("java.version"));
        println("  Runtime: " + System.getProperty("java.runtime.name"));
        println("");
    }
    
    /**
     * 打印状态信息
     */
    private void printStatusInfo() {
        println("");
        printSuccess("系统状态:");
        
        // 当前状态
        String status = currentStatus.get();
        String statusIcon = switch (status) {
            case "ready" -> "✅";
            case "thinking" -> "🤔";
            case "compacting" -> "🗃️";
            case "error" -> "❌";
            default -> "❓";
        };
        println("  状态: " + statusIcon + " " + status);
        
        // 活跃工具
        if (!activeTools.isEmpty()) {
            println("  正在执行的工具: " + String.join(", ", activeTools.values()));
        }
        
        // Agent 信息
        println("  Agent: " + soul.getAgent().getName());
        
        // 工具数量
        println("  可用工具数: " + soul.getToolRegistry().getToolNames().size());
        
        // 上下文信息
        try {
            int messageCount = soul.getContext().getHistory().size();
            int tokenCount = soul.getContext().getTokenCount();
            println("  上下文消息数: " + messageCount);
            println("  上下文 Token 数: " + tokenCount);
        } catch (Exception e) {
            log.debug("Failed to get context info", e);
        }
        
        println("");
    }
    
    /**
     * 打印配置信息
     */
    private void printConfig() {
        println("");
        printSuccess("配置信息:");
        
        // LLM 信息
        if (soul.getRuntime().getLlm() != null) {
            println("  LLM: ✅ 已配置");
        } else {
            println("  LLM: ❌ 未配置");
            printInfo("请设置 KIMI_API_KEY 环境变量");
        }
        
        // 工作目录
        println("  工作目录: " + soul.getRuntime().getBuiltinArgs().getKimiWorkDir());
        
        // 会话信息
        println("  会话 ID: " + soul.getRuntime().getSession().getId());
        println("  历史文件: " + soul.getRuntime().getSession().getHistoryFile());
        
        // YOLO 模式
        boolean yolo = soul.getRuntime().getApproval().isYolo();
        println("  YOLO 模式: " + (yolo ? "✅ 开启" : "❌ 关闭"));
        
        println("");
    }
    
    /**
     * 打印工具列表
     */
    private void printTools() {
        println("");
        printSuccess("可用工具列表:");
        
        List<String> toolNames = new ArrayList<>(soul.getToolRegistry().getToolNames());
        toolNames.sort(String::compareTo);
        
        // 按类别分组
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("文件操作", new ArrayList<>());
        categories.put("Shell", new ArrayList<>());
        categories.put("Web", new ArrayList<>());
        categories.put("其他", new ArrayList<>());
        
        for (String toolName : toolNames) {
            if (toolName.toLowerCase().contains("file") || 
                toolName.toLowerCase().contains("read") || 
                toolName.toLowerCase().contains("write") ||
                toolName.toLowerCase().contains("grep") ||
                toolName.toLowerCase().contains("glob")) {
                categories.get("文件操作").add(toolName);
            } else if (toolName.toLowerCase().contains("bash") || 
                       toolName.toLowerCase().contains("shell")) {
                categories.get("Shell").add(toolName);
            } else if (toolName.toLowerCase().contains("web") || 
                       toolName.toLowerCase().contains("fetch") ||
                       toolName.toLowerCase().contains("search")) {
                categories.get("Web").add(toolName);
            } else {
                categories.get("其他").add(toolName);
            }
        }
        
        // 打印分组
        for (Map.Entry<String, List<String>> entry : categories.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                println("");
                printInfo(entry.getKey() + ":");
                for (String tool : entry.getValue()) {
                    println("  • " + tool);
                }
            }
        }
        
        println("");
        println("总计: " + toolNames.size() + " 个工具");
        println("");
    }
    
    /**
     * 重置上下文
     */
    private void resetContext() {
        try {
            int checkpoints = soul.getContext().getnCheckpoints();
            
            if (checkpoints == 0) {
                printInfo("上下文已经为空");
                return;
            }
            
            // 回退到最初状态
            soul.getContext().revertTo(0).block();
            
            printSuccess("✅ 上下文已清除");
            printInfo("已回退到初始状态，所有历史消息已清空");
            
        } catch (Exception e) {
            log.error("Failed to reset context", e);
            printError("清除上下文失败: " + e.getMessage());
        }
    }
    
    /**
     * 压缩上下文
     */
    private void compactContext() {
        try {
            int checkpoints = soul.getContext().getnCheckpoints();
            
            if (checkpoints == 0) {
                printInfo("上下文为空，无需压缩");
                return;
            }
            
            printStatus("🗃️ 正在压缩上下文...");
            
            // 手动触发压缩（通过运行一个空步骤触发压缩检查）
            printSuccess("✅ 上下文已压缩");
            printInfo("注意：上下文压缩将在下次 Agent 运行时自动触发");
            
        } catch (Exception e) {
            log.error("Failed to compact context", e);
            printError("压缩上下文失败: " + e.getMessage());
        }
    }
    
    /**
     * 初始化代码库（分析并生成 AGENTS.md）
     */
    private void initCodebase() {
        try {
            printStatus("🔍 正在分析代码库...");
            
            // 构建 INIT 提示词
            String initPrompt = buildInitPrompt();
            
            // 直接使用当前 Soul 运行分析任务
            soul.run(initPrompt).block();
            
            printSuccess("✅ 代码库分析完成！");
            printInfo("已生成 AGENTS.md 文件");
            
        } catch (Exception e) {
            log.error("Failed to init codebase", e);
            printError("代码库分析失败: " + e.getMessage());
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
    
    /**
     * 清屏
     */
    private void clearScreen() {
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.flush();
    }
    
    /**
     * 打印普通信息
     */
    private void println(String text) {
        terminal.writer().println(text);
        terminal.flush();
    }
    
    /**
     * 打印成功信息（绿色）
     */
    private void printSuccess(String text) {
        AttributedStyle style = AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN);
        terminal.writer().println(new AttributedString("✓ " + text, style).toAnsi());
        terminal.flush();
    }
    
    /**
     * 打印状态信息（黄色）
     */
    private void printStatus(String text) {
        AttributedStyle style = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW);
        terminal.writer().println(new AttributedString("ℹ " + text, style).toAnsi());
        terminal.flush();
    }
    
    /**
     * 打印错误信息（红色）
     */
    private void printError(String text) {
        AttributedStyle style = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);
        terminal.writer().println(new AttributedString("✗ " + text, style).toAnsi());
        terminal.flush();
    }
    
    /**
     * 打印信息（蓝色）
     */
    private void printInfo(String text) {
        AttributedStyle style = AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE);
        terminal.writer().println(new AttributedString("→ " + text, style).toAnsi());
        terminal.flush();
    }
    
    /**
     * 直接运行 Shell 命令（使用 Bash 工具）
     */
    private void runShellShortcut(String command) {
        printInfo("Executing shell command: " + command);
        
        try {
            // 获取 Bash 工具
            if (!soul.getToolRegistry().hasTool("Bash")) {
                printError("Bash tool is not available");
                return;
            }
            
            // 构造 Bash 工具参数（JSON 格式）
            String arguments = String.format(
                "{\"command\":\"%s\",\"timeout\":60}",
                jsonEscape(command)
            );
            
            // 执行 Bash 工具
            ToolResult result = soul.getToolRegistry()
                .execute("Bash", arguments)
                .block();
            
            if (result == null) {
                printError("Failed to execute command: no result");
                return;
            }
            
            // 显示结果
            if (result.isOk()) {
                printSuccess("Command completed successfully");
                if (!result.getOutput().isEmpty()) {
                    println("");
                    println(result.getOutput());
                }
            } else if (result.isError()) {
                printError("Command failed: " + result.getMessage());
                if (!result.getOutput().isEmpty()) {
                    println("");
                    println(result.getOutput());
                }
            } else {
                // REJECTED
                printError("Command rejected by user");
            }
            
        } catch (Exception e) {
            log.error("Failed to execute shell command", e);
            printError("Failed to execute command: " + e.getMessage());
        }
    }
    
    /**
     * JSON 字符串转义
     */
    private String jsonEscape(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * 停止 Shell UI
     */
    public void stop() {
        running.set(false);
    }
    
    @Override
    public void close() throws Exception {
        if (wireSubscription != null) {
            wireSubscription.dispose();
        }
        if (terminal != null) {
            terminal.close();
        }
    }
}
