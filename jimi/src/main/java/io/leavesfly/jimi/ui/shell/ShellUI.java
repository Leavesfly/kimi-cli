package io.leavesfly.jimi.ui.shell;

import io.leavesfly.jimi.soul.JimiSoul;
import io.leavesfly.jimi.soul.message.ContentPart;
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
import java.util.HashMap;
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
        
        switch (cmd) {
            case "help":
                printHelp();
                break;
            case "status":
                printStatus("Current status: " + currentStatus.get());
                break;
            case "clear":
                clearScreen();
                break;
            case "history":
                printHistory();
                break;
            default:
                printError("Unknown meta command: /" + cmd);
                printInfo("Type /help for available commands");
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
        printSuccess("Available Meta Commands:");
        println("  /help     - Show this help message");
        println("  /status   - Show current status");
        println("  /clear    - Clear the screen");
        println("  /history  - Show command history");
        println("  exit/quit - Exit the shell");
        println("");
        printInfo("Or just type your message to chat with Jimi!");
        println("");
    }
    
    /**
     * 打印历史记录
     */
    private void printHistory() {
        println("");
        printSuccess("Command History:");
        
        int index = 1;
        for (History.Entry entry : lineReader.getHistory()) {
            println(String.format("  %3d  %s", index++, entry.line()));
        }
        
        println("");
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
