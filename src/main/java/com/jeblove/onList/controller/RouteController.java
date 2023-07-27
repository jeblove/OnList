package com.jeblove.onList.controller;

import com.jeblove.onList.common.Result;
import com.jeblove.onList.service.RouteService;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author : Jeb
 * @date :2023/6/1 19:29
 * @classname :  RouteController
 * @description : TODO
 */

@RestController
public class RouteController {
    @Resource
    private RouteService routeService;

    /**
     * 根据路径获取指定path信息
     * @param route 路径，例如'/'，‘/test’
     * @param userId 用户id，请求头参数
     * @return 路径下的文件
     */
    @GetMapping("/")
    public Result handleRouteRequest(@RequestParam("route") String route,@RequestHeader Optional<String> userId) {

        Result result = routeService.handleRoute(route, userId);
        if(result.getCode()==200){
            return Result.success(result.getData());
        }else{
            return Result.error(result.getCode(), result.getMessage());
        }

    }

}
