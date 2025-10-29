package io.leavesfly.jimi.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.time.Instant;

/**
 * 会话实体
 * 表示一个工作目录下的会话
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    
    /**
     * 会话 ID（UUID 格式）
     */
    private String id;
    
    /**
     * 工作目录绝对路径
     */
    private Path workDir;
    
    /**
     * 历史记录文件路径
     */
    private Path historyFile;
    
    /**
     * 创建时间戳
     */
    @Builder.Default
    private Instant createdAt = Instant.now();
}
