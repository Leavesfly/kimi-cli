package io.leavesfly.jimi.llm.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 文本内容部分
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TextPart extends ContentPart {
    
    /**
     * 文本内容
     */
    @JsonProperty("text")
    private String text;
    
    @Override
    public String getType() {
        return "text";
    }
    
    /**
     * 便捷构造方法
     */
    public static TextPart of(String text) {
        return new TextPart(text);
    }
}
