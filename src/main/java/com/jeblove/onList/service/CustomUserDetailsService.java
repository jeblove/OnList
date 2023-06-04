package com.jeblove.onList.service;

import com.jeblove.onList.entity.CustomUserDetails;
import com.jeblove.onList.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


/**
 * @author : Jeb
 * @date :2023/6/4 15:23
 * @classname :  CustomUserDetailsService
 * @description : 用于安全认证
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 根据用户名查询
     * @param username 用户名
     * @return 用户信息
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Query query = new Query(Criteria.where("username").is(username));
        User user = mongoTemplate.findOne(query, User.class);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return new CustomUserDetails(user);
    }
}
