package com.jeblove.onList.service;

import com.jeblove.onList.entity.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

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

    public Path insertPath(String filename, String fileLinkId) {
        Integer lastIndexOfDot = filename.lastIndexOf('.');

        String suffix;
        String filenameWithoutSuffix;
        if(lastIndexOfDot==-1){
            // 无文件后缀（没有找到点号）
            suffix = "";
            filenameWithoutSuffix = filename;
        }else{
            suffix = filename.substring(lastIndexOfDot+1);
            filenameWithoutSuffix = filename.substring(0, lastIndexOfDot);
        }

        Path.Node node = new Path.Node();
        node.setType(0);
        node.setSuffix(suffix);
        node.setFileLinkId(fileLinkId);

        Path path = new Path();
        Map<String, Path.Node> content = new HashMap<>();
        content.put(filenameWithoutSuffix, node);
        path.setContent(content);

        return mongoTemplate.insert(path);
    }
}
