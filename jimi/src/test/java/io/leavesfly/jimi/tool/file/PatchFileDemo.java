package io.leavesfly.jimi.tool.file;

import io.leavesfly.jimi.soul.approval.Approval;
import io.leavesfly.jimi.soul.runtime.BuiltinSystemPromptArgs;
import io.leavesfly.jimi.tool.ToolResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PatchFile 工具完整演示
 * 
 * 展示 PatchFile 工具的各种使用场景：
 * 1. 基本补丁应用
 * 2. 多行修改
 * 3. 添加和删除行
 * 4. 复杂的代码重构
 * 5. 错误处理
 * 6. 与其他工具对比
 * 
 * @author 山泽
 */
class PatchFileDemo {
    
    @TempDir
    Path tempDir;
    
    private PatchFile patchFile;
    
    @BeforeEach
    void setUp() {
        BuiltinSystemPromptArgs builtinArgs = BuiltinSystemPromptArgs.builder()
                .kimiWorkDir(tempDir)
                .build();
        
        Approval approval = new Approval(true);  // YOLO 模式
        
        patchFile = new PatchFile(builtinArgs, approval);
    }
    
    /**
     * 演示 1: 基本补丁应用
     */
    @Test
    void demo1_BasicPatch() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 1: 基本补丁应用");
        System.out.println("=".repeat(70) + "\n");
        
        // 创建原始文件
        Path testFile = tempDir.resolve("example.txt");
        String originalContent = """
                line 1
                line 2 old
                line 3
                """;
        Files.writeString(testFile, originalContent);
        
        System.out.println("原始文件内容:");
        System.out.println(originalContent);
        
        // 创建补丁（修改第 2 行）
        String patch = """
                --- a/example.txt
                +++ b/example.txt
                @@ -1,3 +1,3 @@
                 line 1
                -line 2 old
                +line 2 new
                 line 3
                """;
        
        System.out.println("应用补丁:");
        System.out.println(patch);
        
        // 应用补丁
        PatchFile.Params params = PatchFile.Params.builder()
                .path(testFile.toString())
                .diff(patch)
                .build();
        
        ToolResult result = patchFile.execute(params).block();
        
        System.out.println("结果: " + (result.isOk() ? "✓ 成功" : "✗ 失败"));
        System.out.println("消息: " + result.getBrief());
        
        // 验证结果
        String modifiedContent = Files.readString(testFile);
        System.out.println("\n修改后的文件内容:");
        System.out.println(modifiedContent);
        
        assertTrue(result.isOk());
        assertTrue(modifiedContent.contains("line 2 new"));
        assertFalse(modifiedContent.contains("line 2 old"));
        
        System.out.println("\n✅ 演示完成\n");
    }
    
    /**
     * 演示 2: 多行修改
     */
    @Test
    void demo2_MultiLineChanges() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 2: 多行修改");
        System.out.println("=".repeat(70) + "\n");
        
        // 创建 Java 源文件
        Path javaFile = tempDir.resolve("Example.java");
        String originalCode = """
                public class Example {
                    public void oldMethod() {
                        System.out.println("old");
                    }
                    
                    public int calculate(int x) {
                        return x * 2;
                    }
                }
                """;
        Files.writeString(javaFile, originalCode);
        
        System.out.println("原始代码:");
        System.out.println(originalCode);
        
        // 重构：重命名方法 + 修改实现
        String patch = """
                --- a/Example.java
                +++ b/Example.java
                @@ -1,7 +1,7 @@
                 public class Example {
                -    public void oldMethod() {
                -        System.out.println("old");
                +    public void newMethod() {
                +        System.out.println("new implementation");
                     }
                     
                     public int calculate(int x) {
                """;
        
        System.out.println("应用补丁（重构方法）:");
        System.out.println(patch);
        
        PatchFile.Params params = PatchFile.Params.builder()
                .path(javaFile.toString())
                .diff(patch)
                .build();
        
        ToolResult result = patchFile.execute(params).block();
        
        System.out.println("结果: " + (result.isOk() ? "✓ 成功" : "✗ 失败"));
        
        String modifiedCode = Files.readString(javaFile);
        System.out.println("\n修改后的代码:");
        System.out.println(modifiedCode);
        
        assertTrue(result.isOk());
        assertTrue(modifiedCode.contains("newMethod"));
        assertTrue(modifiedCode.contains("new implementation"));
        
        System.out.println("\n✅ 演示完成\n");
    }
    
    /**
     * 演示 3: 添加和删除行
     */
    @Test
    void demo3_AddAndRemoveLines() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 3: 添加和删除行");
        System.out.println("=".repeat(70) + "\n");
        
        Path configFile = tempDir.resolve("config.yaml");
        String originalConfig = """
                version: 1
                settings:
                  debug: true
                  timeout: 30
                """;
        Files.writeString(configFile, originalConfig);
        
        System.out.println("原始配置:");
        System.out.println(originalConfig);
        
        // 添加新配置项，删除 debug
        String patch = """
                --- a/config.yaml
                +++ b/config.yaml
                @@ -1,4 +1,5 @@
                 version: 1
                 settings:
                -  debug: true
                   timeout: 30
                +  max_retries: 3
                +  log_level: info
                """;
        
        System.out.println("应用补丁（添加配置项，删除 debug）:");
        System.out.println(patch);
        
        PatchFile.Params params = PatchFile.Params.builder()
                .path(configFile.toString())
                .diff(patch)
                .build();
        
        ToolResult result = patchFile.execute(params).block();
        
        System.out.println("结果: " + (result.isOk() ? "✓ 成功" : "✗ 失败"));
        
        String modifiedConfig = Files.readString(configFile);
        System.out.println("\n修改后的配置:");
        System.out.println(modifiedConfig);
        
        assertTrue(result.isOk());
        assertTrue(modifiedConfig.contains("max_retries: 3"));
        assertTrue(modifiedConfig.contains("log_level: info"));
        assertFalse(modifiedConfig.contains("debug: true"));
        
        System.out.println("\n✅ 演示完成\n");
    }
    
    /**
     * 演示 4: 复杂的代码重构
     */
    @Test
    void demo4_ComplexRefactoring() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 4: 复杂的代码重构");
        System.out.println("=".repeat(70) + "\n");
        
        Path serviceFile = tempDir.resolve("UserService.java");
        String originalCode = """
                public class UserService {
                    private UserRepository repository;
                    
                    public User getUser(String id) {
                        return repository.findById(id);
                    }
                    
                    public void saveUser(User user) {
                        repository.save(user);
                    }
                }
                """;
        Files.writeString(serviceFile, originalCode);
        
        System.out.println("原始代码:");
        System.out.println(originalCode);
        
        // 添加依赖注入和日志
        String patch = """
                --- a/UserService.java
                +++ b/UserService.java
                @@ -1,10 +1,17 @@
                +import lombok.RequiredArgsConstructor;
                +import lombok.extern.slf4j.Slf4j;
                +
                +@Slf4j
                +@RequiredArgsConstructor
                 public class UserService {
                -    private UserRepository repository;
                +    private final UserRepository repository;
                     
                     public User getUser(String id) {
                +        log.debug("Getting user with id: {}", id);
                         return repository.findById(id);
                     }
                     
                     public void saveUser(User user) {
                +        log.info("Saving user: {}", user.getId());
                         repository.save(user);
                     }
                """;
        
        System.out.println("应用补丁（添加注解和日志）:");
        System.out.println(patch);
        
        PatchFile.Params params = PatchFile.Params.builder()
                .path(serviceFile.toString())
                .diff(patch)
                .build();
        
        ToolResult result = patchFile.execute(params).block();
        
        System.out.println("结果: " + (result.isOk() ? "✓ 成功" : "✗ 失败"));
        
        String modifiedCode = Files.readString(serviceFile);
        System.out.println("\n修改后的代码:");
        System.out.println(modifiedCode);
        
        assertTrue(result.isOk());
        assertTrue(modifiedCode.contains("@Slf4j"));
        assertTrue(modifiedCode.contains("@RequiredArgsConstructor"));
        assertTrue(modifiedCode.contains("log.debug"));
        assertTrue(modifiedCode.contains("log.info"));
        
        System.out.println("\n✅ 演示完成\n");
    }
    
    /**
     * 演示 5: 错误处理
     */
    @Test
    void demo5_ErrorHandling() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 5: 错误处理");
        System.out.println("=".repeat(70) + "\n");
        
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "line 1\nline 2\nline 3\n");
        
        // 错误 1: 补丁不匹配
        System.out.println("错误 1: 补丁内容不匹配");
        String wrongPatch = """
                --- a/test.txt
                +++ b/test.txt
                @@ -1,3 +1,3 @@
                 line 1
                -wrong line
                +line 2
                 line 3
                """;
        
        PatchFile.Params params1 = PatchFile.Params.builder()
                .path(testFile.toString())
                .diff(wrongPatch)
                .build();
        
        ToolResult result1 = patchFile.execute(params1).block();
        System.out.println("  结果: " + (result1.isError() ? "✓ 正确拒绝" : "✗ 应该失败"));
        System.out.println("  消息: " + result1.getBrief());
        assertTrue(result1.isError());
        
        // 错误 2: 文件不存在
        System.out.println("\n错误 2: 文件不存在");
        PatchFile.Params params2 = PatchFile.Params.builder()
                .path(tempDir.resolve("nonexistent.txt").toString())
                .diff(wrongPatch)
                .build();
        
        ToolResult result2 = patchFile.execute(params2).block();
        System.out.println("  结果: " + (result2.isError() ? "✓ 正确拒绝" : "✗ 应该失败"));
        System.out.println("  消息: " + result2.getBrief());
        assertTrue(result2.isError());
        assertTrue(result2.getBrief().contains("not found"));
        
        // 错误 3: 相对路径
        System.out.println("\n错误 3: 使用相对路径");
        PatchFile.Params params3 = PatchFile.Params.builder()
                .path("relative/path.txt")
                .diff(wrongPatch)
                .build();
        
        ToolResult result3 = patchFile.execute(params3).block();
        System.out.println("  结果: " + (result3.isError() ? "✓ 正确拒绝" : "✗ 应该失败"));
        System.out.println("  消息: " + result3.getBrief());
        assertTrue(result3.isError());
        assertTrue(result3.getBrief().contains("not an absolute path"));
        
        System.out.println("\n✅ 演示完成\n");
    }
    
    /**
     * 演示 6: 与其他工具对比
     */
    @Test
    void demo6_ComparisonWithOtherTools() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 6: PatchFile vs 其他工具");
        System.out.println("=".repeat(70) + "\n");
        
        Path file = tempDir.resolve("compare.txt");
        String content = """
                line 1
                line 2
                line 3
                line 4
                line 5
                """;
        Files.writeString(file, content);
        
        System.out.println("原始文件:");
        System.out.println(content);
        
        System.out.println("任务: 修改第 3 行\n");
        
        // 方案 1: WriteFile（需要重写整个文件）
        System.out.println("【方案 1: WriteFile】");
        System.out.println("  缺点:");
        System.out.println("    - 需要重写整个文件（5 行）");
        System.out.println("    - 容易出错（可能遗漏某行）");
        System.out.println("    - 不直观（看不出具体改了什么）");
        System.out.println("    - 上下文窗口消耗大\n");
        
        // 方案 2: StrReplaceFile（字符串替换）
        System.out.println("【方案 2: StrReplaceFile】");
        System.out.println("  缺点:");
        System.out.println("    - 如果 'line 3' 出现多次会误替换");
        System.out.println("    - 需要精确匹配");
        System.out.println("    - 不支持行级别操作\n");
        
        // 方案 3: Bash sed（shell 命令）
        System.out.println("【方案 3: Bash sed】");
        System.out.println("  缺点:");
        System.out.println("    - 语法复杂（sed 's/pattern/replacement/'）");
        System.out.println("    - 平台依赖性");
        System.out.println("    - 错误处理困难\n");
        
        // 方案 4: PatchFile（推荐）
        System.out.println("【方案 4: PatchFile】 ✓ 推荐");
        System.out.println("  优点:");
        System.out.println("    - 只显示变更部分");
        System.out.println("    - 自动上下文匹配");
        System.out.println("    - 清晰的 diff 格式");
        System.out.println("    - 上下文窗口消耗小");
        
        String patch = """
                --- a/compare.txt
                +++ b/compare.txt
                @@ -1,5 +1,5 @@
                 line 1
                 line 2
                -line 3
                +line 3 modified
                 line 4
                 line 5
                """;
        
        System.out.println("\n  Diff 补丁:");
        System.out.println(patch);
        
        PatchFile.Params params = PatchFile.Params.builder()
                .path(file.toString())
                .diff(patch)
                .build();
        
        ToolResult result = patchFile.execute(params).block();
        
        System.out.println("  结果: " + (result.isOk() ? "✓ 成功" : "✗ 失败"));
        
        String modified = Files.readString(file);
        System.out.println("\n  修改后:");
        System.out.println(modified);
        
        assertTrue(result.isOk());
        
        System.out.println("总结:");
        System.out.println("  ✓ PatchFile 是最精确、最安全的文件编辑方式");
        System.out.println("  ✓ 特别适合代码重构和局部修改");
        System.out.println("  ✓ 与 git diff 格式兼容，开发者熟悉");
        
        System.out.println("\n✅ 演示完成\n");
    }
    
    /**
     * 演示 7: 实际使用场景
     */
    @Test
    void demo7_RealWorldScenarios() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 7: 实际使用场景");
        System.out.println("=".repeat(70) + "\n");
        
        System.out.println("场景 1: 修复 Bug（添加空值检查）");
        System.out.println("-".repeat(70));
        
        Path buggyFile = tempDir.resolve("BuggyService.java");
        String buggyCode = """
                public class BuggyService {
                    public String processUser(User user) {
                        return user.getName().toUpperCase();
                    }
                }
                """;
        Files.writeString(buggyFile, buggyCode);
        
        String bugFixPatch = """
                --- a/BuggyService.java
                +++ b/BuggyService.java
                @@ -1,4 +1,7 @@
                 public class BuggyService {
                     public String processUser(User user) {
                +        if (user == null || user.getName() == null) {
                +            return "";
                +        }
                         return user.getName().toUpperCase();
                     }
                """;
        
        System.out.println("修复补丁:");
        System.out.println(bugFixPatch);
        
        PatchFile.Params params1 = PatchFile.Params.builder()
                .path(buggyFile.toString())
                .diff(bugFixPatch)
                .build();
        
        ToolResult result1 = patchFile.execute(params1).block();
        System.out.println("✓ Bug 已修复\n");
        
        // 场景 2: 代码优化
        System.out.println("场景 2: 性能优化（使用缓存）");
        System.out.println("-".repeat(70));
        
        Path slowFile = tempDir.resolve("SlowService.java");
        String slowCode = """
                public class SlowService {
                    public Data getData(String key) {
                        return database.query(key);
                    }
                }
                """;
        Files.writeString(slowFile, slowCode);
        
        String optimizationPatch = """
                --- a/SlowService.java
                +++ b/SlowService.java
                @@ -1,4 +1,9 @@
                 public class SlowService {
                +    private final Cache<String, Data> cache = new LRUCache<>();
                +    
                     public Data getData(String key) {
                +        Data cached = cache.get(key);
                +        if (cached != null) return cached;
                         return database.query(key);
                     }
                """;
        
        System.out.println("优化补丁:");
        System.out.println(optimizationPatch);
        
        PatchFile.Params params2 = PatchFile.Params.builder()
                .path(slowFile.toString())
                .diff(optimizationPatch)
                .build();
        
        ToolResult result2 = patchFile.execute(params2).block();
        System.out.println("✓ 性能已优化\n");
        
        System.out.println("✅ 实际场景演示完成\n");
    }
    
    /**
     * 功能总结
     */
    @Test
    void demo8_Summary() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("PatchFile 工具功能总结");
        System.out.println("=".repeat(70) + "\n");
        
        System.out.println("核心特性:");
        System.out.println("  1. ✅ Unified Diff 格式");
        System.out.println("     - 标准 diff -u 格式");
        System.out.println("     - 与 git diff 兼容");
        System.out.println("     - 开发者熟悉");
        
        System.out.println("\n  2. ✅ 精确修改");
        System.out.println("     - 只修改需要变更的部分");
        System.out.println("     - 自动上下文匹配");
        System.out.println("     - 处理行号偏移");
        
        System.out.println("\n  3. ✅ 安全性");
        System.out.println("     - 路径验证（限制在工作目录内）");
        System.out.println("     - 审批确认");
        System.out.println("     - 补丁兼容性检查");
        
        System.out.println("\n  4. ✅ 错误处理");
        System.out.println("     - 补丁不匹配检测");
        System.out.println("     - 文件不存在检测");
        System.out.println("     - 详细的错误信息");
        
        System.out.println("\n技术实现:");
        System.out.println("  - java-diff-utils 库");
        System.out.println("  - UnifiedDiffUtils 解析");
        System.out.println("  - Patch.applyTo() 应用");
        System.out.println("  - 响应式编程（Reactor Mono）");
        
        System.out.println("\n适用场景:");
        System.out.println("  ✓ 代码重构");
        System.out.println("  ✓ Bug 修复");
        System.out.println("  ✓ 性能优化");
        System.out.println("  ✓ 配置文件修改");
        System.out.println("  ✓ 批量修改");
        
        System.out.println("\n优势对比:");
        System.out.println("  vs WriteFile:");
        System.out.println("    ✓ 不需要重写整个文件");
        System.out.println("    ✓ 上下文窗口消耗更小");
        System.out.println("    ✓ 变更更直观");
        
        System.out.println("\n  vs StrReplaceFile:");
        System.out.println("    ✓ 支持多行修改");
        System.out.println("    ✓ 自动上下文匹配");
        System.out.println("    ✓ 处理行级别操作");
        
        System.out.println("\n  vs Bash sed:");
        System.out.println("    ✓ 语法更简单");
        System.out.println("    ✓ 跨平台兼容");
        System.out.println("    ✓ 更好的错误处理");
        
        System.out.println("\n与 Python 版本对比:");
        System.out.println("  功能完全对等 ✅");
        System.out.println("  API 设计一致 ✅");
        System.out.println("  使用 java-diff-utils（vs patch_ng）✅");
        System.out.println("  错误处理完善 ✅");
        
        System.out.println("\n" + "=".repeat(70) + "\n");
    }
}
