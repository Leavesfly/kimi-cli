package io.leavesfly.jimi.ui.shell;

import io.leavesfly.jimi.command.CommandHandler;
import io.leavesfly.jimi.command.CommandRegistry;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JimiCompleter 测试
 */
class JimiCompleterTest {
    
    private CommandRegistry commandRegistry;
    private JimiCompleter completer;
    private LineReader lineReader;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() throws IOException {
        // 创建模拟的 CommandRegistry
        commandRegistry = new CommandRegistry(null);
        
        // 注册一些测试命令
        commandRegistry.register(createMockHandler("help", "Show help", "h", "?"));
        commandRegistry.register(createMockHandler("status", "Show status", "s"));
        commandRegistry.register(createMockHandler("config", "Show config", "cfg"));
        commandRegistry.register(createMockHandler("tools", "List tools", "t"));
        
        // 创建测试文件和目录
        Files.createFile(tempDir.resolve("test.java"));
        Files.createFile(tempDir.resolve("README.md"));
        Files.createDirectory(tempDir.resolve("src"));
        Files.createDirectory(tempDir.resolve("target"));
        
        // 创建 completer
        completer = new JimiCompleter(commandRegistry, tempDir);
        lineReader = mock(LineReader.class);
    }
    
    @Test
    void testMetaCommandCompletion() {
        // 测试元命令补全
        ParsedLine line = mockParsedLine("/he", 0);
        List<Candidate> candidates = new ArrayList<>();
        
        completer.complete(lineReader, line, candidates);
        
        // 应该匹配 /help
        assertFalse(candidates.isEmpty());
        assertTrue(candidates.stream().anyMatch(c -> c.value().equals("/help")));
    }
    
    @Test
    void testAliasCompletion() {
        // 测试别名补全
        ParsedLine line = mockParsedLine("/h", 0);
        List<Candidate> candidates = new ArrayList<>();
        
        completer.complete(lineReader, line, candidates);
        
        // 应该匹配 /help (别名 h)
        assertTrue(candidates.stream().anyMatch(c -> c.value().equals("/help")));
    }
    
    @Test
    void testFilePathCompletion() {
        // 测试文件路径补全
        ParsedLine line = mockParsedLine("@test", 0);
        List<Candidate> candidates = new ArrayList<>();
        
        completer.complete(lineReader, line, candidates);
        
        // 应该找到 test.java
        assertTrue(candidates.stream().anyMatch(c -> c.value().contains("test.java")));
    }
    
    @Test
    void testDirectoryCompletion() {
        // 测试目录补全
        ParsedLine line = mockParsedLine("@s", 0);
        List<Candidate> candidates = new ArrayList<>();
        
        completer.complete(lineReader, line, candidates);
        
        // 应该找到 src/ 目录
        assertTrue(candidates.stream().anyMatch(c -> c.value().contains("src")));
    }
    
    @Test
    void testCommonPhraseCompletion() {
        // 测试常用短语补全
        ParsedLine line = mockParsedLine("hel", 0);
        List<Candidate> candidates = new ArrayList<>();
        
        completer.complete(lineReader, line, candidates);
        
        // 应该匹配 "help me"
        assertTrue(candidates.stream().anyMatch(c -> c.value().equals("help me")));
    }
    
    @Test
    void testNoCompletionForMiddleOfLine() {
        // 测试在行中间不应该补全常用短语
        ParsedLine line = mockParsedLine("some text hel", 2);
        List<Candidate> candidates = new ArrayList<>();
        
        completer.complete(lineReader, line, candidates);
        
        // 不应该有常用短语补全
        assertEquals(0, candidates.size());
    }
    
    @Test
    void testEmptyMetaCommand() {
        // 测试输入单独的 / 应该列出所有命令
        ParsedLine line = mockParsedLine("/", 0);
        List<Candidate> candidates = new ArrayList<>();
        
        completer.complete(lineReader, line, candidates);
        
        // 应该列出所有命令
        assertTrue(candidates.size() >= 4); // help, status, config, tools
    }
    
    /**
     * 创建模拟的 CommandHandler
     */
    private CommandHandler createMockHandler(String name, String desc, String... aliases) {
        CommandHandler handler = mock(CommandHandler.class);
        when(handler.getName()).thenReturn(name);
        when(handler.getDescription()).thenReturn(desc);
        when(handler.getAliases()).thenReturn(List.of(aliases));
        when(handler.isAvailable(any())).thenReturn(true);
        return handler;
    }
    
    /**
     * 创建模拟的 ParsedLine
     */
    private ParsedLine mockParsedLine(String line, int wordIndex) {
        ParsedLine parsedLine = mock(ParsedLine.class);
        when(parsedLine.line()).thenReturn(line);
        when(parsedLine.word()).thenReturn(line.trim().split("\\s+")[Math.min(wordIndex, line.trim().split("\\s+").length - 1)]);
        when(parsedLine.wordIndex()).thenReturn(wordIndex);
        return parsedLine;
    }
}
