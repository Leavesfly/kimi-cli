package io.leavesfly.jimi.tool.file;

import io.leavesfly.jimi.soul.runtime.BuiltinSystemPromptArgs;
import io.leavesfly.jimi.tool.AbstractTool;
import io.leavesfly.jimi.tool.ToolResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Glob 工具 - 使用 Glob 模式匹配文件
 * 支持文件和目录的匹配
 */
@Slf4j
public class Glob extends AbstractTool<Glob.Params> {
    
    private static final int MAX_MATCHES = 1000;
    
    private final Path workDir;
    
    /**
     * 参数模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Params {
        /**
         * Glob 模式
         */
        private String pattern;
        
        /**
         * 搜索目录的绝对路径（默认为工作目录）
         */
        private String directory;
        
        /**
         * 是否包含目录
         */
        @Builder.Default
        private boolean includeDirs = true;
    }
    
    public Glob(BuiltinSystemPromptArgs builtinArgs) {
        super(
            "Glob",
            String.format("Search for files/directories using glob patterns. Maximum %d matches returned.", MAX_MATCHES),
            Params.class
        );
        this.workDir = builtinArgs.getKimiWorkDir();
    }
    
    @Override
    public Mono<ToolResult> execute(Params params) {
        return Mono.defer(() -> {
            try {
                // 验证模式
                if (params.pattern.startsWith("**")) {
                    // 列出工作目录顶层
                    List<String> topLevel = new ArrayList<>();
                    try (Stream<Path> stream = Files.list(workDir)) {
                        stream.forEach(p -> topLevel.add(p.getFileName().toString()));
                    }
                    
                    return Mono.just(ToolResult.error(
                        String.join("\n", topLevel),
                        String.format("Pattern `%s` starts with '**' which is not allowed. " +
                            "This would recursively search all directories and may include large " +
                            "directories like `node_modules`. Use more specific patterns instead. " +
                            "For your convenience, a list of all files and directories in the " +
                            "top level of the working directory is provided below.", params.pattern),
                        "Unsafe pattern"
                    ));
                }
                
                // 确定搜索目录
                Path searchDir = params.directory != null ? Path.of(params.directory) : workDir;
                
                // 验证路径
                if (!searchDir.isAbsolute()) {
                    return Mono.just(ToolResult.error(
                        String.format("`%s` is not an absolute path. You must provide an absolute path to search.", params.directory),
                        "Invalid directory"
                    ));
                }
                
                ToolResult dirError = validateDirectory(searchDir);
                if (dirError != null) {
                    return Mono.just(dirError);
                }
                
                if (!Files.exists(searchDir)) {
                    return Mono.just(ToolResult.error(
                        String.format("`%s` does not exist.", params.directory),
                        "Directory not found"
                    ));
                }
                
                if (!Files.isDirectory(searchDir)) {
                    return Mono.just(ToolResult.error(
                        String.format("`%s` is not a directory.", params.directory),
                        "Invalid directory"
                    ));
                }
                
                // 执行 Glob 搜索
                List<Path> matches = new ArrayList<>();
                PathMatcher matcher = searchDir.getFileSystem().getPathMatcher("glob:" + params.pattern);
                
                try (Stream<Path> stream = Files.walk(searchDir)) {
                    stream.filter(p -> {
                        // 获取相对路径
                        Path relativePath = searchDir.relativize(p);
                        // 匹配模式
                        return matcher.matches(relativePath);
                    })
                    .filter(p -> params.includeDirs || Files.isRegularFile(p))
                    .sorted()
                    .limit(MAX_MATCHES + 1)
                    .forEach(matches::add);
                }
                
                // 生成结果
                String message;
                if (matches.isEmpty()) {
                    message = String.format("No matches found for pattern `%s`.", params.pattern);
                } else {
                    message = String.format("Found %d matches for pattern `%s`.", matches.size(), params.pattern);
                    if (matches.size() > MAX_MATCHES) {
                        matches = matches.subList(0, MAX_MATCHES);
                        message += String.format(" Only the first %d matches are returned. " +
                            "You may want to use a more specific pattern.", MAX_MATCHES);
                    }
                }
                
                // 构建输出（相对路径）
                List<String> output = new ArrayList<>();
                for (Path match : matches) {
                    output.add(searchDir.relativize(match).toString());
                }
                
                return Mono.just(ToolResult.ok(
                    String.join("\n", output),
                    message
                ));
                
            } catch (Exception e) {
                log.error("Failed to execute glob: {}", params.pattern, e);
                return Mono.just(ToolResult.error(
                    String.format("Failed to search for pattern %s. Error: %s", params.pattern, e.getMessage()),
                    "Glob failed"
                ));
            }
        });
    }
    
    /**
     * 验证目录安全性
     */
    private ToolResult validateDirectory(Path searchDir) {
        try {
            Path resolvedDir = searchDir.toRealPath();
            Path resolvedWorkDir = workDir.toRealPath();
            
            if (!resolvedDir.startsWith(resolvedWorkDir)) {
                return ToolResult.error(
                    String.format("`%s` is outside the working directory. You can only search within the working directory.", searchDir),
                    "Directory outside working directory"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to validate directory: {}", searchDir, e);
        }
        
        return null;
    }
}
