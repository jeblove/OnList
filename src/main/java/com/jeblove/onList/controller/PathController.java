package com.jeblove.onList.controller;

import com.jeblove.onList.common.Result;
import com.jeblove.onList.entity.Path;
import com.jeblove.onList.entity.User;
import com.jeblove.onList.service.PathService;
import com.jeblove.onList.service.RouteService;
import com.jeblove.onList.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
    @Resource
    private RouteService routeService;

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
        // 更新redis缓存
        routeService.updateRedisValue(userId);
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
        long count = pathService.deleteDir(user.getUsername(), pathId, folderName, pathList);
        Result result = Result.success(count);
        if(count==0){
            result = Result.error(500, "删除失败");
        } else if (count==-1) {
            result = Result.error(500, "目录或文件不存在");
        }
        // 更新redis缓存
        routeService.updateRedisValue(userId);
        return result;
    }

    /**
     * 拷贝目录
     * api
     * @param userId 用户id
     * @param filename 目录名
     * @param pathList 所在路径（不包含）
     * @param targetPathList 目标路径（不包含）
     * @return 成功则1
     */
    @RequestMapping("cpDir")
    public Result cpDir(String userId, String filename,@RequestParam List<String> pathList,@RequestParam List<String> targetPathList){
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
     * 剪切目录
     * api
     * @param userId 用户id
     * @param filename 目录名
     * @param pathList 所在路径（不包含）
     * @param targetPathList 目标路径（不包含）
     * @return 成功则1
     */
    @RequestMapping("mvDir")
    public Result mvDir(String userId, String filename,@RequestParam List<String> pathList,@RequestParam List<String> targetPathList){
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
     * 目录重命名
     * api
     * @param userId 用户id
     * @param filename 目录名
     * @param pathList 所在路径（不包含）
     * @param newName 新文件名
     * @return 成功则1
     */
    @RequestMapping("renameDir")
    public Result renameDir(String userId, String filename,@RequestParam List<String> pathList, String newName){
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

    @RequestMapping("getAllPathInfo")
    public Result getAllPathInfo(){
        return Result.success(pathService.getAllPathInfo());
    }
}
