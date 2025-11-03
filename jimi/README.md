# Jimi - Java Implementation of Moonshot Intelligence

Jimi is a Java implementation of Kimi CLI, providing a powerful CLI agent for software development tasks and terminal operations.

## Overview

This project is a complete Java rewrite of the Python-based Kimi CLI, using Java 17, Maven, and the Spring ecosystem while maintaining functional parity with the original implementation.

## Technology Stack

- **Java 17**: Modern Java language features
- **Maven**: Build and dependency management
- **Spring Boot 3.x**: Application framework and dependency injection
- **Project Reactor**: Reactive programming for async operations
- **Picocli**: Command-line argument parsing
- **JLine 3**: Interactive terminal capabilities
- **Jackson**: JSON/YAML processing
- **SLF4J + Logback**: Logging

## Project Structure

```
jimi/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/io/leavesfly/jimi/
│   │   │   ├── JimiApplication.java
│   │   │   ├── cli/
│   │   │   ├── config/
│   │   │   ├── session/
│   │   │   ├── soul/
│   │   │   ├── llm/
│   │   │   ├── tools/
│   │   │   ├── ui/
│   │   │   ├── wire/
│   │   │   ├── exception/
│   │   │   └── util/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── agents/
│   │       └── logback-spring.xml
│   └── test/
│       └── java/io/leavesfly/jimi/
└── README.md
```

## Key Features

- **Shell Mode**: Interactive terminal with shell command execution
- **Print Mode**: Non-interactive batch processing
- **ACP Support**: Agent Client Protocol for IDE integration
- **MCP Support**: Model Context Protocol for tool integration
- **Context Management**: Automatic context compression and checkpointing
- **Approval Mechanism**: User confirmation for dangerous operations
- **D-Mail**: Time-travel capability for error correction

## Requirements

- Java 17 or higher
- Maven 3.9 or higher

## Building

### 构建可执行 JAR（推荐）

```bash
# 设置 Java 17 环境
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home

# 构建项目
cd jimi
mvn clean package
```

构建成功后会生成：
- `target/jimi-0.1.0.jar` - 完整的可执行 JAR（约 27MB，包含所有依赖）

### 开发模式编译

```bash
mvn clean compile
```

## Running

### 方式 1：使用启动脚本（推荐）

**Linux/macOS:**
```bash
# 查看帮助
./jimi --help

# 启动交互式 Shell
./jimi -w /path/to/project

# 执行单次命令
./jimi -w /path/to/project -c "分析项目结构"
```

**Windows:**
```cmd
jimi.bat --help
jimi.bat -w C:\path\to\project
```

### 方式 2：直接运行 JAR

```bash
java -jar target/jimi-0.1.0.jar --help
java -jar target/jimi-0.1.0.jar -w /path/to/project
```

### 方式 3：使用 Maven（开发模式）

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--help"
mvn spring-boot:run -Dspring-boot.run.arguments="-w /path/to/project"
```

### 命令行参数

```
Options:
  --verbose              打印详细信息
  --debug                输出调试日志
  -w, --work-dir PATH    Agent 工作目录（默认：当前目录）
  -C, --continue         继续上一个会话
  -m, --model MODEL      使用的 LLM 模型
  -y, --yolo, --yes      自动批准所有操作
  --agent-file PATH      自定义 Agent 配置文件
  --mcp-config-file PATH MCP 配置文件（可多次指定）
  -c, --command TEXT     直接执行命令
  -h, --help             显示帮助信息
  -V, --version          显示版本信息
```

## Configuration

### 首次运行配置

1. **创建配置目录**

```bash
mkdir -p ~/.config/jimi
```

2. **复制配置模板**

```bash
cp src/main/resources/config-template.yaml ~/.config/jimi/config.yaml
cp src/main/resources/providers-config-examples.yaml ~/.config/jimi/providers.yaml
```

3. **配置 LLM API Key**

编辑 `~/.config/jimi/providers.yaml`，填入你的 API Key：

```yaml
llm:
  providers:
    moonshot:
      api_key: "your-moonshot-api-key"
      base_url: "https://api.moonshot.cn/v1"
```

或使用环境变量：

```bash
export MOONSHOT_API_KEY="your-api-key"
```

### 支持的 LLM 提供商

- Moonshot AI (Kimi)
- OpenAI
- DeepSeek
- Alibaba Qwen
- Ollama (本地)

详见 `src/main/resources/providers-config-examples.yaml`

### 配置文件位置

- 主配置：`~/.config/jimi/config.yaml`
- LLM 配置：`~/.config/jimi/providers.yaml`
- 会话历史：`~/.kimi-cli/sessions/`
- 日志文件：`~/.kimi-cli/logs/jimi.log`

## Development

### Compile

```bash
mvn compile
```

### Run Tests

```bash
mvn test
```

### Package

```bash
mvn package
```

## License

Same as the original Kimi CLI project.

## Acknowledgments

This is a Java reimplementation of [Kimi CLI](https://github.com/MoonshotAI/kimi-cli) by Moonshot AI.
