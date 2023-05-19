package com.jeblove.onList.controller;

import com.jeblove.onList.common.Result;
import com.jeblove.onList.entity.User;
import com.jeblove.onList.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : Jeb
 * @date :2023/5/18 22:59
 * @classname :  UserController
 * @description : TODO
 */
@RequestMapping("/user/")
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("login")
    public Result login(String username, String password){
        return userService.login(username, password);
    }

    @RequestMapping("register")
    public String register(User user){
        return userService.register(user);
    }
}
