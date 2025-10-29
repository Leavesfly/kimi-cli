package io.leavesfly.jimi.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.leavesfly.jimi.tool.todo.SetTodoList;
import io.leavesfly.jimi.tool.web.FetchURL;
import io.leavesfly.jimi.tool.web.WebSearch;

import java.util.HashMap;
import java.util.List;

/**
 * 工具演示程序
 * 展示所有工具的使用方法和功能
 */
public class ToolsDemo {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("Jimi 工具集演示");
        System.out.println("=".repeat(80));
        System.out.println();
        
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 运行演示
        demo1_SetTodoList();
        demo2_FetchURL();
        demo3_WebSearch(objectMapper);
        demo4_AllToolsOverview();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("所有演示完成！");
        System.out.println("=".repeat(80));
    }
    
    /**
     * 演示1：SetTodoList - 待办事项管理
     */
    private static void demo1_SetTodoList() {
        System.out.println("\n=== 演示1：SetTodoList - 待办事项管理 ===\n");
        
        SetTodoList tool = new SetTodoList();
        
        System.out.println("工具名称: " + tool.getName());
        System.out.println("工具描述: ");
        System.out.println(tool.getDescription());
        System.out.println();
        
        // 创建待办事项列表
        SetTodoList.Params params = SetTodoList.Params.builder()
            .todos(List.of(
                SetTodoList.Todo.builder()
                    .title("研究 API 集成")
                    .status("Done")
                    .build(),
                SetTodoList.Todo.builder()
                    .title("实现认证功能")
                    .status("In Progress")
                    .build(),
                SetTodoList.Todo.builder()
                    .title("编写单元测试")
                    .status("Pending")
                    .build(),
                SetTodoList.Todo.builder()
                    .title("部署到生产环境")
                    .status("Pending")
                    .build()
            ))
            .build();
        
        // 执行工具
        ToolResult result = tool.execute(params).block();
        
        System.out.println("执行结果:");
        System.out.println(result.getOutput());
        System.out.println("✅ 演示1完成\n");
    }
    
    /**
     * 演示2：FetchURL - 网页内容抓取
     */
    private static void demo2_FetchURL() {
        System.out.println("\n=== 演示2：FetchURL - 网页内容抓取 ===\n");
        
        FetchURL tool = new FetchURL();
        
        System.out.println("工具名称: " + tool.getName());
        System.out.println("工具功能: 从 URL 抓取网页内容并提取主要文本");
        System.out.println();
        
        System.out.println("参数示例:");
        System.out.println("  url: https://example.com");
        System.out.println();
        
        System.out.println("功能说明:");
        System.out.println("  • 发送 HTTP GET 请求");
        System.out.println("  • 使用 Jsoup 解析 HTML");
        System.out.println("  • 移除 script、style 等标签");
        System.out.println("  • 提取主要文本内容");
        System.out.println("  • 处理网络错误和 HTTP 错误");
        System.out.println();
        
        System.out.println("注意: 需要实际网络连接，此处仅展示用法");
        System.out.println("✅ 演示2完成\n");
    }
    
    /**
     * 演示3：WebSearch - 网页搜索
     */
    private static void demo3_WebSearch(ObjectMapper objectMapper) {
        System.out.println("\n=== 演示3：WebSearch - 网页搜索 ===\n");
        
        // 注意：实际使用需要配置搜索服务
        WebSearch tool = new WebSearch(
            "https://api.search.example.com",
            "your-api-key",
            new HashMap<>(),
            objectMapper
        );
        
        System.out.println("工具名称: " + tool.getName());
        System.out.println("工具功能: 使用搜索服务搜索网页");
        System.out.println();
        
        System.out.println("参数示例:");
        System.out.println("  query: Java reactive programming");
        System.out.println("  limit: 5");
        System.out.println("  includeContent: false");
        System.out.println();
        
        System.out.println("功能说明:");
        System.out.println("  • 支持自定义搜索查询");
        System.out.println("  • 可控制返回结果数量（1-20）");
        System.out.println("  • 可选择是否包含页面内容");
        System.out.println("  • 返回标题、URL、摘要、日期等信息");
        System.out.println();
        
        System.out.println("注意: 需要配置搜索服务（如 Moonshot Search）");
        System.out.println("✅ 演示3完成\n");
    }
    
    /**
     * 演示4：所有工具总览
     */
    private static void demo4_AllToolsOverview() {
        System.out.println("\n=== 演示4：Jimi 工具集总览 ===\n");
        
        System.out.println("📂 文件工具 (io.leavesfly.jimi.tool.file):");
        System.out.println("  • ReadFile       - 读取文件内容");
        System.out.println("  • WriteFile      - 写入文件（覆盖/追加）");
        System.out.println("  • StrReplaceFile - 字符串替换");
        System.out.println("  • Glob           - 文件模式匹配");
        System.out.println("  • Grep           - 正则表达式搜索");
        System.out.println();
        
        System.out.println("💻 Shell 工具 (io.leavesfly.jimi.tool.bash):");
        System.out.println("  • Bash           - 执行 Shell 命令");
        System.out.println();
        
        System.out.println("🧠 思考工具 (io.leavesfly.jimi.tool.think):");
        System.out.println("  • Think          - 记录思考过程");
        System.out.println();
        
        System.out.println("🌐 Web 工具 (io.leavesfly.jimi.tool.web):");
        System.out.println("  • WebSearch      - 网页搜索");
        System.out.println("  • FetchURL       - 抓取网页内容");
        System.out.println();
        
        System.out.println("📋 待办工具 (io.leavesfly.jimi.tool.todo):");
        System.out.println("  • SetTodoList    - 管理待办事项");
        System.out.println();
        
        System.out.println("工具总数: 10 个");
        System.out.println();
        
        System.out.println("所有工具特性:");
        System.out.println("  ✓ 响应式执行（Reactor Mono）");
        System.out.println("  ✓ 审批机制集成（敏感操作）");
        System.out.println("  ✓ 路径安全验证");
        System.out.println("  ✓ 完整的错误处理");
        System.out.println("  ✓ 统一的结果格式");
        System.out.println("  ✓ JSON Schema 导出");
        System.out.println();
        
        System.out.println("✅ 演示4完成\n");
    }
}
