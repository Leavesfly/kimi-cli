package io.leavesfly.jimi.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 会话管理服务
 * 负责创建、恢复和管理会话
 */
@Slf4j
@Service
public class SessionManager {
    
    private static final String METADATA_FILE = "metadata.json";
    private final ObjectMapper objectMapper;
    
    public SessionManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * 创建新会话
     */
    public Session createSession(Path workDir) {
        log.debug("Creating new session for work directory: {}", workDir);
        
        Metadata metadata = loadMetadata();
        WorkDirMetadata workDirMeta = findOrCreateWorkDirMetadata(metadata, workDir);
        
        String sessionId = UUID.randomUUID().toString();
        Path historyFile = workDirMeta.getSessionsDir().resolve(sessionId + ".jsonl");
        
        try {
            Files.createDirectories(historyFile.getParent());
            if (Files.exists(historyFile)) {
                log.warn("History file already exists, truncating: {}", historyFile);
                Files.delete(historyFile);
            }
            Files.createFile(historyFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create history file: " + historyFile, e);
        }
        
        workDirMeta.setLastSessionId(sessionId);
        workDirMeta.getSessionIds().add(sessionId);
        saveMetadata(metadata);
        
        return Session.builder()
                     .id(sessionId)
                     .workDir(workDir.toAbsolutePath())
                     .historyFile(historyFile)
                     .build();
    }
    
    /**
     * 继续上次会话
     */
    public Optional<Session> continueSession(Path workDir) {
        log.debug("Continuing session for work directory: {}", workDir);
        
        Metadata metadata = loadMetadata();
        Optional<WorkDirMetadata> workDirMeta = findWorkDirMetadata(metadata, workDir);
        
        if (workDirMeta.isEmpty()) {
            log.debug("Work directory never been used");
            return Optional.empty();
        }
        
        String lastSessionId = workDirMeta.get().getLastSessionId();
        if (lastSessionId == null) {
            log.debug("Work directory never had a session");
            return Optional.empty();
        }
        
        Path historyFile = workDirMeta.get().getSessionsDir().resolve(lastSessionId + ".jsonl");
        
        return Optional.of(Session.builder()
                                  .id(lastSessionId)
                                  .workDir(workDir.toAbsolutePath())
                                  .historyFile(historyFile)
                                  .build());
    }
    
    private Metadata loadMetadata() {
        Path metadataPath = getMetadataPath();
        if (Files.exists(metadataPath)) {
            try {
                return objectMapper.readValue(metadataPath.toFile(), Metadata.class);
            } catch (IOException e) {
                log.warn("Failed to load metadata, using empty metadata", e);
                return new Metadata();
            }
        }
        return new Metadata();
    }
    
    private void saveMetadata(Metadata metadata) {
        Path metadataPath = getMetadataPath();
        try {
            Files.createDirectories(metadataPath.getParent());
            objectMapper.writerWithDefaultPrettyPrinter()
                       .writeValue(metadataPath.toFile(), metadata);
        } catch (IOException e) {
            log.error("Failed to save metadata", e);
        }
    }
    
    private Path getMetadataPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".kimi-cli", METADATA_FILE);
    }
    
    private WorkDirMetadata findOrCreateWorkDirMetadata(Metadata metadata, Path workDir) {
        String workDirStr = workDir.toAbsolutePath().toString();
        return findWorkDirMetadata(metadata, workDir)
                .orElseGet(() -> {
                    WorkDirMetadata newMeta = WorkDirMetadata.builder()
                                                            .path(workDirStr)
                                                            .sessionIds(new ArrayList<>())
                                                            .build();
                    metadata.getWorkDirs().add(newMeta);
                    return newMeta;
                });
    }
    
    private Optional<WorkDirMetadata> findWorkDirMetadata(Metadata metadata, Path workDir) {
        String workDirStr = workDir.toAbsolutePath().toString();
        return metadata.getWorkDirs().stream()
                      .filter(wd -> wd.getPath().equals(workDirStr))
                      .findFirst();
    }
    
    /**
     * 元数据容器
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    static class Metadata {
        @com.fasterxml.jackson.annotation.JsonProperty("work_dirs")
        private List<WorkDirMetadata> workDirs = new ArrayList<>();
    }
}
