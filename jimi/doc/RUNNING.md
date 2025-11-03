# Jimi 单 JAR 运行指南

## 构建可执行 JAR

```bash
# 设置 Java 17 环境
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home

# 进入项目目录
cd /Users/yefei.yf/Qoder/kimi-cli/jimi

# 构建项目（包含所有依赖）
mvn clean package
```

构建成功后，会在 `target/` 目录下生成：
- `jimi-0.1.0.jar` - 完整的可执行 JAR（包含所有依赖）

## 运行方式

### 方式 1：使用启动脚本（推荐）

**Linux/macOS:**
```bash
./jimi --help
./jimi -w /path/to/project -c "你的命令"
```

**Windows:**
```cmd
jimi.bat --help
jimi.bat -w C:\path\to\project -c "你的命令"
```

### 方式 2：直接运行 JAR

```bash
java -jar target/jimi-0.1.0.jar --help
```

### 方式 3：作为可执行文件（仅 Linux/macOS）

Spring Boot 的可执行 JAR 功能允许直接运行：
```bash
target/jimi-0.1.0.jar --help
```

## 命令行参数

```
Usage: jimi [OPTIONS]

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

## 使用示例

### 启动交互式 Shell
```bash
./jimi -w /path/to/project
```

### 执行单次命令
```bash
./jimi -w /path/to/project -c "分析项目结构"
```

### 继续上一个会话
```bash
./jimi -w /path/to/project -C
```

### 使用特定模型
```bash
./jimi -m moonshot-v1-128k -w /path/to/project
```

### YOLO 模式（自动批准）
```bash
./jimi -y -w /path/to/project -c "重构代码"
```

## 配置文件

首次运行前，请确保配置以下文件：

1. **~/.config/jimi/config.yaml** - 主配置文件
2. **~/.config/jimi/providers.yaml** - LLM 提供商配置

参考模板：
- `src/main/resources/config-template.yaml`
- `src/main/resources/providers-config-examples.yaml`
- `src/main/resources/env-template.sh`

## 环境变量

```bash
# LLM API Key（根据使用的提供商设置）
export MOONSHOT_API_KEY="your-api-key"
export OPENAI_API_KEY="your-api-key"
export DEEPSEEK_API_KEY="your-api-key"

# JVM 内存设置（可选）
export JVM_OPTS="-Xms512m -Xmx4g"
```

## JAR 分发

生成的 `jimi-0.1.0.jar` 是一个自包含的可执行 JAR，可以直接分发给其他用户：

1. 复制 `jimi-0.1.0.jar` 到目标机器
2. 确保目标机器安装了 Java 17+
3. 运行 `java -jar jimi-0.1.0.jar`

## 故障排查

### JAR 文件未找到
```bash
# 重新构建
mvn clean package
```

### Java 版本错误
```bash
# 检查 Java 版本
java -version

# 应该显示 17 或更高版本
```

### 内存不足
```bash
# 增加 JVM 堆内存
export JVM_OPTS="-Xms1g -Xmx4g"
./jimi [OPTIONS]
```

### 配置文件未找到
```bash
# 复制模板文件
mkdir -p ~/.config/jimi
cp src/main/resources/config-template.yaml ~/.config/jimi/config.yaml
cp src/main/resources/providers-config-examples.yaml ~/.config/jimi/providers.yaml

# 编辑配置文件，填入 API Key
```

## 开发模式

开发时可以使用 Maven 直接运行（无需打包）：

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--help"
mvn spring-boot:run -Dspring-boot.run.arguments="-w /path/to/project"
```

## 技术细节

- **打包方式**: Spring Boot Maven Plugin (repackage)
- **JAR 类型**: 可执行 JAR (Executable JAR)
- **依赖包含**: 所有运行时依赖已内嵌
- **启动类**: `io.leavesfly.jimi.JimiApplication`
- **文件大小**: 约 60-80 MB（包含所有依赖）
