package com.jeblove.onList.controller;

import com.jeblove.onList.common.Result;
import com.jeblove.onList.entity.FileLink;
import com.jeblove.onList.service.FileLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author : Jeb
 * @date :2023/5/16 20:16
 * @classname :  FileLinkController
 * @description : TODO
 */
@RequestMapping("/fileLink/")
@RestController
public class FileLinkController {
    @Autowired
    private FileLinkService fileLinkService;
    @RequestMapping("findAll")
    public List<FileLink> findAll(){
        return fileLinkService.findAll();
    }

    @RequestMapping("findOne")
    public FileLink findOne(String id){
        return fileLinkService.getFileLinkById(id);
    }

    @RequestMapping("getAllFileLinkInfo")
    public Result getAllFileLinkInfo(){
        return Result.success(fileLinkService.getAllFileLinkInfo());
    }

    @RequestMapping("showFileLinkInfo")
    public List<Map<Object, Object>> showFileLinkInfo(){
        return fileLinkService.showFileLinkInfo();
    }
}
