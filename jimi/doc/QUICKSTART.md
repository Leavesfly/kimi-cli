# Jimi 快速入门指南

## 前置要求

- ✅ Java 17 或更高版本
- ✅ Maven 3.9+ （仅构建时需要）

## 快速开始

### 1. 构建项目

```bash
# 方式 1: 使用 Maven
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
cd jimi
mvn clean package

# 方式 2: 使用 Makefile（推荐）
make build

# 方式 3: 构建 + 测试
make all
```

构建成功后会生成：
- `target/jimi-0.1.0.jar` （约 27MB）

### 2. 配置

```bash
# 创建配置目录
mkdir -p ~/.config/jimi

# 复制配置模板
cp src/main/resources/config-template.yaml ~/.config/jimi/config.yaml
cp src/main/resources/providers-config-examples.yaml ~/.config/jimi/providers.yaml

# 设置 API Key（选择一个提供商）
export MOONSHOT_API_KEY="your-api-key"
# 或
export OPENAI_API_KEY="your-api-key"
# 或
export DEEPSEEK_API_KEY="your-api-key"
```

### 3. 运行

#### 方式 A: 使用启动脚本（推荐）

```bash
# 查看版本
./jimi --version

# 显示帮助
./jimi --help

# 启动交互式 Shell
./jimi -w /path/to/project

# 执行单次命令
./jimi -w /path/to/project -c "分析项目结构"

# 继续上一个会话
./jimi -w /path/to/project -C
```

#### 方式 B: 直接运行 JAR

```bash
java -jar target/jimi-0.1.0.jar --help
java -jar target/jimi-0.1.0.jar -w /path/to/project
```

#### 方式 C: 使用 Makefile

```bash
# 运行帮助
make run

# 启动 Shell
make run-shell

# 开发模式
make dev
```

### 4. 安装到系统

```bash
# 方式 1: 使用部署脚本
./deploy.sh

# 方式 2: 使用 Makefile
make install

# 安装后可直接使用
jimi --help
jimi -w /path/to/project
```

## 常用命令

### 构建相关

```bash
make build        # 构建项目
make clean        # 清理构建文件
make test         # 运行测试
make all          # 清理 + 构建 + 测试
make verify       # 验证构建
```

### 运行相关

```bash
make run          # 运行帮助
make run-shell    # 启动 Shell
make dev          # 开发模式
make dev-debug    # 调试模式
```

### 部署相关

```bash
make install      # 安装到本地
make uninstall    # 卸载
make info         # 显示项目信息
make check-java   # 检查 Java 环境
```

### 其他

```bash
make help              # 显示所有可用命令
make dependency-tree   # 显示依赖树
```

## 命令行参数说明

```
Usage: jimi [OPTIONS]

核心选项:
  -w, --work-dir PATH    工作目录（必填，默认当前目录）
  -c, --command TEXT     执行单次命令
  -C, --continue         继续上一个会话

模型配置:
  -m, --model MODEL      指定模型（如 moonshot-v1-32k）
  --agent-file PATH      自定义 Agent 配置

MCP 集成:
  --mcp-config-file PATH MCP 配置文件（可多次指定）

行为控制:
  -y, --yolo, --yes      自动批准所有操作（危险）
  --verbose              详细输出
  --debug                调试日志

帮助:
  -h, --help             显示帮助
  -V, --version          显示版本
```

## 使用示例

### 示例 1: 分析项目结构

```bash
./jimi -w ~/myproject -c "分析这个项目的代码结构"
```

### 示例 2: 交互式开发

```bash
# 启动 Shell
./jimi -w ~/myproject

# 在 Shell 中使用元命令
> /help           # 查看所有命令
> /config         # 查看当前配置
> /tools          # 查看可用工具
> /init           # 初始化项目上下文
```

### 示例 3: 继续之前的会话

```bash
./jimi -w ~/myproject -C
```

### 示例 4: 使用特定模型

```bash
./jimi -w ~/myproject -m moonshot-v1-128k
```

### 示例 5: YOLO 模式（自动批准）

```bash
./jimi -w ~/myproject -y -c "修复所有编译错误"
```

## 配置文件

### 主配置 (`~/.config/jimi/config.yaml`)

```yaml
loop_control:
  max_steps_per_run: 50
  max_retries_per_step: 3
  max_total_llm_requests: 100
```

### LLM 配置 (`~/.config/jimi/providers.yaml`)

```yaml
llm:
  providers:
    moonshot:
      api_key: "${MOONSHOT_API_KEY}"
      base_url: "https://api.moonshot.cn/v1"
      models:
        moonshot-v1-8k:
          context_size: 8192
        moonshot-v1-32k:
          context_size: 32768
        moonshot-v1-128k:
          context_size: 131072
```

### Agent 配置 (`src/main/resources/agents/default/agent.yaml`)

```yaml
version: 1
agent:
  name: "default"
  system_prompt_path: ./system.md
  tools:
    - "io.leavesfly.jimi.tool.file.ReadFile"
    - "io.leavesfly.jimi.tool.file.WriteFile"
    - "io.leavesfly.jimi.tool.bash.Bash"
    # ... 更多工具
  subagents:
    code_fixer:
      path: ./subagents/code_fixer.yaml
      description: "代码修复专家"
```

## 目录结构

```
~/.config/jimi/          配置文件目录
  ├── config.yaml        主配置
  └── providers.yaml     LLM 提供商配置

~/.kimi-cli/            运行时数据
  ├── sessions/         会话历史
  └── logs/            日志文件
    └── jimi.log       应用日志

~/.local/bin/           安装目录（可选）
  ├── jimi              启动脚本
  └── jimi.jar          可执行 JAR
```

## 环境变量

```bash
# LLM API Keys（选择一个或多个）
export MOONSHOT_API_KEY="sk-..."
export OPENAI_API_KEY="sk-..."
export DEEPSEEK_API_KEY="sk-..."
export QWEN_API_KEY="sk-..."

# JVM 内存配置（可选）
export JVM_OPTS="-Xms512m -Xmx4g"

# Java 路径（可选）
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
```

## 故障排查

### 问题 1: 找不到 Java

```bash
# 检查 Java 版本
java -version

# 应该显示 17 或更高

# 设置 JAVA_HOME
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
```

### 问题 2: 构建失败

```bash
# 清理后重新构建
make clean
make build

# 或使用 Maven
mvn clean package -X
```

### 问题 3: 配置文件错误

```bash
# 检查配置文件是否存在
ls -la ~/.config/jimi/

# 重新复制模板
cp src/main/resources/config-template.yaml ~/.config/jimi/config.yaml
```

### 问题 4: API Key 未设置

```bash
# 检查环境变量
env | grep API_KEY

# 或在配置文件中直接设置
vim ~/.config/jimi/providers.yaml
```

### 问题 5: 内存不足

```bash
# 增加 JVM 内存
export JVM_OPTS="-Xms1g -Xmx4g"

# 或修改启动脚本
vim jimi  # 修改 JVM_OPTS 默认值
```

## 开发者模式

### 修改代码后快速测试

```bash
# 编译（不打包）
make compile

# 运行测试
make test

# 完整构建
make build

# 开发模式运行（热重载）
make dev
```

### 查看依赖

```bash
# 依赖树
make dependency-tree

# 或
mvn dependency:tree
```

### 调试

```bash
# 启用调试日志
./jimi --debug -w /path/to/project

# 或使用 Maven 调试模式
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"
```

## 更多信息

- 📖 完整文档: `RUNNING.md`
- 🔧 配置示例: `src/main/resources/`
- 🐛 问题反馈: GitHub Issues
- 💬 讨论: GitHub Discussions

## 下一步

1. ✅ 配置好 LLM API Key
2. ✅ 运行 `./jimi --version` 验证安装
3. ✅ 在测试项目上运行 `./jimi -w /path/to/test`
4. ✅ 学习元命令 `/help`, `/config`, `/tools`
5. ✅ 阅读 Agent 配置文档
6. ✅ 自定义工具和子 Agent

祝你使用愉快！🚀
