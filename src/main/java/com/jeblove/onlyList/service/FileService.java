package com.jeblove.onlyList.service;

import com.jeblove.onlyList.common.Result;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : Jeb
 * @date :2023/5/12 11:09
 * @classname :  FileService
 * @description : 文件的上传，下载，查询
 */
@Service
public class FileService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    /**
     * 获取文件信息
     * @param name 文件名
     * @return 文件信息
     */
    public Result getFileByName(String name){
        Query query = new Query(Criteria.where("filename").is(name));
        GridFSFile gridFSFile = gridFsTemplate.findOne(query);
        Result result;
        if (gridFSFile==null){
//            文件不存在
            result = Result.error(500,"文件不存在");
        }else {
            result = Result.success(gridFSFile);
        }
        return result;
    }

    /**
     * 获取所有文件信息
     * @return 所有文件信息
     */
    public List<GridFSFile> getFileList(){
        List<GridFSFile> fileList = new ArrayList<>();
        gridFsTemplate.find(new Query()).into(fileList);
        return fileList;
    }

    /**
     * 上传文件到数据库
     * @param inputStream 文件流
     * @param fileName 文件名
     * @return 成功则返回文件id
     * @throws IOException
     */
    public Result storeFile(InputStream inputStream, String fileName) throws IOException {
        ObjectId objectId = gridFsTemplate.store(inputStream, fileName);
        Result result;
        if (objectId.toString().equals("")|| objectId.toString() == null){
            result = Result.error(500,"上传失败");
        }else{
            result = Result.success(objectId.toString());
        }
        inputStream.close();
        return result;
    }

    /**
     * 删除文件
     * @param id 文件id
     * @return 成功code：200，失败code：500
     */
    public Result deleteFileById(String id){
        GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        Result result;
        if (gridFSFile==null){
//            文件不存在
            result = Result.error(500,"文件不存在");
        }else {
            result = Result.success(true);
            gridFsTemplate.delete(new Query(Criteria.where("_id").is(id)));
        }
        return result;
    }





}
