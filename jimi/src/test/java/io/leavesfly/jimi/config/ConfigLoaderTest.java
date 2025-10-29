package io.leavesfly.jimi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.leavesfly.jimi.exception.ConfigException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigLoader 单元测试
 */
class ConfigLoaderTest {
    
    private ConfigLoader configLoader;
    private ObjectMapper objectMapper;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        configLoader = new ConfigLoader(objectMapper);
    }
    
    @Test
    void testGetDefaultConfig() {
        JimiConfig config = configLoader.getDefaultConfig();
        
        assertNotNull(config);
        assertEquals("", config.getDefaultModel());
        assertNotNull(config.getModels());
        assertNotNull(config.getProviders());
        assertNotNull(config.getLoopControl());
        assertEquals(100, config.getLoopControl().getMaxStepsPerRun());
        assertEquals(3, config.getLoopControl().getMaxRetriesPerStep());
    }
    
    @Test
    void testSaveAndLoadConfig() {
        JimiConfig config = JimiConfig.builder()
                                     .defaultModel("test-model")
                                     .build();
        
        Path configFile = tempDir.resolve("test-config.json");
        configLoader.saveConfig(config, configFile);
        
        assertTrue(Files.exists(configFile));
        
        // TODO: 完整的加载测试需要修改 loadConfig 方法签名
        // 这里仅验证文件创建成功
    }
    
    @Test
    void testConfigValidation() {
        JimiConfig config = configLoader.getDefaultConfig();
        
        // 默认配置应该有效
        assertDoesNotThrow(config::validate);
    }
}
