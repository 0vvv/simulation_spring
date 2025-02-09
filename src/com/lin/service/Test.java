package com.lin.service;

import com.lin.spring.LinApplicationContext;

public class Test {
    public static void main(String[] args) {
        LinApplicationContext applicationContext = new LinApplicationContext(AppConfig.class);
        // 默认单例bean，多次获取都是同一个对象；如果是prototype，多次获取是不同的对象
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));
    }
}