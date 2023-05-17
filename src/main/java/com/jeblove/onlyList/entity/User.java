package com.jeblove.onlyList.entity;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @author : Jeb
 * @date :2023/5/16 20:36
 * @classname :  User
 * @description : TODO
 */
@Data
public class User{
    private String id;
    private String username;
    private String password;
    private Date signUpTime;
    private String eMail;
    private String phone;
    private String pathId;
    private Map<String, Object> permissions;
}
