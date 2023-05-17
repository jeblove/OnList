package com.jeblove.onlyList.service;

import com.jeblove.onlyList.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

/**
 * @author : Jeb
 * @date :2023/5/16 20:41
 * @classname :  UserService
 * @description : TODO
 */
@Service
public class UserService {
    @Autowired
    private MongoTemplate mongoTemplate;

    public User getUser(String id){
        return mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)), User.class);
    }

}
