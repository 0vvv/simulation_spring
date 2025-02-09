package com.lin.spring;

public class BeanDefinition {
    // bean的类型
    private Class type;
    // bean是否单例
    private String scope;


    public void setType(Class<?> clazz) {
        this.type = clazz;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getScope() {
        return this.scope;
    }

    public Class getType() {
        return this.type;
    }
}
