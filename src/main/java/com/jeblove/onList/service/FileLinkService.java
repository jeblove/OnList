package com.jeblove.onList.service;

import com.jeblove.onList.entity.FileLink;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 查询所有
     * @return 列表形式fileLInk信息
     */
    public List<FileLink> findAll(){
        return mongoTemplate.findAll(FileLink.class);
    }

    /**
     * 查询链接文件信息
     * @param id fileLink的id
     * @return fileLink信息
     */
    public FileLink getFileLinkById(String id){
        return mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)), FileLink.class);
    }

    /**
     * 查询链接文件记录是否存在
     * @param type 哈希类型，例如：MD5
     * @param code 文件哈希码
     * @return 文件不存在返回FALSE，文件存在返回TRUE
     */
    public Boolean fileLinkExists(String type, String code){
        FileLink fileLink = mongoTemplate.findOne(new Query(Criteria.where("hashType").is(type).and("hashCode").is(code)), FileLink.class);
        Boolean result;
        if(fileLink==null){
            // 文件不存在，可以上传
            result = Boolean.FALSE;
        }else{
            // 文件存在，直接链接
            result = Boolean.TRUE;
        }
        return result;
    }

    /**
     * 添加链接文件记录
     * @param fileId fs.file文件id
     * @param hashType 哈希类型
     * @param hashCode 文件哈希码
     * @param path 路径记录
     * @param username 上传用户名
     * @return FileLink信息
     */
    public FileLink insertFileLink(String fileId, String hashType, String hashCode, String path,
                                   String username){
        FileLink fileLink = new FileLink();
        fileLink.setFileId(fileId);
        fileLink.setHashType(hashType);
        fileLink.setHashCode(hashCode);
        fileLink.setPath(path);
        fileLink.setLinkNum(1);
        Map<String, Integer> linkUserMap = new HashMap<>();
        linkUserMap.put(username, 1);
        fileLink.setLinkUserMap(linkUserMap);
        return mongoTemplate.insert(fileLink);
    }

    /**
     * 追加fileLink记录
     * @param hashCode 文件哈希码
     * @param username 用户名
     * @return 成功修改数
     */
    public long appendFileLink(String hashCode, String username){
        FileLink fileLink = findFileLinkByHashCode(hashCode);
        Map<String, Integer> linkUserMap = fileLink.getLinkUserMap();

        Query query = new Query(Criteria.where("hashCode").is(hashCode));
        Update update = new Update().set("linkNum",fileLink.getLinkNum()+1);

        if(linkUserMap.containsKey(username)){
            linkUserMap.put(username, linkUserMap.get(username)+1);
            update.set("linkUserMap", linkUserMap);
        }else{
            // 不存在该username
            linkUserMap.put(username, 1);
            update.set("linkUserMap",linkUserMap);
        }
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, FileLink.class);
        return updateResult.getModifiedCount();
    }

    /**
     * 查询链接文件记录
     * @param hashCode 文件哈希码
     * @return fileLink信息
     */
    public FileLink findFileLinkByHashCode(String hashCode){
        return mongoTemplate.findOne(new Query(Criteria.where("hashCode").is(hashCode)), FileLink.class);
    }



}
