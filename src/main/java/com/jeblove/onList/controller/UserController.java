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

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 成功：token；失败code 502
     */
    @RequestMapping("login")
    public Result login(String username, String password){
        return userService.loginSecurity(username, password);
    }

    /**
     * 用户注册
     * @param user 用户信息
     * @return 用户信息
     * @throws Exception
     */
    @RequestMapping("register")
    public Result register(User user) throws Exception {
        Result register = userService.register(user);
        if(register.getCode()!=200){
            return Result.error(register.getCode(),register.getMessage());
        }
        return Result.success(register.getData());
    }

    /**
     * 删除用户
     * @param id 用户id
     * @param password 用户密码
     * @return code：成功则200，失败则502
     */
    @RequestMapping("deleteUser")
    public Result deleteUser(String id, String password){
        return userService.deleteUser(id, password);
    }

    @RequestMapping("modifyUser")
    public Result modifyUser(String username, String password, User user){
        return userService.modifyUser(username, password, user);
    }

    @RequestMapping("getAllUserInfo")
    public Result getAllUserInfo(){
        return Result.success(userService.getAllUserInfo());
    }
}
