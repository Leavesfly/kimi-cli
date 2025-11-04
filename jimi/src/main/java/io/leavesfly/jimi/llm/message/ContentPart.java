package io.leavesfly.jimi.llm.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 内容部分抽象类
 * 消息内容可以包含多种类型的部分
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextPart.class, name = "text"),
    @JsonSubTypes.Type(value = ImagePart.class, name = "image")
})
public abstract class ContentPart {
    
    /**
     * 获取内容类型
     */
    public abstract String getType();
}
