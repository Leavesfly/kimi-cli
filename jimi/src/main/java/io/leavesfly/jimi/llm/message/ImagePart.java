package io.leavesfly.jimi.llm.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 图片内容部分
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ImagePart extends ContentPart {
    
    /**
     * 图片 URL 或 Base64 编码
     */
    @JsonProperty("url")
    private String url;
    
    /**
     * 图片详细程度（可选）
     */
    @JsonProperty("detail")
    private String detail;
    
    @Override
    public String getType() {
        return "image";
    }
    
    /**
     * 便捷构造方法
     */
    public static ImagePart of(String url) {
        return new ImagePart(url, null);
    }
}
