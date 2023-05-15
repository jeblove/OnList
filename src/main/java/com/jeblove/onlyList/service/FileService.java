package com.jeblove.onlyList.service;

import com.jeblove.onlyList.common.Result;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private GridFSBucket gridFSBucket;

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
     * @param multipartFile 文件
     * @param fileName 文件名
     * @return 成功则返回文件id
     * @throws IOException
     */
    public Result storeFile(MultipartFile multipartFile, String fileName) throws Exception {
        InputStream inputStream = multipartFile.getInputStream();
        ObjectId objectId = null;

        objectId = gridFsTemplate.store(inputStream, fileName);
        System.out.println(objectId);

        Result result;
        if (objectId.toString().equals("") || objectId.toString() == null || objectId == null){
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

    /**
     * 文件下载
     * @param id 需要下载文件的id
     * @return 返回ResponseEntity类型的字节流
     * @throws IOException
     */
    public ResponseEntity<StreamingResponseBody> downloadFileById(String id) throws IOException{
        GridFSFile  gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        ResponseEntity responseEntity;
        if (gridFSFile==null){
//            返回404
            responseEntity = ResponseEntity.notFound().build();
        }else{
//            打开下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
//            创建resource，用于获取流对象
            GridFsResource resource = new GridFsResource(gridFSFile, gridFSDownloadStream);
//            获取流中的数据
            StreamingResponseBody streamingResponseBody = outputStream -> {
                IOUtils.copy(resource.getInputStream(), outputStream);
            };
            /**
             * HttpHeaders:
             *  CONTENT_TYPE 指示如何响应内容的格式
             *  CONTENT_DISPOSITION 指示如何处理响应内容
             *      inline 直接在页面显示
             *      attachment 以附件形式下载
             *
             * MediaType:
             *  APPLICATION_OCTET_STREAM 告知浏览器是字节流，浏览器处理字节流默认方式是下载
             */
            responseEntity = ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+gridFSFile.getFilename())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(streamingResponseBody);
        }
        return responseEntity;
    }

}
