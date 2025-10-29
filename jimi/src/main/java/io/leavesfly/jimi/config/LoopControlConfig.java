package io.leavesfly.jimi.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 循环控制配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoopControlConfig {
    
    /**
     * 每次运行的最大步数
     */
    @JsonProperty("max_steps_per_run")
    @Builder.Default
    private int maxStepsPerRun = 100;
    
    /**
     * 每步的最大重试次数
     */
    @JsonProperty("max_retries_per_step")
    @Builder.Default
    private int maxRetriesPerStep = 3;
}
