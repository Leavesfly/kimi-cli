package io.leavesfly.jimi.tool;

import io.leavesfly.jimi.soul.runtime.BuiltinSystemPromptArgs;
import io.leavesfly.jimi.tool.file.ReadFile;
import io.leavesfly.jimi.tool.think.Think;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tool工具系统演示测试
 * 展示工具的创建和使用
 */
@DisplayName("Tool工具系统演示")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ToolDemo {
    
    @TempDir
    static Path tempDir;
    
    @Test
    @Order(1)
    @DisplayName("演示1：Think工具基本使用")
    void demo1_thinkTool() {
        System.out.println("\n=== 演示1：Think工具基本使用 ===\n");
        
        // 创建Think工具
        Think think = new Think();
        
        System.out.println("工具信息：");
        System.out.println("  名称: " + think.getName());
        System.out.println("  描述: " + think.getDescription());
        System.out.println("  参数类型: " + think.getParamsType().getSimpleName());
        
        // 执行工具
        Think.Params params = Think.Params.builder()
                .thought("我需要先分析用户的需求，然后制定执行计划")
                .build();
        
        System.out.println("\n执行Think工具：");
        System.out.println("  思考内容: " + params.getThought());
        
        ToolResult result = think.execute(params).block();
        
        System.out.println("\n执行结果：");
        System.out.println("  类型: " + result.getType());
        System.out.println("  消息: " + result.getMessage());
        System.out.println("  输出: " + (result.getOutput().isEmpty() ? "(无输出)" : result.getOutput()));
        
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isOk());
        Assertions.assertEquals("", result.getOutput());
        
        System.out.println("\n✅ 演示1完成\n");
    }
    
    @Test
    @Order(2)
    @DisplayName("演示2：ToolResult结果类型")
    void demo2_toolResults() {
        System.out.println("\n=== 演示2：ToolResult结果类型 ===\n");
        
        // 成功结果
        ToolResult ok = ToolResult.ok("输出内容", "操作成功完成");
        System.out.println("成功结果：");
        System.out.println("  类型: " + ok.getType());
        System.out.println("  消息: " + ok.getMessage());
        System.out.println("  是否成功: " + ok.isOk());
        
        // 错误结果
        ToolResult error = ToolResult.error("错误信息", "操作失败");
        System.out.println("\n错误结果：");
        System.out.println("  类型: " + error.getType());
        System.out.println("  消息: " + error.getMessage());
        System.out.println("  简要: " + error.getBrief());
        System.out.println("  是否错误: " + error.isError());
        
        // 拒绝结果
        ToolResult rejected = ToolResult.rejected();
        System.out.println("\n拒绝结果：");
        System.out.println("  类型: " + rejected.getType());
        System.out.println("  消息: " + rejected.getMessage());
        System.out.println("  简要: " + rejected.getBrief());
        System.out.println("  是否被拒绝: " + rejected.isRejected());
        
        Assertions.assertTrue(ok.isOk());
        Assertions.assertTrue(error.isError());
        Assertions.assertTrue(rejected.isRejected());
        
        System.out.println("\n✅ 演示2完成\n");
    }
    
    @Test
    @Order(3)
    @DisplayName("演示3：ToolResultBuilder构建器")
    void demo3_toolResultBuilder() {
        System.out.println("\n=== 演示3：ToolResultBuilder构建器 ===\n");
        
        ToolResultBuilder builder = new ToolResultBuilder();
        
        System.out.println("写入测试数据：");
        String line1 = "第一行输出\n";
        String line2 = "第二行输出\n";
        String line3 = "第三行输出\n";
        
        int written1 = builder.write(line1);
        int written2 = builder.write(line2);
        int written3 = builder.write(line3);
        
        System.out.println("  写入1: " + written1 + " 字符");
        System.out.println("  写入2: " + written2 + " 字符");
        System.out.println("  写入3: " + written3 + " 字符");
        System.out.println("  总字符数: " + builder.getNChars());
        System.out.println("  总行数: " + builder.getNLines());
        
        ToolResult result = builder.ok("数据写入完成");
        
        System.out.println("\n构建的结果：");
        System.out.println("  输出内容:");
        System.out.println(result.getOutput());
        System.out.println("  消息: " + result.getMessage());
        
        Assertions.assertTrue(result.isOk());
        Assertions.assertEquals(3, builder.getNLines());
        
        System.out.println("✅ 演示3完成\n");
    }
    
    @Test
    @Order(4)
    @DisplayName("演示4：ToolResultBuilder截断测试")
    void demo4_truncation() {
        System.out.println("\n=== 演示4：ToolResultBuilder截断测试 ===\n");
        
        // 创建限制为100字符的构建器
        ToolResultBuilder builder = new ToolResultBuilder(100, 30);
        
        // 写入超长行（确保超过30字符）
        String longLine = "这是一个非常非常非常非常非常非常非常非常非常非常长的行，应该会被截断\n";
        System.out.println("原始行长度: " + longLine.length());
        
        int written = builder.write(longLine);
        System.out.println("写入后字符数: " + builder.getNChars());
        System.out.println("实际写入: " + written + " 字符");
        
        ToolResult result = builder.ok("写入完成");
        
        System.out.println("\n结果：");
        System.out.println("  最终字符数: " + builder.getNChars());
        System.out.println("  消息: " + result.getMessage());
        System.out.println("  包含截断提示: " + result.getMessage().contains("截断"));
        
        // 只要行被截断了，就应该有提示
        Assertions.assertTrue(result.getMessage().contains("截断") || longLine.length() <= 30);
        
        System.out.println("\n✅ 演示4完成\n");
    }
    
    @Test
    @Order(5)
    @DisplayName("演示5：ReadFile工具读取文件")
    void demo5_readFile() throws Exception {
        System.out.println("\n=== 演示5：ReadFile工具读取文件 ===\n");
        
        // 创建测试文件
        Path testFile = tempDir.resolve("test.txt");
        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= 20; i++) {
            content.append("这是第").append(i).append("行内容\n");
        }
        Files.writeString(testFile, content.toString());
        System.out.println("✓ 创建测试文件: " + testFile);
        System.out.println("  文件行数: 20");
        
        // 创建BuiltinArgs
        BuiltinSystemPromptArgs builtinArgs = BuiltinSystemPromptArgs.builder()
                .kimiWorkDir(tempDir)
                .build();
        
        // 创建ReadFile工具
        ReadFile readFile = new ReadFile(builtinArgs);
        
        System.out.println("\n工具信息：");
        System.out.println("  名称: " + readFile.getName());
        System.out.println("  描述预览: " + readFile.getDescription().substring(0, 50) + "...");
        
        // 读取文件（前10行）
        ReadFile.Params params = ReadFile.Params.builder()
                .path(testFile.toString())
                .lineOffset(1)
                .nLines(10)
                .build();
        
        System.out.println("\n执行读取：");
        System.out.println("  路径: " + params.getPath());
        System.out.println("  起始行: " + params.getLineOffset());
        System.out.println("  行数: " + params.getNLines());
        
        ToolResult result = readFile.execute(params).block();
        
        System.out.println("\n结果：");
        System.out.println("  类型: " + result.getType());
        System.out.println("  消息: " + result.getMessage());
        System.out.println("\n输出内容（前5行）：");
        String[] lines = result.getOutput().split("\n");
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            System.out.println("  " + lines[i]);
        }
        if (lines.length > 5) {
            System.out.println("  ... (" + (lines.length - 5) + " 行省略)");
        }
        
        Assertions.assertTrue(result.isOk());
        Assertions.assertTrue(result.getOutput().contains("第1行内容"));
        Assertions.assertTrue(result.getOutput().contains("第10行内容"));
        
        System.out.println("\n✅ 演示5完成\n");
    }
    
    @Test
    @Order(6)
    @DisplayName("演示6：ReadFile读取部分行")
    void demo6_readFilePartial() throws Exception {
        System.out.println("\n=== 演示6：ReadFile读取部分行 ===\n");
        
        // 创建大文件
        Path testFile = tempDir.resolve("large.txt");
        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= 100; i++) {
            content.append(String.format("行 %03d: 这是测试内容\n", i));
        }
        Files.writeString(testFile, content.toString());
        System.out.println("✓ 创建大文件: 100行");
        
        BuiltinSystemPromptArgs builtinArgs = BuiltinSystemPromptArgs.builder()
                .kimiWorkDir(tempDir)
                .build();
        
        ReadFile readFile = new ReadFile(builtinArgs);
        
        // 读取中间部分（第50-60行）
        ReadFile.Params params = ReadFile.Params.builder()
                .path(testFile.toString())
                .lineOffset(50)
                .nLines(10)
                .build();
        
        System.out.println("读取参数：");
        System.out.println("  起始行: " + params.getLineOffset());
        System.out.println("  行数: " + params.getNLines());
        
        ToolResult result = readFile.execute(params).block();
        
        System.out.println("\n结果：");
        System.out.println("  消息: " + result.getMessage());
        
        String[] lines = result.getOutput().split("\n");
        System.out.println("  实际读取行数: " + lines.length);
        System.out.println("\n前3行：");
        for (int i = 0; i < Math.min(3, lines.length); i++) {
            System.out.println("  " + lines[i]);
        }
        
        Assertions.assertTrue(result.isOk());
        Assertions.assertTrue(result.getOutput().contains("行 050"));
        Assertions.assertTrue(result.getOutput().contains("行 059"));
        
        System.out.println("\n✅ 演示6完成\n");
    }
    
    @Test
    @Order(7)
    @DisplayName("演示7：ReadFile错误处理")
    void demo7_readFileErrors() {
        System.out.println("\n=== 演示7：ReadFile错误处理 ===\n");
        
        BuiltinSystemPromptArgs builtinArgs = BuiltinSystemPromptArgs.builder()
                .kimiWorkDir(tempDir)
                .build();
        
        ReadFile readFile = new ReadFile(builtinArgs);
        
        // 测试1：文件不存在
        System.out.println("测试1：文件不存在");
        ReadFile.Params params1 = ReadFile.Params.builder()
                .path(tempDir.resolve("notexist.txt").toString())
                .build();
        
        ToolResult result1 = readFile.execute(params1).block();
        System.out.println("  结果: " + result1.getType());
        System.out.println("  消息: " + result1.getMessage());
        Assertions.assertTrue(result1.isError());
        Assertions.assertTrue(result1.getMessage().contains("不存在"));
        
        // 测试2：相对路径错误
        System.out.println("\n测试2：相对路径错误");
        ReadFile.Params params2 = ReadFile.Params.builder()
                .path("relative/path.txt")
                .build();
        
        ToolResult result2 = readFile.execute(params2).block();
        System.out.println("  结果: " + result2.getType());
        System.out.println("  消息: " + result2.getMessage());
        Assertions.assertTrue(result2.isError());
        Assertions.assertTrue(result2.getMessage().contains("绝对路径"));
        
        System.out.println("\n✅ 演示7完成\n");
    }
}
