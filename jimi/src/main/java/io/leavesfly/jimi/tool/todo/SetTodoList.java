package io.leavesfly.jimi.tool.todo;

import io.leavesfly.jimi.tool.AbstractTool;
import io.leavesfly.jimi.tool.ToolResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置待办事项列表工具
 * 用于管理和显示待办事项
 */
@Slf4j
public class SetTodoList extends AbstractTool<SetTodoList.Params> {
    
    /**
     * 待办事项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Todo {
        /**
         * 待办事项标题
         */
        private String title;
        
        /**
         * 待办事项状态：Pending, In Progress, Done
         */
        private String status;
    }
    
    /**
     * 参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Params {
        /**
         * 待办事项列表
         */
        @Builder.Default
        private List<Todo> todos = new ArrayList<>();
    }
    
    public SetTodoList() {
        super(
            "SetTodoList",
            """
            Set or update a todo list to track task progress.
            
            This tool allows you to manage a list of tasks with their current status.
            Each todo item has a title and a status (Pending, In Progress, or Done).
            
            Use this tool to:
            - Create a new todo list
            - Update the status of existing todos
            - Add new todos to the list
            - Remove completed todos
            
            Parameters:
            - todos: List of todo items, each with a title and status
            
            Example:
            {
              "todos": [
                {"title": "Research API integration", "status": "Done"},
                {"title": "Implement authentication", "status": "In Progress"},
                {"title": "Write unit tests", "status": "Pending"}
              ]
            }
            """,
            Params.class
        );
    }
    
    @Override
    public Mono<ToolResult> execute(Params params) {
        return Mono.defer(() -> {
            if (params.todos == null || params.todos.isEmpty()) {
                return Mono.just(ToolResult.ok(
                    "Todo list is empty.",
                    "Empty todo list"
                ));
            }
            
            // 渲染待办事项列表
            StringBuilder rendered = new StringBuilder();
            for (Todo todo : params.todos) {
                rendered.append("- ")
                    .append(todo.getTitle())
                    .append(" [")
                    .append(todo.getStatus())
                    .append("]\n");
            }
            
            log.info("Todo list updated with {} items", params.todos.size());
            
            return Mono.just(ToolResult.ok(rendered.toString(), ""));
        });
    }
}
