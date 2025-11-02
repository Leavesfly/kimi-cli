#!/bin/bash
# Jimi 环境变量模板
# 将此文件复制为 .env 并根据需要修改

# ============================================================================
# LLM API 配置
# ============================================================================

# Kimi API
export KIMI_API_KEY="sk-your-kimi-api-key"
export KIMI_BASE_URL="https://api.moonshot.cn"
export KIMI_MODEL_NAME="moonshot-v1-32k"

# DeepSeek API
# export KIMI_API_KEY="sk-your-deepseek-api-key"
# export KIMI_BASE_URL="https://api.deepseek.com"
# export KIMI_MODEL_NAME="deepseek-chat"

# Qwen API
# export KIMI_API_KEY="sk-your-qwen-api-key"
# export KIMI_BASE_URL="https://dashscope.aliyuncs.com/compatible-mode"
# export KIMI_MODEL_NAME="qwen-plus"

# Ollama (本地)
# export KIMI_BASE_URL="http://localhost:11434"
# export KIMI_MODEL_NAME="llama3"

# OpenAI API
# export KIMI_API_KEY="sk-your-openai-api-key"
# export KIMI_BASE_URL="https://api.openai.com"
# export KIMI_MODEL_NAME="gpt-4"

# ============================================================================
# Java 环境配置
# ============================================================================
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"

# ============================================================================
# 其他配置
# ============================================================================
# 工作目录
# export JIMI_WORK_DIR="/path/to/your/project"

# 会话 ID
# export JIMI_SESSION_ID="my-session"

# YOLO 模式（自动批准所有操作，谨慎使用）
# export JIMI_YOLO="true"
