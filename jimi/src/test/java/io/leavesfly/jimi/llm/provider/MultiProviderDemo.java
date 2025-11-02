package io.leavesfly.jimi.llm.provider;

import org.junit.jupiter.api.Test;

/**
 * 多 LLM Provider 支持演示
 * 
 * 展示 Jimi 支持的所有 LLM Provider：
 * 1. Kimi (Moonshot AI)
 * 2. DeepSeek
 * 3. Qwen (阿里通义千问)
 * 4. Ollama (本地模型)
 * 5. OpenAI
 * 
 * @author 山泽
 */
class MultiProviderDemo {
    
    /**
     * 演示 1: Provider 支持概览
     */
    @Test
    void demo1_ProviderOverview() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 1: Jimi 支持的 LLM Provider");
        System.out.println("=".repeat(70) + "\n");
        
        System.out.println("核心 Provider:\n");
        
        System.out.println("1. 🌙 Kimi (Moonshot AI)");
        System.out.println("   - API: https://api.moonshot.cn");
        System.out.println("   - 模型: moonshot-v1-8k, moonshot-v1-32k, moonshot-v1-128k");
        System.out.println("   - 特点: 长上下文、工具调用、中文优化");
        
        System.out.println("\n2. 🧠 DeepSeek");
        System.out.println("   - API: https://api.deepseek.com");
        System.out.println("   - 模型: deepseek-chat, deepseek-coder");
        System.out.println("   - 特点: 代码能力强、成本低、开源");
        
        System.out.println("\n3. 🎯 Qwen (通义千问)");
        System.out.println("   - API: https://dashscope.aliyuncs.com");
        System.out.println("   - 模型: qwen-turbo, qwen-plus, qwen-max");
        System.out.println("   - 特点: 阿里云、中文理解、多模态");
        
        System.out.println("\n4. 🦙 Ollama (本地模型)");
        System.out.println("   - API: http://localhost:11434");
        System.out.println("   - 模型: llama3, qwen2, deepseek-coder 等");
        System.out.println("   - 特点: 本地运行、隐私保护、离线可用");
        
        System.out.println("\n5. 🤖 OpenAI");
        System.out.println("   - API: https://api.openai.com");
        System.out.println("   - 模型: gpt-4, gpt-3.5-turbo");
        System.out.println("   - 特点: 工具调用、稳定、通用能力强");
        
        System.out.println("\n✅ 演示完成\n");
    }
    
    /**
     * 演示 2: 配置示例
     */
    @Test
    void demo2_ConfigurationExamples() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 2: Provider 配置示例");
        System.out.println("=".repeat(70) + "\n");
        
        System.out.println("Kimi 配置 (config.yaml):\n");
        System.out.println("```yaml");
        System.out.println("llm:");
        System.out.println("  provider:");
        System.out.println("    type: kimi");
        System.out.println("    base_url: https://api.moonshot.cn");
        System.out.println("    api_key: ${KIMI_API_KEY}");
        System.out.println("  model:");
        System.out.println("    model: moonshot-v1-32k");
        System.out.println("    max_context_size: 32000");
        System.out.println("```\n");
        
        System.out.println("DeepSeek 配置:\n");
        System.out.println("```yaml");
        System.out.println("llm:");
        System.out.println("  provider:");
        System.out.println("    type: deepseek");
        System.out.println("    base_url: https://api.deepseek.com");
        System.out.println("    api_key: ${DEEPSEEK_API_KEY}");
        System.out.println("  model:");
        System.out.println("    model: deepseek-chat");
        System.out.println("    max_context_size: 32000");
        System.out.println("```\n");
        
        System.out.println("Qwen 配置:\n");
        System.out.println("```yaml");
        System.out.println("llm:");
        System.out.println("  provider:");
        System.out.println("    type: qwen");
        System.out.println("    base_url: https://dashscope.aliyuncs.com/compatible-mode");
        System.out.println("    api_key: ${QWEN_API_KEY}");
        System.out.println("  model:");
        System.out.println("    model: qwen-plus");
        System.out.println("    max_context_size: 32000");
        System.out.println("```\n");
        
        System.out.println("Ollama 配置 (本地):\n");
        System.out.println("```yaml");
        System.out.println("llm:");
        System.out.println("  provider:");
        System.out.println("    type: ollama");
        System.out.println("    base_url: http://localhost:11434");
        System.out.println("    # Ollama 不需要 API Key");
        System.out.println("  model:");
        System.out.println("    model: llama3");
        System.out.println("    max_context_size: 8000");
        System.out.println("```\n");
        
        System.out.println("OpenAI 配置:\n");
        System.out.println("```yaml");
        System.out.println("llm:");
        System.out.println("  provider:");
        System.out.println("    type: openai");
        System.out.println("    base_url: https://api.openai.com");
        System.out.println("    api_key: ${OPENAI_API_KEY}");
        System.out.println("  model:");
        System.out.println("    model: gpt-4");
        System.out.println("    max_context_size: 8000");
        System.out.println("```\n");
        
        System.out.println("✅ 演示完成\n");
    }
    
    /**
     * 演示 3: 环境变量配置
     */
    @Test
    void demo3_EnvironmentVariables() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 3: 环境变量配置");
        System.out.println("=".repeat(70) + "\n");
        
        System.out.println("支持通过环境变量覆盖配置:\n");
        
        System.out.println("```bash");
        System.out.println("# Kimi");
        System.out.println("export KIMI_API_KEY=sk-xxx");
        System.out.println("export KIMI_BASE_URL=https://api.moonshot.cn");
        System.out.println("export KIMI_MODEL_NAME=moonshot-v1-32k");
        System.out.println("");
        System.out.println("# DeepSeek");
        System.out.println("export KIMI_API_KEY=sk-xxx  # 复用环境变量");
        System.out.println("");
        System.out.println("# Qwen");
        System.out.println("export KIMI_API_KEY=sk-xxx");
        System.out.println("");
        System.out.println("# Ollama (本地，无需 API Key)");
        System.out.println("export KIMI_BASE_URL=http://localhost:11434");
        System.out.println("export KIMI_MODEL_NAME=llama3");
        System.out.println("```\n");
        
        System.out.println("✅ 演示完成\n");
    }
    
    /**
     * 演示 4: 功能对比
     */
    @Test
    void demo4_FeatureComparison() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 4: Provider 功能对比");
        System.out.println("=".repeat(70) + "\n");
        
        System.out.println("功能对比表:\n");
        System.out.printf("%-15s %-10s %-10s %-10s %-15s%n", 
            "Provider", "工具调用", "流式输出", "中文优化", "部署方式");
        System.out.println("-".repeat(70));
        System.out.printf("%-15s %-10s %-10s %-10s %-15s%n", 
            "Kimi", "✅", "✅", "✅", "云端 API");
        System.out.printf("%-15s %-10s %-10s %-10s %-15s%n", 
            "DeepSeek", "✅", "✅", "✅", "云端 API");
        System.out.printf("%-15s %-10s %-10s %-10s %-15s%n", 
            "Qwen", "✅", "✅", "✅", "云端 API");
        System.out.printf("%-15s %-10s %-10s %-10s %-15s%n", 
            "Ollama", "❌*", "✅", "✅", "本地部署");
        System.out.printf("%-15s %-10s %-10s %-10s %-15s%n", 
            "OpenAI", "✅", "✅", "⚠️", "云端 API");
        
        System.out.println("\n注:");
        System.out.println("  * Ollama 部分模型支持工具调用");
        System.out.println("  ⚠️ OpenAI 中文理解较弱");
        
        System.out.println("\n成本对比 (每百万 Token):\n");
        System.out.println("  Kimi:      ¥12 - ¥60");
        System.out.println("  DeepSeek:  ¥1 - ¥2  (最低)");
        System.out.println("  Qwen:      ¥4 - ¥20");
        System.out.println("  Ollama:    免费 (本地)");
        System.out.println("  OpenAI:    $0.5 - $30");
        
        System.out.println("\n✅ 演示完成\n");
    }
    
    /**
     * 演示 5: 使用场景推荐
     */
    @Test
    void demo5_UsageRecommendations() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 5: 使用场景推荐");
        System.out.println("=".repeat(70) + "\n");
        
        System.out.println("推荐场景:\n");
        
        System.out.println("🌙 Kimi - 适合:");
        System.out.println("  ✓ 需要超长上下文 (128k)");
        System.out.println("  ✓ 中文为主的应用");
        System.out.println("  ✓ 需要稳定工具调用");
        System.out.println("  ✓ 文档分析、代码理解");
        
        System.out.println("\n🧠 DeepSeek - 适合:");
        System.out.println("  ✓ 代码生成和理解");
        System.out.println("  ✓ 成本敏感场景");
        System.out.println("  ✓ 高频调用");
        System.out.println("  ✓ 开发测试环境");
        
        System.out.println("\n🎯 Qwen - 适合:");
        System.out.println("  ✓ 阿里云生态");
        System.out.println("  ✓ 中文场景");
        System.out.println("  ✓ 多模态需求");
        System.out.println("  ✓ 企业应用");
        
        System.out.println("\n🦙 Ollama - 适合:");
        System.out.println("  ✓ 隐私敏感场景");
        System.out.println("  ✓ 离线使用");
        System.out.println("  ✓ 内网部署");
        System.out.println("  ✓ 开发调试");
        
        System.out.println("\n🤖 OpenAI - 适合:");
        System.out.println("  ✓ 英文为主");
        System.out.println("  ✓ 需要最强能力");
        System.out.println("  ✓ 复杂推理任务");
        System.out.println("  ✓ 生产环境");
        
        System.out.println("\n✅ 演示完成\n");
    }
    
    /**
     * 演示 6: 技术实现
     */
    @Test
    void demo6_TechnicalImplementation() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("演示 6: 技术实现");
        System.out.println("=".repeat(70) + "\n");
        
        System.out.println("实现架构:\n");
        
        System.out.println("1. ChatProvider 接口");
        System.out.println("   - 定义统一的 LLM 交互接口");
        System.out.println("   - generate(): 非流式生成");
        System.out.println("   - generateStream(): 流式生成");
        
        System.out.println("\n2. KimiChatProvider");
        System.out.println("   - Kimi 专用实现");
        System.out.println("   - 完整工具调用支持");
        
        System.out.println("\n3. OpenAICompatibleChatProvider");
        System.out.println("   - 通用 OpenAI API 实现");
        System.out.println("   - 支持: DeepSeek, Qwen, Ollama, OpenAI");
        System.out.println("   - 自动适配工具调用能力");
        
        System.out.println("\n4. LLMProviderConfig");
        System.out.println("   - 提供商配置");
        System.out.println("   - 支持类型: KIMI, DEEPSEEK, QWEN, OLLAMA, OPENAI");
        
        System.out.println("\n5. JimiFactory");
        System.out.println("   - 根据配置创建对应 Provider");
        System.out.println("   - 支持环境变量覆盖");
        
        System.out.println("\n技术特点:");
        System.out.println("  ✓ 统一接口，易于扩展");
        System.out.println("  ✓ 响应式编程 (Reactor)");
        System.out.println("  ✓ 流式处理");
        System.out.println("  ✓ 工具调用自适应");
        System.out.println("  ✓ WebClient 异步 HTTP");
        
        System.out.println("\n✅ 演示完成\n");
    }
}
