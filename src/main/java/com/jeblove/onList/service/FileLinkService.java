package com.jeblove.onList.service;

import com.jeblove.onList.entity.FileLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * @author : Jeb
 * @date :2023/5/16 17:33
 * @classname :  FileLinkService
 * @description : TODO
 */
@Service
public class FileLinkService {
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<FileLink> findAll(){
        return mongoTemplate.findAll(FileLink.class);
    }

    public FileLink getFileLinkById(String id){
        return mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)), FileLink.class);
    }

}
