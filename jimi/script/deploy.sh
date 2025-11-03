#!/bin/bash
#
# Jimi 快速部署脚本
# 用于将构建好的 JAR 部署到指定位置
#

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# JAR 文件路径
JAR_FILE="$SCRIPT_DIR/target/jimi-0.1.0.jar"

# 检查 JAR 是否存在
if [ ! -f "$JAR_FILE" ]; then
    print_error "找不到 JAR 文件: $JAR_FILE"
    print_info "正在构建项目..."
    
    # 设置 JAVA_HOME
    if [ -z "$JAVA_HOME" ]; then
        if [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home" ]; then
            export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
        fi
    fi
    
    # 构建项目
    mvn clean package -DskipTests
    
    if [ ! -f "$JAR_FILE" ]; then
        print_error "构建失败"
        exit 1
    fi
fi

# 默认安装位置
INSTALL_DIR="${INSTALL_DIR:-$HOME/.local/bin}"

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        --install-dir)
            INSTALL_DIR="$2"
            shift 2
            ;;
        --help)
            echo "用法: $0 [选项]"
            echo ""
            echo "选项:"
            echo "  --install-dir DIR   指定安装目录 (默认: ~/.local/bin)"
            echo "  --help              显示帮助信息"
            exit 0
            ;;
        *)
            print_error "未知选项: $1"
            exit 1
            ;;
    esac
done

print_info "开始部署 Jimi..."
print_info "JAR 文件: $JAR_FILE"
print_info "安装目录: $INSTALL_DIR"

# 创建安装目录
mkdir -p "$INSTALL_DIR"

# 复制 JAR 文件
print_info "复制 JAR 文件..."
cp "$JAR_FILE" "$INSTALL_DIR/jimi.jar"

# 创建启动脚本
print_info "创建启动脚本..."
cat > "$INSTALL_DIR/jimi" << 'EOF'
#!/bin/bash
# Jimi 启动脚本 (自动生成)

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# JAR 文件路径
JAR_FILE="$SCRIPT_DIR/jimi.jar"

# 检查 JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
    # 尝试自动检测 Java 17
    if [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
    elif [ -d "/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home"
    fi
fi

# 设置 Java 可执行文件路径
if [ -n "$JAVA_HOME" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD="java"
fi

# JVM 参数
JVM_OPTS="${JVM_OPTS:--Xms256m -Xmx2g}"

# 运行 JAR
exec "$JAVA_CMD" $JVM_OPTS -jar "$JAR_FILE" "$@"
EOF

# 设置可执行权限
chmod +x "$INSTALL_DIR/jimi"

# 显示文件大小
JAR_SIZE=$(du -h "$INSTALL_DIR/jimi.jar" | cut -f1)
print_info "JAR 文件大小: $JAR_SIZE"

# 检查是否在 PATH 中
if [[ ":$PATH:" != *":$INSTALL_DIR:"* ]]; then
    print_warn "安装目录不在 PATH 中"
    print_warn "请将以下内容添加到 ~/.bashrc 或 ~/.zshrc:"
    echo ""
    echo "    export PATH=\"$INSTALL_DIR:\$PATH\""
    echo ""
fi

print_info "部署完成！"
echo ""
echo "运行方式："
echo "  1. 如果 $INSTALL_DIR 在 PATH 中："
echo "     jimi --help"
echo ""
echo "  2. 使用完整路径："
echo "     $INSTALL_DIR/jimi --help"
echo ""
echo "配置说明："
echo "  - 配置目录: ~/.config/jimi/"
echo "  - 配置模板: $SCRIPT_DIR/src/main/resources/config-template.yaml"
echo ""
