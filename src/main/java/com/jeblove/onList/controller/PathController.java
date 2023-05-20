package com.jeblove.onList.controller;

import com.jeblove.onList.entity.Path;
import com.jeblove.onList.entity.User;
import com.jeblove.onList.service.PathService;
import com.jeblove.onList.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : Jeb
 * @date :2023/5/18 0:32
 * @classname :  PathController
 * @description : TODO
 */
@RequestMapping("/path/")
@RestController
public class PathController {
    @Autowired
    private PathService pathService;
    @Autowired
    private UserService userService;

    @RequestMapping("getPathById")
    public Path getPathById(String id){
        return pathService.findById(id);
    }

    @RequestMapping("createDir")
    public long createDir(String userId, String folderName, @RequestParam List<String> pathList){
        System.out.println(pathList);
        User user = userService.getUser(userId);
        System.out.println(user.getPathId());
        String pathId = user.getPathId();
        return pathService.createDir(pathId, folderName, pathList);
    }

    @RequestMapping("deleteDir")
    public long deleteDir(String userId, String folderName,@RequestParam List<String> pathList){
        User user = userService.getUser(userId);
        String pathId = user.getPathId();
        return pathService.deleteDir(pathId, folderName, pathList);
    }
}
