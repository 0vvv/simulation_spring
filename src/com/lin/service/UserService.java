package com.lin.service;

import com.lin.spring.Autowired;
import com.lin.spring.Component;
import com.lin.spring.Scope;

@Component
@Scope("prototype")
public class UserService {
    @Autowired
    private OrderService orderService;

    public void test() {
        System.out.println(orderService);
    }
}
