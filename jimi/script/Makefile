# Jimi Makefile
# 简化常用构建和运行命令

.PHONY: help build clean test run install deploy package all

# Java 环境配置
JAVA_HOME ?= /Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
export JAVA_HOME

# Maven 配置
MAVEN_OPTS ?= -Xmx1g
export MAVEN_OPTS

# 项目配置
JAR_FILE = target/jimi-0.1.0.jar
INSTALL_DIR ?= $(HOME)/.local/bin

# 默认目标
.DEFAULT_GOAL := help

help: ## 显示帮助信息
	@echo "Jimi 构建和部署工具"
	@echo ""
	@echo "使用方法: make [目标]"
	@echo ""
	@echo "可用目标:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-15s %s\n", $$1, $$2}'

all: clean build test ## 清理、构建和测试

build: ## 构建项目（跳过测试）
	@echo "构建 Jimi..."
	mvn clean package -DskipTests
	@echo "✓ 构建完成: $(JAR_FILE)"

package: build ## 同 build，构建可执行 JAR

compile: ## 仅编译源代码
	@echo "编译源代码..."
	mvn compile

test: ## 运行所有测试
	@echo "运行测试..."
	mvn test

clean: ## 清理构建输出
	@echo "清理构建文件..."
	mvn clean

run: ## 运行应用（显示帮助信息）
	@if [ ! -f "$(JAR_FILE)" ]; then \
		echo "JAR 文件不存在，正在构建..."; \
		make build; \
	fi
	java -jar $(JAR_FILE) --help

run-shell: ## 启动交互式 Shell
	@if [ ! -f "$(JAR_FILE)" ]; then \
		echo "JAR 文件不存在，正在构建..."; \
		make build; \
	fi
	./jimi -w .

install: build ## 安装到本地 bin 目录
	@echo "安装 Jimi 到 $(INSTALL_DIR)..."
	./deploy.sh --install-dir $(INSTALL_DIR)

deploy: install ## 同 install，部署到本地

uninstall: ## 卸载已安装的 Jimi
	@echo "卸载 Jimi..."
	rm -f $(INSTALL_DIR)/jimi
	rm -f $(INSTALL_DIR)/jimi.jar
	@echo "✓ 卸载完成"

check-java: ## 检查 Java 环境
	@echo "检查 Java 环境..."
	@if [ -z "$(JAVA_HOME)" ]; then \
		echo "✗ JAVA_HOME 未设置"; \
		exit 1; \
	fi
	@echo "JAVA_HOME: $(JAVA_HOME)"
	@$(JAVA_HOME)/bin/java -version

check-jar: ## 检查 JAR 文件
	@if [ -f "$(JAR_FILE)" ]; then \
		echo "✓ JAR 文件存在: $(JAR_FILE)"; \
		ls -lh $(JAR_FILE); \
	else \
		echo "✗ JAR 文件不存在，运行 'make build' 构建"; \
		exit 1; \
	fi

dev: ## 开发模式（使用 Maven 运行）
	mvn spring-boot:run

dev-debug: ## 开发模式（启用调试）
	mvn spring-boot:run -Dspring-boot.run.arguments="--debug"

format: ## 格式化代码（需要安装 google-java-format）
	@echo "格式化 Java 代码..."
	@find src -name "*.java" -type f -exec java -jar $(HOME)/.m2/repository/com/google/googlejavaformat/google-java-format/1.17.0/google-java-format-1.17.0-all-deps.jar --replace {} +

info: ## 显示项目信息
	@echo "Jimi 项目信息"
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@echo "项目名称:   Jimi"
	@echo "版本:       0.1.0"
	@echo "Java 版本:  17"
	@echo "构建工具:   Maven"
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@echo "JAR 文件:   $(JAR_FILE)"
	@echo "安装目录:   $(INSTALL_DIR)"
	@echo "JAVA_HOME:  $(JAVA_HOME)"
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@if [ -f "$(JAR_FILE)" ]; then \
		echo "JAR 状态:   ✓ 已构建"; \
		echo "JAR 大小:   $$(du -h $(JAR_FILE) | cut -f1)"; \
	else \
		echo "JAR 状态:   ✗ 未构建"; \
	fi
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

verify: ## 验证构建
	mvn verify

dependency-tree: ## 显示依赖树
	mvn dependency:tree

lint: ## 运行代码检查
	mvn compile -Xlint:all

# 快捷方式
b: build ## build 的快捷方式
t: test ## test 的快捷方式
r: run ## run 的快捷方式
c: clean ## clean 的快捷方式
i: install ## install 的快捷方式
