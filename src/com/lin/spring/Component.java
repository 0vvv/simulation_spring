package com.lin.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
// Target(ElementType.TYPE)表示这个注解只能加在类上
public @interface Component {
//    给bean指定名字
    String value() default "";

}
