package com.jeblove.onList.service;

import com.jeblove.onList.entity.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

/**
 * @author : Jeb
 * @date :2023/5/17 23:53
 * @classname :  PathService
 * @description : TODO
 */
@Service
public class PathService {
    @Autowired
    private MongoTemplate mongoTemplate;

    public Path findById(String id){
        Query query = new Query(Criteria.where("_id").is(id));
        return mongoTemplate.findOne(query, Path.class);
    }
}
