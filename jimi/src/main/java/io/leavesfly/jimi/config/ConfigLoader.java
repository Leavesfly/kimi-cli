package io.leavesfly.jimi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.leavesfly.jimi.exception.ConfigException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置加载服务
 * 负责从配置文件加载、保存和管理 Jimi 配置
 */
@Slf4j
@Service
public class ConfigLoader {
    
    private static final String CONFIG_FILE_NAME = "config.json";
    private static final String KIMI_CLI_DIR = ".kimi-cli";
    
    private final ObjectMapper objectMapper;
    
    public ConfigLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * 获取配置文件路径
     */
    public Path getConfigFilePath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, KIMI_CLI_DIR, CONFIG_FILE_NAME);
    }
    
    /**
     * 获取 Kimi CLI 数据目录路径
     */
    public Path getKimiCliDir() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, KIMI_CLI_DIR);
    }
    
    /**
     * 加载配置
     * 配置优先级：环境变量 > 自定义配置文件 > 默认配置文件 > 内置默认配置
     */
    public JimiConfig loadConfig(Path customConfigFile) {
        Path configFile = customConfigFile != null ? customConfigFile : getConfigFilePath();
        
        JimiConfig config;
        if (Files.exists(configFile)) {
            log.debug("Loading config from file: {}", configFile);
            try {
                config = objectMapper.readValue(configFile.toFile(), JimiConfig.class);
            } catch (IOException e) {
                throw new ConfigException("Failed to load config from file: " + configFile, e);
            }
        } else {
            log.debug("No config file found, creating default config");
            config = getDefaultConfig();
            try {
                saveConfig(config, configFile);
            } catch (ConfigException e) {
                log.warn("Failed to save default config: {}", e.getMessage());
            }
        }
        
        // 应用环境变量覆盖
        applyEnvironmentOverrides(config);
        
        // 验证配置
        try {
            config.validate();
        } catch (IllegalStateException e) {
            throw new ConfigException("Invalid configuration: " + e.getMessage(), e);
        }
        
        return config;
    }
    
    /**
     * 保存配置
     */
    public void saveConfig(JimiConfig config, Path configFile) {
        try {
            // 确保目录存在
            Files.createDirectories(configFile.getParent());
            
            // 写入配置文件
            objectMapper.writerWithDefaultPrettyPrinter()
                       .writeValue(configFile.toFile(), config);
            
            log.info("Config saved to: {}", configFile);
        } catch (IOException e) {
            throw new ConfigException("Failed to save config to file: " + configFile, e);
        }
    }
    
    /**
     * 获取默认配置
     */
    public JimiConfig getDefaultConfig() {
        return JimiConfig.builder()
                        .defaultModel("")
                        .models(new HashMap<>())
                        .providers(new HashMap<>())
                        .loopControl(LoopControlConfig.builder().build())
                        .services(ServicesConfig.builder().build())
                        .build();
    }
    
    /**
     * 应用环境变量覆盖
     */
    private void applyEnvironmentOverrides(JimiConfig config) {
        Map<String, String> envOverrides = new HashMap<>();
        
        // 检查 KIMI_BASE_URL
        String baseUrl = System.getenv("KIMI_BASE_URL");
        if (baseUrl != null && !baseUrl.isEmpty()) {
            log.info("Using KIMI_BASE_URL from environment: {}", baseUrl);
            envOverrides.put("KIMI_BASE_URL", baseUrl);
            
            // 如果有默认模型，更新其提供商的 baseUrl
            if (!config.getDefaultModel().isEmpty()) {
                LLMModelConfig modelConfig = config.getModels().get(config.getDefaultModel());
                if (modelConfig != null) {
                    LLMProviderConfig providerConfig = config.getProviders().get(modelConfig.getProvider());
                    if (providerConfig != null) {
                        providerConfig.setBaseUrl(baseUrl);
                    }
                }
            }
        }
        
        // 检查 KIMI_API_KEY
        String apiKey = System.getenv("KIMI_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            log.info("Using KIMI_API_KEY from environment");
            envOverrides.put("KIMI_API_KEY", apiKey);
            
            // 如果有默认模型，更新其提供商的 apiKey
            if (!config.getDefaultModel().isEmpty()) {
                LLMModelConfig modelConfig = config.getModels().get(config.getDefaultModel());
                if (modelConfig != null) {
                    LLMProviderConfig providerConfig = config.getProviders().get(modelConfig.getProvider());
                    if (providerConfig != null) {
                        providerConfig.setApiKey(apiKey);
                    }
                }
            }
        }
        
        // 检查 KIMI_MODEL_NAME
        String modelName = System.getenv("KIMI_MODEL_NAME");
        if (modelName != null && !modelName.isEmpty()) {
            log.info("Using KIMI_MODEL_NAME from environment: {}", modelName);
            envOverrides.put("KIMI_MODEL_NAME", modelName);
            
            // 更新默认模型（如果存在）
            if (config.getModels().containsKey(modelName)) {
                config.setDefaultModel(modelName);
            }
        }
    }
}
