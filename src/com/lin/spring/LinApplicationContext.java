package com.lin.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class LinApplicationContext {
    private Class configClass;
    // 存储bean的定义
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    // 存储单例池
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();

    private ArrayList<BeanPostProcessor> beanPostProcessors = new ArrayList<>();


    public LinApplicationContext(Class configClass) {
        this.configClass = configClass;
        // 扫描，有ComponentScan注解的类的话就要扫描
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            String path = componentScan.value(); // 扫描路径com.lin.service，真正要扫描的是里面的.class文件
            path = path.replace(".", "/"); // 替换成com/lin/service
//            System.out.println(path);

            ClassLoader classLoader = LinApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path); // 获取相对路径path的绝对路径
            File file = new File(resource.getFile()); // 获取文件
//            System.out.println(file);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    String fileName = f.getAbsolutePath();
//                    System.out.println(fileName);
                    if (fileName.endsWith(".class")) {
                        // 获取扫描路径下的类名
                        String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                        className = className.replace("\\", ".");
//                        System.out.println(className);
                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            // 如果这个class是一个带Component注解的类，说明这里定义了一个Bean
                            if (clazz.isAnnotationPresent(Component.class)) {
                                // 如果这个类实现了BeanPostProcessor接口，就把这个类加到beanPostProcessors里面
                                if(BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                    BeanPostProcessor instance = (BeanPostProcessor) clazz.getConstructor().newInstance();
                                    beanPostProcessors.add(instance);
                                }

                                // 拿到Component注解的value值，就是bean的名字
                                String beanName = clazz.getAnnotation(Component.class).value();
                                if (beanName.equals("")) {
                                    // 如果没有指定bean的名字，就用类名首字母小写作为bean的名字
                                    beanName = Introspector.decapitalize(clazz.getSimpleName());
                                }

                                // 生成beanDefinition
                                BeanDefinition beanDefinition = new BeanDefinition();
                                // 有scope注解的话就不是默认的单例
                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                                    // 获取注解要求的值
                                    String scope = scopeAnnotation.value();
                                    beanDefinition.setScope(scope);
                                } else {
                                    // 默认bean是单例
                                    beanDefinition.setScope("singleton");
                                }
                                beanDefinition.setType(clazz);  // 类型
                                // 存储beanDefinition到map中
                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        } catch (InstantiationException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            } else {

            }
        }

        // 扫描完之后，可以把所有的单例bean创建起来，放进单例池进行管理（后续从这里拿）
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getType();
        try {
            // bean的实例化
            Object instance = clazz.getConstructor().newInstance();
            // 依赖注入，要给加了Autowired注解的字段赋值
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(Autowired.class)) {
                    f.setAccessible(true);
                    // 找出名为f.getName()的bean，然后赋值给f
                    f.set(instance, getBean(f.getName()));
                }
            }
            // Aware回调（自定义了bean的名称）如果实现了BeanNameAware接口，就调用setBeanName方法
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }
            // AOP，bean初始化前的操作
            for(BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                instance = beanPostProcessor.postProcessBeforeInitialization(beanName, instance);
            }
            // initializingBean，调用初始化方法
            if (instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }
            // AOP，bean初始化后的操作
            for(BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                instance = beanPostProcessor.postProcessAfterInitialization(beanName, instance);
            }

            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getBean(String beanName) {
        // 根据名字找到类，然后实例化，
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new NullPointerException();
        } else {
            String scope = beanDefinition.getScope();
            if (scope.equals("singleton")) {
                // 单例，从单例池中拿
                Object bean = singletonObjects.get(beanName);
                if (bean == null) {
                    // 如果单例池中没有（懒加载），就创建一个
                    bean = createBean(beanName, beanDefinition);
                    singletonObjects.put(beanName, bean);
                }
                return bean;
            } else {
                // 多例，每次都创建一个新的
                return createBean(beanName, beanDefinition);
            }
        }
    }
}

