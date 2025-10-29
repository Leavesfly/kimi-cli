package io.leavesfly.jimi.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 外部服务配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicesConfig {
    
    /**
     * Moonshot Search 配置
     */
    @JsonProperty("moonshot_search")
    private MoonshotSearchConfig moonshotSearch;
}
