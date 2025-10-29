# Kimi CLI

[![Commit Activity](https://img.shields.io/github/commit-activity/w/MoonshotAI/kimi-cli)](https://github.com/MoonshotAI/kimi-cli/graphs/commit-activity)
[![Checks](https://img.shields.io/github/check-runs/MoonshotAI/kimi-cli/main)](https://github.com/MoonshotAI/kimi-cli/actions)
[![Version](https://img.shields.io/pypi/v/kimi-cli)](https://pypi.org/project/kimi-cli/)
[![Downloads](https://img.shields.io/pypi/dw/kimi-cli)](https://pypistats.org/packages/kimi-cli)

[中文](https://www.kimi.com/coding/docs/kimi-cli.html)

Kimi CLI 是一个全新的 CLI 智能代理，可以帮助您完成软件开发任务和终端操作。

> [!IMPORTANT]
> Kimi CLI 目前处于技术预览阶段。

## 主要功能

- 类 Shell 用户界面和原生 shell 命令执行
- Zsh 集成
- [Agent Client Protocol] 支持
- MCP 支持
- 更多功能即将推出...

[Agent Client Protocol]: https://github.com/agentclientprotocol/agent-client-protocol

## 安装

> [!IMPORTANT]
> Kimi CLI 目前仅支持 macOS 和 Linux。Windows 支持即将推出。

Kimi CLI 以 Python 包的形式发布在 PyPI 上。我们强烈推荐使用 [uv](https://docs.astral.sh/uv/) 进行安装。如果您还没有安装 uv，请先按照[这里](https://docs.astral.sh/uv/getting-started/installation/)的说明进行安装。

安装 uv 后，您可以使用以下命令安装 Kimi CLI：

```sh
uv tool install --python 3.13 kimi-cli
```

运行 `kimi --help` 以检查 Kimi CLI 是否安装成功。

> [!IMPORTANT]
> 由于 macOS 的安全检查机制，首次运行 `kimi` 命令可能需要 10 秒或更长时间，具体取决于您的系统环境。

## 升级

使用以下命令将 Kimi CLI 升级到最新版本：

```sh
uv tool upgrade kimi-cli --no-cache
```

## 使用方法

在您想要工作的目录中运行 `kimi` 命令，然后发送 `/setup` 来设置 Kimi CLI：

![](./docs/images/setup.png)

设置完成后，Kimi CLI 就可以使用了。您可以发送 `/help` 获取更多信息。

## 功能特性

### Shell 模式

Kimi CLI 不仅是一个编码代理，还是一个 shell。您可以通过按 `Ctrl-X` 来切换模式。在 shell 模式下，您可以直接运行 shell 命令，无需离开 Kimi CLI。

> [!NOTE]
> 内置的 shell 命令（如 `cd`）暂不支持。

### Zsh 集成

您可以将 Kimi CLI 与 Zsh 一起使用，为您的 shell 体验赋予 AI 代理能力。

通过以下命令安装 [zsh-kimi-cli](https://github.com/MoonshotAI/zsh-kimi-cli) 插件：

```sh
git clone https://github.com/MoonshotAI/zsh-kimi-cli.git \
  ${ZSH_CUSTOM:-~/.oh-my-zsh/custom}/plugins/kimi-cli
```

> [!NOTE]
> 如果您使用的是 Oh My Zsh 之外的插件管理器，您可能需要参考该插件的 README 以获取安装说明。

然后在 `~/.zshrc` 中将 `kimi-cli` 添加到您的 Zsh 插件列表中：

```sh
plugins=(... kimi-cli)
```

重启 Zsh 后，您可以通过按 `Ctrl-X` 切换到代理模式。

### ACP 支持

Kimi CLI 开箱即支持 [Agent Client Protocol]。您可以将它与任何兼容 ACP 的编辑器或 IDE 一起使用。

例如，要在 [Zed](https://zed.dev/) 中使用 Kimi CLI，请将以下配置添加到您的 `~/.config/zed/settings.json`：

```json
{
  "agent_servers": {
    "Kimi CLI": {
      "command": "kimi",
      "args": ["--acp"],
      "env": {}
    }
  }
}
```

然后您可以在 Zed 的代理面板中创建 Kimi CLI 线程。

### 使用 MCP 工具

Kimi CLI 支持完善的 MCP 配置约定。例如：

```json
{
  "mcpServers": {
    "context7": {
      "url": "https://mcp.context7.com/mcp",
      "headers": {
        "CONTEXT7_API_KEY": "YOUR_API_KEY"
      }
    },
    "chrome-devtools": {
      "command": "npx",
      "args": ["-y", "chrome-devtools-mcp@latest"]
    }
  }
}
```

使用 `--mcp-config-file` 选项运行 `kimi` 以连接到指定的 MCP 服务器：

```sh
kimi --mcp-config-file /path/to/mcp.json
```

## 开发

要开发 Kimi CLI，请运行：

```sh
git clone https://github.com/MoonshotAI/kimi-cli.git
cd kimi-cli

make prepare  # 准备开发环境
```

然后您就可以开始开发 Kimi CLI 了。

在进行更改后，请参考以下命令：

```sh
uv run kimi  # 运行 Kimi CLI

make format  # 格式化代码
make check  # 运行 lint 和类型检查
make test  # 运行测试
make help  # 显示所有 make 目标
```

## 贡献

我们欢迎对 Kimi CLI 的贡献！请参阅 [CONTRIBUTING.md](./CONTRIBUTING.md) 了解更多信息。
