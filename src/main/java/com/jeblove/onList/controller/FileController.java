package com.jeblove.onList.controller;

import com.jeblove.onList.common.Result;
import com.jeblove.onList.entity.FileLink;
import com.jeblove.onList.entity.User;
import com.jeblove.onList.service.*;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author : Jeb
 * @date :2023/5/11 22:47
 * @classname :  FileController
 * @description : TODO
 */
@RequestMapping("/file/")
@RestController
public class FileController {
    @Autowired
    private FileService fileService;
    @Autowired
    private FileLinkService fileLinkService;
    @Autowired
    @Lazy
    private PathService pathService;
    @Autowired
    @Lazy
    private UserService userService;
    @Resource
    private RouteService routeService;

    /**
     * 获取fs.file文件信息
     * @param name fs.files文件名
     * @return 文件信息
     */
    @RequestMapping("getFileByName")
    public Result getFileByName(String name){
        return fileService.getFileByName(name);
    }

    /**
     * 获取所有fs.files文件信息
     * @return 列表形式的所有文件信息
     */
    @RequestMapping("getFileList")
    public List<GridFSFile> getFileList(){
        return fileService.getFileList();
    }

    /**
     * 实际删除file文件
     * @param id fs.file的id
     * @return 返回true则删除成功，返回500代码则失败
     */
    @RequestMapping("deleteFileById")
    public Result deleteFileById(String id){
        return fileService.deleteFileById(id);
    }

    /**
     * 下载文件[api]
     * @param fileLinkId 文件链接id
     * @return 文件流
     * @throws IOException
     */
    @RequestMapping("downloadFileByFileLinkId")
    public ResponseEntity<StreamingResponseBody> downloadFileByFileLinkId(String fileLinkId) throws IOException{
        FileLink fileLink = fileLinkService.getFileLinkById(fileLinkId);
        return fileService.downloadFileById(fileLink.getFileId());
    }

    /**
     * 下载文件
     * @param id fs.file文件id
     * @return 文件流
     * @throws IOException
     */
    @RequestMapping("downloadFileById")
    public ResponseEntity<StreamingResponseBody> downloadFileById(String id) throws IOException {
        return fileService.downloadFileById(id);
    }

    /**
     * 上传文件
     * api
     * @param uploadFile 文件
     * @param userId 用户id
     * @param pathList 所在目录的列表
     * @return data.fileLinkId 文件链接id
     * @throws Exception
     */
    @RequestMapping("upload")
    public Result uploadFile(MultipartFile uploadFile, String userId, @RequestParam List<String> pathList) throws Exception {
        Result result = fileService.uploadFile(uploadFile, userId, pathList);
        // 更新redis缓存
        routeService.updateRedisValue(userId);
        return result;
    }


    /**
     * 删除文件
     * api
     * @param userId 用户id
     * @param filename 文件名
     * @param pathList 所在目录（不包含）
     * @return code 200:成功 500失败
     */
    @RequestMapping("deleteFile")
    public Result deleteFile(String userId, String filename,@RequestParam List<String> pathList){
        Result result = fileService.deleteFile(userId, filename, pathList);
        // 更新redis缓存
        routeService.updateRedisValue(userId);
        return result;
    }

    /**
     * 拷贝文件
     * api
     * @param userId 用户id
     * @param filename 文件名
     * @param pathList 所在路径（不包含）
     * @param targetPathList 目标路径（不包含）
     * @return 成功则1
     */
    @RequestMapping("cpFile")
    public Result cpFile(String userId, String filename,@RequestParam List<String> pathList,@RequestParam List<String> targetPathList){
        User user = userService.getUser(userId);
        long count = pathService.copyAMoveFile(false, user.getPathId(), user.getUsername(), filename, pathList, filename, targetPathList);
        Result result = Result.error(500, "拷贝失败");
        if(count>0){
            result = Result.success(count);
        }
        // 更新redis缓存
        routeService.updateRedisValue(userId);
        return result;
    }

    /**
     * 剪切文件
     * api
     * @param userId 用户id
     * @param filename 文件名
     * @param pathList 所在路径（不包含）
     * @param targetPathList 目标路径（不包含）
     * @return 成功则1
     */
    @RequestMapping("mvFile")
    public Result mvFile(String userId, String filename,@RequestParam List<String> pathList,@RequestParam List<String> targetPathList){
        User user = userService.getUser(userId);
        long count = pathService.copyAMoveFile(true, user.getPathId(), user.getUsername(), filename, pathList, filename, targetPathList);
        Result result = Result.error(500, "剪切失败");
        if(count>0){
            result = Result.success(count);
        }
        // 更新redis缓存
        routeService.updateRedisValue(userId);
        return result;
    }

    /**
     * 文件重命名
     * api
     * @param userId 用户id
     * @param filename 文件名
     * @param pathList 所在路径（不包含）
     * @param newName 新文件名
     * @return 成功则1
     */
    @RequestMapping("renameFile")
    public Result renameFile(String userId, String filename,@RequestParam  List<String> pathList, String newName){
        User user = userService.getUser(userId);
        long count = pathService.copyAMoveFile(true, user.getPathId(), user.getUsername(), filename, pathList, newName, pathList);
        Result result = Result.error(500, "重命名失败");
        if(count>0){
            result = Result.success(count);
        }
        // 更新redis缓存
        routeService.updateRedisValue(userId);
        return result;
    }

    /**
     * 查询总占用空间
     * @return MB
     */
    @RequestMapping("queryTotalSize")
    public Result queryTotalSize(){
        return fileService.queryTotalSize();
    }

    @RequestMapping("getAllFileInfo")
    public Result getAllFileInfo() {
        return Result.success(fileService.getAllFileInfo());
    }
}
