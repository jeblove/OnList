package com.jeblove.onList.controller;

import com.jeblove.onList.common.FileCheckHashUtil;
import com.jeblove.onList.common.Result;
import com.jeblove.onList.entity.FileLink;
import com.jeblove.onList.service.FileLinkService;
import com.jeblove.onList.service.FileService;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.IOException;
import java.util.HashMap;
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

    @Value("${fileLink.hashType}")
    private String hashType;
    @Value("${fileLink.fileLinkPath}")
    private String fileLinkPath;

    @RequestMapping("test")
    public String test(){
        return "Test";
    }

    @RequestMapping("getFileByName")
    public Result getFileByName(String name){
        return fileService.getFileByName(name);
    }

    @RequestMapping("getFileList")
    public List<GridFSFile> getFileList(){
        return fileService.getFileList();
    }

    @RequestMapping("upload")
    public Result uploadFile(MultipartFile uploadFile, String username) throws Exception {
        System.out.println(uploadFile.getOriginalFilename()+" 文件大小："+uploadFile.getSize());
        // .getName():uploadFile  getOriginalFilename():文件名.xxx
        String fileLinkId;
        String msg;

        // 检测md5
        String hashCode = FileCheckHashUtil.getHashCode(uploadFile);
        // 已存在检测
        if(!fileLinkService.fileLinkExists(hashType, hashCode)){
            // False: 文件不存在，则上传文件
            Result fileResult = fileService.storeFile(uploadFile, uploadFile.getOriginalFilename());
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
        return Result.success(map);
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

    @RequestMapping("downloadFileById")
    public ResponseEntity<StreamingResponseBody> downloadFileById(String id) throws IOException {
        return fileService.downloadFileById(id);

    }
}
