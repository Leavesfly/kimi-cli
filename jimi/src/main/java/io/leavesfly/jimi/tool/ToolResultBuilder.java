package io.leavesfly.jimi.tool;

/**
 * 工具结果构建器
 * 用于构建带输出限制的工具结果
 */
public class ToolResultBuilder {
    
    private static final int DEFAULT_MAX_CHARS = 50_000;
    private static final int DEFAULT_MAX_LINE_LENGTH = 2000;
    private static final String MARKER = "[...truncated]";
    
    private final int maxChars;
    private final Integer maxLineLength;
    private final StringBuilder buffer;
    private int nChars;
    private int nLines;
    private boolean truncationHappened;
    
    public ToolResultBuilder() {
        this(DEFAULT_MAX_CHARS, DEFAULT_MAX_LINE_LENGTH);
    }
    
    public ToolResultBuilder(int maxChars, Integer maxLineLength) {
        this.maxChars = maxChars;
        this.maxLineLength = maxLineLength;
        this.buffer = new StringBuilder();
        this.nChars = 0;
        this.nLines = 0;
        this.truncationHappened = false;
    }
    
    /**
     * 写入文本到输出缓冲区
     * 
     * @param text 要写入的文本
     * @return 实际写入的字符数
     */
    public int write(String text) {
        if (isFull()) {
            return 0;
        }
        
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        String[] lines = text.split("\n", -1);
        int charsWritten = 0;
        
        for (int i = 0; i < lines.length; i++) {
            if (isFull()) {
                break;
            }
            
            String line = lines[i];
            if (i < lines.length - 1) {
                line += "\n";
            }
            
            String originalLine = line;
            int remainingChars = maxChars - nChars;
            int limit = maxLineLength != null 
                    ? Math.min(remainingChars, maxLineLength) 
                    : remainingChars;
            
            line = truncateLine(line, limit);
            if (!line.equals(originalLine)) {
                truncationHappened = true;
            }
            
            buffer.append(line);
            charsWritten += line.length();
            nChars += line.length();
            if (line.endsWith("\n")) {
                nLines++;
            }
        }
        
        return charsWritten;
    }
    
    /**
     * 创建成功结果
     */
    public ToolResult ok(String message) {
        return ok(message, "");
    }
    
    /**
     * 创建成功结果（带简要描述）
     */
    public ToolResult ok(String message, String brief) {
        String output = buffer.toString();
        String finalMessage = message;
        
        if (!finalMessage.isEmpty() && !finalMessage.endsWith(".")) {
            finalMessage += ".";
        }
        
        if (truncationHappened) {
            String truncationMsg = "输出被截断以适应消息限制";
            if (!finalMessage.isEmpty()) {
                finalMessage += " " + truncationMsg + ".";
            } else {
                finalMessage = truncationMsg + ".";
            }
        }
        
        return ToolResult.ok(output, finalMessage, brief);
    }
    
    /**
     * 创建错误结果
     */
    public ToolResult error(String message, String brief) {
        String output = buffer.toString();
        String finalMessage = message;
        
        if (truncationHappened) {
            String truncationMsg = "输出被截断以适应消息限制";
            finalMessage = finalMessage.isEmpty() 
                    ? truncationMsg 
                    : finalMessage + " " + truncationMsg + ".";
        }
        
        return ToolResult.error(output, finalMessage, brief);
    }
    
    /**
     * 截断行
     */
    private String truncateLine(String line, int maxLength) {
        if (line.length() <= maxLength) {
            return line;
        }
        
        // 保留行尾的换行符
        String linebreak = "";
        if (line.endsWith("\r\n")) {
            linebreak = "\r\n";
        } else if (line.endsWith("\n")) {
            linebreak = "\n";
        } else if (line.endsWith("\r")) {
            linebreak = "\r";
        }
        
        String end = MARKER + linebreak;
        maxLength = Math.max(maxLength, end.length());
        return line.substring(0, maxLength - end.length()) + end;
    }
    
    /**
     * 检查缓冲区是否已满
     */
    public boolean isFull() {
        return nChars >= maxChars;
    }
    
    /**
     * 获取当前字符数
     */
    public int getNChars() {
        return nChars;
    }
    
    /**
     * 获取当前行数
     */
    public int getNLines() {
        return nLines;
    }
}
