package com.jeblove.onlyList.controller;

import com.jeblove.onlyList.entity.FileLink;
import com.jeblove.onlyList.service.FileLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
