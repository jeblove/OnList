package com.jeblove.onlyList.controller;

import com.jeblove.onlyList.common.Result;
import com.jeblove.onlyList.service.FileService;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Result uploadFile(MultipartFile uploadFile) throws IOException {
        System.out.println(uploadFile.getOriginalFilename()+uploadFile.getSize());
        // .getName():uploadFile  getOriginalFilename():文件名.xxx
        return fileService.storeFile(uploadFile.getInputStream(), uploadFile.getOriginalFilename());
    }

    @RequestMapping("deleteFileById")
    public Result deleteFileById(String id){
        return fileService.deleteFileById(id);
    }
}
