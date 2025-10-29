package io.leavesfly.jimi.ui.shell;

import org.jline.reader.EOFError;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;

/**
 * Jimi 命令解析器
 * 解析用户输入的命令
 */
public class JimiParser implements Parser {
    
    private final DefaultParser defaultParser;
    
    public JimiParser() {
        this.defaultParser = new DefaultParser();
        // 禁用转义字符处理
        this.defaultParser.setEscapeChars(null);
        // 禁用引号处理（简化输入）
        this.defaultParser.setQuoteChars(null);
    }
    
    @Override
    public ParsedLine parse(String line, int cursor, ParseContext context) {
        return defaultParser.parse(line, cursor, context);
    }
    
    @Override
    public boolean isEscapeChar(char ch) {
        return false;
    }
    
    @Override
    public boolean validCommandName(String name) {
        return true;
    }
    
    @Override
    public boolean validVariableName(String name) {
        return true;
    }
    
    @Override
    public String getCommand(String line) {
        return defaultParser.getCommand(line);
    }
    
    @Override
    public String getVariable(String line) {
        return defaultParser.getVariable(line);
    }
}
