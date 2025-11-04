package io.leavesfly.jimi.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.leavesfly.jimi.config.JimiConfig;
import io.leavesfly.jimi.config.LLMModelConfig;
import io.leavesfly.jimi.config.LLMProviderConfig;
import io.leavesfly.jimi.llm.provider.KimiChatProvider;
import io.leavesfly.jimi.llm.provider.OpenAICompatibleChatProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * LLM 工厂类
 * 根据配置创建 LLM 实例
 */
@Slf4j
@RequiredArgsConstructor
public class LLMFactory {
    
    private final ObjectMapper objectMapper;
    
    /**
     * 根据配置创建 LLM
     * 
     * @param config 全局配置
     * @param modelName 模型名称（可选，默认使用配置中的默认模型）
     * @return LLM 实例
     */
    public LLM createLLM(JimiConfig config, String modelName) {
        String actualModelName = modelName != null ? modelName : config.getDefaultModel();
        
        if (actualModelName == null || actualModelName.isEmpty()) {
            throw new IllegalArgumentException("Model name is required");
        }
        
        // 获取模型配置
        LLMModelConfig modelConfig = config.getModels().get(actualModelName);
        if (modelConfig == null) {
            throw new IllegalArgumentException("Unknown model: " + actualModelName);
        }
        
        // 获取提供商配置
        LLMProviderConfig providerConfig = config.getProviders().get(modelConfig.getProvider());
        if (providerConfig == null) {
            throw new IllegalArgumentException("Unknown provider: " + modelConfig.getProvider());
        }
        
        // 创建 ChatProvider
        ChatProvider chatProvider = createChatProvider(
            modelConfig.getModel(),
            providerConfig
        );
        
        // 创建 LLM
        return LLM.builder()
            .chatProvider(chatProvider)
            .maxContextSize(modelConfig.getMaxContextSize())
            .build();
    }
    
    /**
     * 创建默认 LLM（使用配置中的默认模型）
     */
    public LLM createDefaultLLM(JimiConfig config) {
        return createLLM(config, null);
    }
    
    /**
     * 根据提供商配置创建 ChatProvider
     */
    private ChatProvider createChatProvider(
        String modelName,
        LLMProviderConfig providerConfig
    ) {
        switch (providerConfig.getType()) {
            case KIMI:
                return new KimiChatProvider(modelName, providerConfig, objectMapper);
            case OPENAI_LEGACY:
                return new OpenAICompatibleChatProvider(modelName, providerConfig, objectMapper, "OpenAI Legacy");
            case CHAOS:
                return new OpenAICompatibleChatProvider(modelName, providerConfig, objectMapper, "Chaos");
            default:
                throw new IllegalArgumentException("Unknown provider type: " + providerConfig.getType());
        }
    }
}
