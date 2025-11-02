# Subagents 使用指南

## 什么是 Subagents？

Subagents（子代理）是 Jimi 的核心特性之一，允许将复杂任务委托给专门的子 Agent 处理。

### 核心优势

1. **上下文隔离**：子 Agent 拥有独立的上下文，不会污染主 Agent
2. **专业分工**：不同子 Agent 专注于不同领域（构建、测试、调试等）
3. **并行执行**：可以同时启动多个子 Agent 处理独立任务

## 可用的 Subagents

### 1. Build Agent (构建代理)
- **职责**：构建和编译项目
- **使用场景**：修复编译错误、优化构建配置
- **工具**：Bash, ReadFile, WriteFile, Grep, Think

### 2. Test Agent (测试代理)
- **职责**：运行测试、分析测试结果
- **使用场景**：修复测试失败、提高测试覆盖率
- **工具**：Bash, ReadFile, WriteFile, Grep, Think

### 3. Debug Agent (调试代理)
- **职责**：调试和修复代码错误
- **使用场景**：修复运行时错误、逻辑错误
- **工具**：Bash, ReadFile, WriteFile, StrReplaceFile, Grep, Think

### 4. Research Agent (研究代理)
- **职责**：搜索和研究技术信息
- **使用场景**：学习新技术、查找最佳实践
- **工具**：SearchWeb, FetchURL, ReadFile, Grep, Think

## 使用方法

### 通过对话触发（推荐）

主 Agent 会自动判断何时使用子 Agent：

```
用户: 帮我构建这个项目并修复所有编译错误

主 Agent: 我将使用 Build Agent 来处理这个任务
[调用 Task 工具，subagent_name=build]

Build Agent: 
1. 检测到 Maven 项目
2. 执行 mvn clean compile
3. 发现 3 个编译错误
4. 已修复所有错误
5. 重新编译成功
```

### 手动调用（高级）

在代码中直接调用 Task 工具：

```json
{
  "tool": "Task",
  "parameters": {
    "description": "build project",
    "subagent_name": "build",
    "prompt": "请构建 Maven 项目并修复所有编译错误"
  }
}
```

## 配置 Subagents

### 1. 在主 Agent 中声明

编辑 `agents/default/agent.yaml`：

```yaml
subagents:
  build:
    path: agents/build/agent.yaml
    description: Build and compile the project
  custom_agent:
    path: agents/custom/agent.yaml
    description: Custom specialized agent
```

### 2. 创建子 Agent 配置

创建 `agents/custom/agent.yaml`：

```yaml
name: Custom Agent
description: My custom specialized agent

system_prompt: system_prompt.md
system_prompt_args: {}

tools:
  - Bash
  - ReadFile
  - WriteFile
```

创建 `agents/custom/system_prompt.md`：

```markdown
# Custom Agent System Prompt

You are a specialized agent for [specific task].

## Your Mission
...
```

## 最佳实践

### 1. 选择合适的 Subagent

- **构建问题** → Build Agent
- **测试失败** → Test Agent
- **运行时错误** → Debug Agent
- **技术调研** → Research Agent

### 2. 提供清晰的 Prompt

好的 prompt 示例：
```
请使用 Maven 构建项目，如果遇到编译错误：
1. 分析错误原因
2. 修复相关代码
3. 重新编译验证
4. 提供完整的错误和修复摘要
```

### 3. 并行使用多个 Subagents

```
用户: 同时构建项目和运行测试

主 Agent 可以并行调用：
- Build Agent 负责构建
- Test Agent 负责测试
```

## 进阶功能

### 上下文隔离原理

每个子 Agent 都有独立的：
- 历史文件（`.jimi_history_sub_1.jsonl`）
- 上下文消息列表
- Token 计数

主 Agent 只接收子 Agent 的最终摘要，不会看到详细的工具调用过程。

### 响应质量检查

Task 工具会自动检查子 Agent 的响应质量：
- 如果响应过短（< 200 字符），会要求补充详细信息
- 确保主 Agent 获得足够的上下文信息

### 自定义工具集

每个子 Agent 可以有不同的工具权限：

```yaml
# Build Agent - 不需要 Web 搜索
tools:
  - Bash
  - ReadFile
  - WriteFile

# Research Agent - 需要 Web 工具
tools:
  - SearchWeb
  - FetchURL
  - ReadFile
```

## 故障排查

### Subagent 未找到

错误：`Subagent not found: xxx`

解决：
1. 检查主 Agent 的 `subagents` 配置
2. 确认子 Agent 的 `path` 正确
3. 确认子 Agent 的 YAML 文件存在

### Subagent 执行失败

错误：`Failed to run subagent: xxx`

解决：
1. 检查子 Agent 的系统提示词文件
2. 确认子 Agent 的工具配置正确
3. 查看子 Agent 的历史文件了解详情

## 示例场景

### 场景 1：修复编译错误

```
用户：这个 Java 项目编译失败，帮我修复

主 Agent：我会使用 Build Agent 来处理
→ 调用 Task(build, "修复 Maven 编译错误")
→ Build Agent 工作...
→ 返回：已修复 3 个编译错误，项目构建成功

主 Agent：编译错误已修复，详情如下...
```

### 场景 2：研究新技术

```
用户：我想了解 Project Reactor 的最佳实践

主 Agent：我会使用 Research Agent 来调研
→ 调用 Task(research, "研究 Project Reactor 最佳实践")
→ Research Agent 搜索官方文档、博客、示例...
→ 返回：完整的最佳实践总结和代码示例

主 Agent：根据研究结果，推荐以下实践...
```

### 场景 3：复杂任务分解

```
用户：完整实现一个新功能

主 Agent：我将分步骤处理：
1. 使用 Research Agent 研究最佳实现方案
2. 实现核心代码
3. 使用 Test Agent 创建测试
4. 使用 Build Agent 验证编译
5. 使用 Debug Agent 修复问题
```

## 总结

Subagents 是 Jimi 的强大特性，合理使用可以：
- 保持主上下文清晰
- 提高任务执行效率
- 实现专业化分工
- 支持并行处理

开始使用子代理，让 Jimi 更智能地帮助你！
