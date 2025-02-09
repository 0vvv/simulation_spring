package com.lin.service;

import com.lin.spring.BeanPostProcessor;
import com.lin.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class LinBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(String beanName, Object bean) {
        if(beanName.equals("userService")) {
            // 就可以把bean这个object强制转换成UserService进行一些特定的操作
            System.out.println("UserService初始化之前");
        }
        System.out.println("初始化之前");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(String beanName, Object bean) {
        System.out.println("初始化之后");
        if(beanName.equals("userService")) {
            Object proxyInstance = Proxy.newProxyInstance((LinBeanPostProcessor.class).getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    // 调用bean的方法时，要先执行这里的逻辑，即切面方法，再返回原来要执行的那个方法 return method.invoke(bean, args);
                    // 动态代理是Java中的一个特性，它允许在运行时为一组接口创建一个代理实例。这个代理可以拦截方法调用并在方法执行前后添加自定义行为
                    System.out.println("切面方法");
                    return method.invoke(bean, args);
                }
            });
            return proxyInstance;
        }
        return bean;
    }
}
