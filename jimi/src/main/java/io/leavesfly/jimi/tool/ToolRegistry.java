package io.leavesfly.jimi.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.leavesfly.jimi.soul.approval.Approval;
import io.leavesfly.jimi.soul.runtime.BuiltinSystemPromptArgs;
import io.leavesfly.jimi.tool.bash.Bash;
import io.leavesfly.jimi.tool.file.*;
import io.leavesfly.jimi.tool.think.Think;
import io.leavesfly.jimi.tool.todo.SetTodoList;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 工具注册表
 * 管理所有可用工具的注册、查找和调用
 */
@Slf4j
public class ToolRegistry {

    private final Map<String, Tool<?>> tools;
    private final ObjectMapper objectMapper;

    public ToolRegistry(ObjectMapper objectMapper) {
        this.tools = new HashMap<>();
        this.objectMapper = objectMapper;
    }

    /**
     * 注册工具
     */
    public void register(Tool<?> tool) {
        tools.put(tool.getName(), tool);
        log.debug("Registered tool: {}", tool.getName());
    }

    /**
     * 批量注册工具
     */
    public void registerAll(Collection<Tool<?>> toolList) {
        toolList.forEach(this::register);
    }

    /**
     * 获取工具
     */
    public Optional<Tool<?>> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    /**
     * 获取所有工具名称
     */
    public Set<String> getToolNames() {
        return tools.keySet();
    }

    /**
     * 获取所有工具
     */
    public Collection<Tool<?>> getAllTools() {
        return tools.values();
    }

    /**
     * 检查工具是否存在
     */
    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }

    /**
     * 执行工具调用
     *
     * @param toolName  工具名称
     * @param arguments 参数（JSON格式字符串）
     * @return 工具执行结果
     */
    public Mono<ToolResult> execute(String toolName, String arguments) {
        return Mono.defer(() -> {
            Optional<Tool<?>> toolOpt = getTool(toolName);
            if (toolOpt.isEmpty()) {
                return Mono.just(ToolResult.error(
                        String.format("Tool not found: %s", toolName),
                        "Tool not found"
                ));
            }

            Tool<?> tool = toolOpt.get();

            try {
                // 解析参数
                Object params = objectMapper.readValue(arguments, tool.getParamsType());

                // 执行工具（使用原始类型）
                return executeToolUnchecked(tool, params);

            } catch (Exception e) {
                log.error("Failed to execute tool: {}", toolName, e);
                return Mono.just(ToolResult.error(
                        String.format("Failed to execute tool. Error: %s", e.getMessage()),
                        "Execution failed"
                ));
            }
        });
    }

    /**
     * 执行工具（原始类型调用）
     */
    @SuppressWarnings("unchecked")
    private <P> Mono<ToolResult> executeToolUnchecked(Tool<?> tool, Object params) {
        Tool<P> typedTool = (Tool<P>) tool;
        P typedParams = (P) params;
        return typedTool.execute(typedParams);
    }

    /**
     * 生成工具的 JSON Schema 定义列表
     * 用于传递给 LLM
     */
    public List<JsonNode> getToolSchemas() {
        return getToolSchemas(null);
    }

    /**
     * 生成工具的 JSON Schema 定义列表（带过滤）
     *
     * @param includeTools 要包含的工具名称列表（null表示全部）
     */
    public List<JsonNode> getToolSchemas(List<String> includeTools) {
        List<JsonNode> schemas = new ArrayList<>();

        Collection<Tool<?>> toolsToInclude;
        if (includeTools == null) {
            toolsToInclude = tools.values();
        } else {
            toolsToInclude = new ArrayList<>();
            for (String toolName : includeTools) {
                Tool<?> tool = tools.get(toolName);
                if (tool != null) {
                    toolsToInclude.add(tool);
                } else {
                    log.warn("Tool not found in registry: {}", toolName);
                }
            }
        }

        for (Tool<?> tool : toolsToInclude) {
            schemas.add(generateToolSchema(tool));
        }

        return schemas;
    }

    /**
     * 生成单个工具的 JSON Schema
     */
    private JsonNode generateToolSchema(Tool<?> tool) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "function");

        ObjectNode function = objectMapper.createObjectNode();
        function.put("name", tool.getName());
        function.put("description", tool.getDescription());

        // 生成参数 schema
        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("type", "object");

        // 使用反射从参数类生成详细的 schema
        ObjectNode properties = objectMapper.createObjectNode();
        ArrayNode required = objectMapper.createArrayNode();

        Class<?> paramsType = tool.getParamsType();
        if (paramsType != null) {
            for (Field field : paramsType.getDeclaredFields()) {
                String propName = field.getName();
                JsonProperty jp = field.getAnnotation(JsonProperty.class);
                if (jp != null && !jp.value().isEmpty()) {
                    propName = jp.value();
                }

                ObjectNode propSchema = objectMapper.createObjectNode();
                Class<?> t = field.getType();
                if (t == String.class) {
                    propSchema.put("type", "string");
                } else if (t == Integer.class || t == int.class) {
                    propSchema.put("type", "integer");
                } else if (t == Long.class || t == long.class) {
                    propSchema.put("type", "integer");
                } else if (t == Boolean.class || t == boolean.class) {
                    propSchema.put("type", "boolean");
                } else if (java.util.List.class.isAssignableFrom(t)) {
                    propSchema.put("type", "array");
                    ObjectNode items = objectMapper.createObjectNode();
                    items.put("type", "string");
                    propSchema.set("items", items);
                } else {
                    // 默认按字符串处理
                    propSchema.put("type", "string");
                }

                properties.set(propName, propSchema);
                required.add(propName);
            }
        }

        parameters.set("properties", properties);
        parameters.set("required", required);

        function.set("parameters", parameters);
        schema.set("function", function);

        return schema;
    }

    /**
     * 创建标准工具集
     * 包含所有内置工具
     */
    public static ToolRegistry createStandardRegistry(
            BuiltinSystemPromptArgs builtinArgs,
            Approval approval,
            ObjectMapper objectMapper
    ) {
        ToolRegistry registry = new ToolRegistry(objectMapper);

        // 注册文件工具
        registry.register(new ReadFile(builtinArgs));
        registry.register(new WriteFile(builtinArgs, approval));
        registry.register(new StrReplaceFile(builtinArgs, approval));
        registry.register(new Glob(builtinArgs));
        registry.register(new Grep(builtinArgs));

        // 注册 Bash 工具
        registry.register(new Bash(approval));

        // 注册 Think 工具
        registry.register(new Think());

        // 注册 Todo 工具
        registry.register(new SetTodoList());

        // TODO: 注册其他工具
        // registry.register(new Task(...));
        // registry.register(new WebSearch(...));
        // registry.register(new WebFetch(...));

        log.info("Created standard tool registry with {} tools", registry.tools.size());
        return registry;
    }
}
