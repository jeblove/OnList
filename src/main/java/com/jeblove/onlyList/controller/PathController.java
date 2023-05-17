package com.jeblove.onlyList.controller;

import com.jeblove.onlyList.entity.Path;
import com.jeblove.onlyList.service.PathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping("getPathById")
    public Path getPathById(String id){
        return pathService.findById(id);
    }
}
