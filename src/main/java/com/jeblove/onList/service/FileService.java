package com.jeblove.onList.service;

import com.jeblove.onList.common.FileCheckHashUtil;
import com.jeblove.onList.common.Result;
import com.jeblove.onList.entity.FileLink;
import com.jeblove.onList.entity.User;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
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
    @Value("${fileLink.hashType}")
    private String hashType;
    @Value("${fileLink.fileLinkPath}")
    private String fileLinkPath;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    @Lazy
    private FileLinkService fileLinkService;
    @Autowired
    private PathService pathService;
    @Autowired
    private UserService userService;

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
     * 删除fs.files的文件
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

    /**
     * 上传文件
     * @param uploadFile 文件
     * @param userId 用户id
     * @param pathList 上传到目录的列表
     * @return data.fileLinkId 文件链接id
     * @throws Exception
     */
    public Result uploadFile(MultipartFile uploadFile, String userId, @RequestParam List<String> pathList) throws Exception {
        User user = userService.getUser(userId);
        String username = user.getUsername();

        System.out.println(uploadFile.getOriginalFilename()+" 文件大小："+uploadFile.getSize());
        // .getName():uploadFile  getOriginalFilename():文件名.xxx
        String fileLinkId;
        String msg;

        // 检测md5
        String hashCode = FileCheckHashUtil.getHashCode(uploadFile);
        // 已存在检测
        if(!fileLinkService.fileLinkExists(hashType, hashCode)){
            // False: 文件不存在，则上传文件
            Result fileResult = storeFile(uploadFile, uploadFile.getOriginalFilename());
            String fileId = (String) fileResult.getData();

            System.out.println(fileLinkPath);
            // 添加FileLInk记录
            FileLink fileLink = fileLinkService.insertFileLink(fileId, hashType, hashCode, fileLinkPath, username);
            fileLinkId = fileLink.getId();
            msg = "文件不存在";
        }else{
            // True: 文件存在，查询链接，返回linkId
            FileLink fileLink = fileLinkService.findFileLinkByHashCode(hashCode);
            fileLinkId = fileLink.getId();
            // 添加链接数和用户名
            fileLinkService.appendFileLink(hashCode, username);
            msg = "文件已存在";
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("fileLinkId",fileLinkId);
        map.put("msg",msg);

        // 添加fileLinkId到指定目录
        long count = pathService.addFileToPath(user.getPathId(), uploadFile.getOriginalFilename(), fileLinkId, pathList);
        map.put("count", String.valueOf(count));

        // 上传到相同位置，不进行链接+1
        if(fileLinkId != null && count==0){
            fileLinkService.deleteFileLinkUpdate(fileLinkId, user.getUsername());
        }

        return Result.success(map);
    }

    /**
     * 删除文件
     * @param userId 用户id
     * @param filename 文件名
     * @param pathList 所在目录（不包含）
     * @return code 200:成功 500失败
     */
    public Result deleteFile(String userId, String filename,@RequestParam List<String> pathList){
        User user = userService.getUser(userId);

        // 获取fileLinkId并从path中删除该文件
        String fileLinkId = pathService.deleteFile(user.getPathId(), filename, pathList);
        System.out.println(fileLinkId);
        if(fileLinkId==null){
            return Result.error(500, "文件或目录不正确");
        }
        // 更新链接文件
        Result result = fileLinkService.deleteFileLinkUpdate(fileLinkId, user.getUsername());
        if(result.getCode() != 200){
            return Result.error(500, "删除链接文件失败");
        }
        return result;
    }

    /**
     * 查询总占用空间
     * @return MB
     */
    public Result queryTotalSize(){
        GridFsResource[] resources = gridFsTemplate.getResources("*");
        long totalSize = 0;
        for(GridFsResource resource: resources){
            try {
                totalSize += resource.contentLength();
            } catch (IOException e) {
                return Result.error(500,"查询失败："+e.getMessage());
            }
        }
        double totalSizeM = totalSize / (1024.0 * 1024.0);
        DecimalFormat df = new DecimalFormat("0.00");
        String formattedTotalSize = df.format(totalSizeM);
        return Result.success(formattedTotalSize);
    }

    /**
     * 获取文件信息
     * @param id 文件id
     * @return 文件信息
     */
    public GridFSFile getFileInfoById(String id){
        GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        return gridFSFile;
    }

    /**
     * 获取文件指定信息
     * @return List<Map<String, Object>> 包含所有文件详细信息的列表，其中每个Map对象代表一个文件的信息：
     * <ul>
     *   <li>"id" - 文件的ObjectId，以字符串形式返回</li>
     *   <li>"filename" - 文件的名称</li>
     *   <li>"chunkSize" - 文件的分块大小（以字节为单位）</li>
     *   <li>"uploadDate" - 文件的上传日期时间</li>
     * </ul>
     */
    public List<Map<Object, Object>> getAllFileInfo(){
        Query query = new Query();
        GridFSFindIterable files = gridFsTemplate.find(query);

        List<Map<Object, Object>> mapList = new ArrayList<>();
        for (GridFSFile file: files){
            HashMap<Object, Object> map = new HashMap<>();
            // 正常输出_id
            map.put("id", file.getId().asObjectId().getValue().toHexString());
            map.put("filename", file.getFilename());
            map.put("chunkSize", file.getChunkSize());
            map.put("uploadDate", file.getUploadDate());
            mapList.add(map);
        }
        return mapList;
    }
}
