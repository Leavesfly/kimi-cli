package io.leavesfly.jimi.ui.shell;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Shell UI 演示程序
 * 展示 JLine 交互式界面的功能
 */
public class ShellUIDemo {

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("Shell UI 演示程序");
        System.out.println("=".repeat(80));
        System.out.println();

        try {

            System.out.println("\n" + "=".repeat(80));
            System.out.println("演示完成！");
            System.out.println("=".repeat(80));

        } catch (Exception e) {
            System.err.println("演示失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 打印功能特性
     */
    private static void printFeatures() {
        System.out.println("Shell UI 功能特性：");
        System.out.println("  • 富文本彩色输出");
        System.out.println("  • 命令历史记录（上下箭头）");
        System.out.println("  • Tab 自动补全");
        System.out.println("  • 语法高亮");
        System.out.println("  • Wire 消息实时显示");
        System.out.println("  • 元命令支持 (/help, /status, etc.)");
        System.out.println("  • Ctrl-C 中断，Ctrl-D 退出");
    }

    /**
     * 打印使用说明
     */
    private static void printUsage() {
        System.out.println("\n可用命令：");
        System.out.println("  /help     - 显示帮助信息");
        System.out.println("  /status   - 显示当前状态");
        System.out.println("  /clear    - 清屏");
        System.out.println("  /history  - 显示命令历史");
        System.out.println("  exit/quit - 退出程序");
        System.out.println("\n或者直接输入消息与 Jimi 对话！");
    }

    /**
     * 删除目录（递归）
     */
    private static void deleteDirectory(Path directory) {
        try {
            if (Files.exists(directory)) {
                Files.walk(directory)
                        .sorted((a, b) -> -a.compareTo(b))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (Exception e) {
                                // Ignore
                            }
                        });
            }
        } catch (Exception e) {
            // Ignore
        }
    }
}
