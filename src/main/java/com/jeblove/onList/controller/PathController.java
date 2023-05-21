package com.jeblove.onList.controller;

import com.jeblove.onList.common.Result;
import com.jeblove.onList.entity.Path;
import com.jeblove.onList.entity.User;
import com.jeblove.onList.service.PathService;
import com.jeblove.onList.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    /**
     * 创建目录
     * api
     * @param userId 用户id
     * @param folderName 目录名
     * @param pathList 所在目录列表（不包含）
     * @return code 200:成功 500失败
     */
    @RequestMapping("createDir")
    public Result createDir(String userId, String folderName, @RequestParam List<String> pathList){
        System.out.println(pathList);
        User user = userService.getUser(userId);
        System.out.println(user.getPathId());
        String pathId = user.getPathId();
        Result result = pathService.createDir(pathId, folderName, pathList);
        if(result.getCode() != 200 ){
            result = Result.error(result.getCode(), result.getMessage());
        }
        return result;
    }

    /**
     * 删除目录
     * api
     * @param userId 用户id
     * @param folderName 目录名
     * @param pathList 所在目录列表（不包含）
     * @return
     */
    @RequestMapping("deleteDir")
    public Result deleteDir(String userId, String folderName,@RequestParam List<String> pathList){
        User user = userService.getUser(userId);
        String pathId = user.getPathId();
        long count = pathService.deleteDir(pathId, folderName, pathList);
        Result result = Result.success(count);
        if(count==0){
            result = Result.error(500, "删除失败");
        }
        return result;
    }
}
