package com.jeblove.onList.service;

import com.jeblove.onList.common.Result;
import com.jeblove.onList.entity.FileLink;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    @Autowired
    private FileService fileService;

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

    /**
     * 删除整个fileLink文档
     * 面向其它服务
     * @param hashCode 文件哈希码
     * @return 删除条数
     */
    public long deleteFileLink(String hashCode){
        DeleteResult deleteResult = mongoTemplate.remove(new Query(Criteria.where("hashCode").is(hashCode)), FileLink.class);
        return deleteResult.getDeletedCount();
    }

    /**
     * 更新fileLink的linkNum和linkUserArray
     * @param hashCode 文件哈希码
     * @param linkNum 总链接数
     * @param linkUserMap 用户链接
     * @return 修改条数
     */
    public long updateFileLinkNum(String hashCode, Integer linkNum, Map<String, Integer> linkUserMap){
        Query query = new Query(Criteria.where("hashCode").is(hashCode));
        Update update = new Update().set("linkNum", linkNum).set("linkUserMap", linkUserMap);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, FileLink.class);
        return updateResult.getModifiedCount();
    }

    /**
     * 删除链接文件
     * 无链接再删除文件
     * @param fileLinkId 文件链接id
     * @param username   删除文件用户
     * @return data删除条数
     */
    public Result deleteFileLinkUpdate(String fileLinkId, String username){
        // 先删除用户num
        FileLink fileLink = getFileLinkById(fileLinkId);
        Map<String, Integer> linkUserMap = fileLink.getLinkUserMap();
        Integer num = linkUserMap.get(username);
        Result result;
        if(num==null){
            // 该用户没有该文件
            result = Result.error(404,"没有该文件");
        }else {
            long count;
            if (num > 1) {
                linkUserMap.put(username, num - 1);
            } else {
                // 该用户链接数为0，删除该用户username
                linkUserMap.remove(username);
            }
            Integer linkNum = fileLink.getLinkNum();
            if (linkNum > 1) {
                fileLink.setLinkNum(linkNum - 1);
                count = updateFileLinkNum(fileLink.getHashCode(), fileLink.getLinkNum(), linkUserMap);
            } else {
                // 没有其它链接，删除链接文件记录
                count = deleteFileLink(fileLink.getHashCode());
                // 删除文件
                fileService.deleteFileById(fileLink.getFileId());
            }
            result = Result.success(count);
        }
        return result;
    }

    /**
     * 获取fileLink列表[api]
     * @return List<FileLink>
     */
    public List<FileLink> getAllFileLinkInfo(){
        List<FileLink> fileLinks = mongoTemplate.findAll(FileLink.class);
        System.out.println(fileLinks);
        return fileLinks;
    }

    /**
     * 获取特定fileLink信息[api admin]
     * @return List<Map<Object, Object>>
     */
    public List<Map<Object, Object>> showFileLinkInfo(){
        List<FileLink> fileLinks = mongoTemplate.findAll(FileLink.class);
        List<Map<Object, Object>> mapList = new ArrayList<>();

        for (FileLink kv : fileLinks){
            HashMap<Object, Object> map = new HashMap<>();
            map.put("id", kv.getId());
            String filename = fileService.getFileInfoById(kv.getFileId()).getFilename();
            map.put("filename", filename);
            map.put("linkNum", kv.getLinkNum());
            map.put("linkUserMap", kv.getLinkUserMap());
            mapList.add(map);
        }
//        System.out.println(mapList);
        return mapList;
    }
}
