package io.leavesfly.jimi.ui.shell;

import org.jline.reader.Completer;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

/**
 * Jimi 命令补全器
 * 提供元命令和常用命令的自动补全
 */
public class JimiCompleter implements Completer {
    
    private static final String[] META_COMMANDS = {
        "/help",
        "/status",
        "/clear",
        "/history"
    };
    
    private static final String[] COMMON_PHRASES = {
        "help me",
        "show me",
        "explain",
        "what is",
        "how to",
        "please"
    };
    
    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String word = line.word();
        
        // 补全元命令
        if (word.startsWith("/")) {
            for (String cmd : META_COMMANDS) {
                if (cmd.startsWith(word)) {
                    candidates.add(new Candidate(
                        cmd,
                        cmd,
                        null,
                        getCommandDescription(cmd),
                        null,
                        null,
                        true
                    ));
                }
            }
        }
        
        // 补全常用短语（仅在行首）
        if (line.wordIndex() == 0 && !word.startsWith("/")) {
            for (String phrase : COMMON_PHRASES) {
                if (phrase.startsWith(word.toLowerCase())) {
                    candidates.add(new Candidate(
                        phrase,
                        phrase,
                        null,
                        "common phrase",
                        null,
                        null,
                        false
                    ));
                }
            }
        }
    }
    
    /**
     * 获取命令描述
     */
    private String getCommandDescription(String command) {
        return switch (command) {
            case "/help" -> "Show help message";
            case "/status" -> "Show current status";
            case "/clear" -> "Clear the screen";
            case "/history" -> "Show command history";
            default -> null;
        };
    }
}
