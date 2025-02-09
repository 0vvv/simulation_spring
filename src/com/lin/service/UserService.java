package com.lin.service;

import com.lin.spring.*;

@Component
@Scope("prototype")
public class UserService implements BeanNameAware, InitializingBean, UserInterface {
    @Autowired
    private OrderService orderService;
    private String beanName;

    @Override
    public void test() {
        System.out.println("userService test");
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("初始化了");
    }
}
