package com.jeblove.onList.controller;

import com.jeblove.onList.common.Result;
import com.jeblove.onList.service.FileService;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
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
    public Result uploadFile(MultipartFile uploadFile) throws Exception {
        System.out.println(uploadFile.getOriginalFilename()+uploadFile.getSize());
        // .getName():uploadFile  getOriginalFilename():文件名.xxx
        return fileService.storeFile(uploadFile, uploadFile.getOriginalFilename());
    }

    @RequestMapping("deleteFileById")
    public Result deleteFileById(String id){
        return fileService.deleteFileById(id);
    }

    @RequestMapping("downloadFileById")
    public ResponseEntity<StreamingResponseBody> downloadFileById(String id) throws IOException {
        return fileService.downloadFileById(id);

    }
}
