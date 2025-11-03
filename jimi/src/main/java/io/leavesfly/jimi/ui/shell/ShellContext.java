package io.leavesfly.jimi.ui.shell;

import io.leavesfly.jimi.soul.JimiSoul;
import io.leavesfly.jimi.ui.shell.output.OutputFormatter;
import lombok.Builder;
import lombok.Getter;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;


@Getter
@Builder
public class ShellContext {

    /**
     * JimiSoul 实例
     */
    private final JimiSoul soul;

    /**
     * 终端实例
     */
    private final Terminal terminal;

    /**
     * LineReader 实例
     */
    private final LineReader lineReader;

    /**
     * 原始输入字符串
     */
    private final String rawInput;


    /**
     * 输出格式化器
     */
    private final OutputFormatter outputFormatter;


}
