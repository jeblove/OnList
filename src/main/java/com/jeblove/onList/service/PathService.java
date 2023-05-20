package com.jeblove.onList.service;

import com.jeblove.onList.entity.Path;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import java.util.*;

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
        Map<String, Path.Node> map = new HashMap<>();
        node.setContent(map);

        Path path = new Path();
        Map<String, Path.Node> content = new HashMap<>();
        content.put(filenameWithoutSuffix, node);
        path.setContent(content);

        return mongoTemplate.insert(path);
    }

    /**
     * 点目录处理
     * @param folderName 文件夹名
     * @param pathList 文件夹名所在的目录
     * @return 用.连接起来的字符串目录
     */
    public String handleDir(String folderName, List<String> pathList){
        List<String> list = new ArrayList<>(pathList);
        // if判断是否为根目录
        if(list.isEmpty()){
            System.out.println("/");
        }else{
            // 防止传递空字符串[""]列表
            if(list.get(0).equals("")){
                list.remove(0);
            }
            for(int i=0; i<list.size(); i++){
                // 下标偶数
                if(i%2==0){
                    list.add(i, "content");
                }
            }
        }
        list.add("content");
        list.add(folderName);

        // 文件夹
        String dirPath = String.join(".", list);
        System.out.println(dirPath);
        return dirPath;
    }

    /**
     * 创建文件夹
     * @param pathId 路径id
     * @param folderName 文件夹名
     * @param pathList 创建目录路径的列表（不含创建名）
     * @return 修改数
     */
    public long createDir(String pathId, String folderName, List<String> pathList){
        String dirPath = handleDir(folderName, pathList);

        Query query = new Query(Criteria.where("_id").is(pathId));
        // 属性
        String typeKey = dirPath+".type";
        String suffixKey = dirPath+".suffix";
        String fileLinkIdKey = dirPath+".fileLinkId";
        String contentKey = dirPath+".content";

        Update update = new Update()
                .set(typeKey, 1)
                .set(suffixKey, "")
                .set(fileLinkIdKey, "")
                .set(contentKey, new HashMap<>());

        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Path.class);
        System.out.println(updateResult);
        return updateResult.getModifiedCount();
    }

    /**
     * 删除文件夹
     * @param pathId 路径id
     * @param folderName 文件夹名
     * @param pathList 删除目录路径的列表（不含删除名）
     * @return 删除数
     */
    public long deleteDir(String pathId, String folderName, List<String> pathList){
        String dirPath = handleDir(folderName, pathList);

        Query query = new Query(Criteria.where("_id").is(pathId));
        Update update = new Update().unset(dirPath);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Path.class);
        System.out.println(updateResult);
        return updateResult.getModifiedCount();
    }
}
