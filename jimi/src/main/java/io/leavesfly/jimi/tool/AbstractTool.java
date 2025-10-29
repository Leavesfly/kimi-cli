package io.leavesfly.jimi.tool;

/**
 * 工具抽象基类
 * 提供工具的通用实现
 * 
 * @param <P> 参数类型
 */
public abstract class AbstractTool<P> implements Tool<P> {
    
    private final String name;
    private final String description;
    private final Class<P> paramsType;
    
    protected AbstractTool(String name, String description, Class<P> paramsType) {
        this.name = name;
        this.description = description;
        this.paramsType = paramsType;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public Class<P> getParamsType() {
        return paramsType;
    }
}
