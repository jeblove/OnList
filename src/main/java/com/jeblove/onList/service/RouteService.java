package com.jeblove.onList.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeblove.onList.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author : Jeb
 * @date :2023/6/13 23:34
 * @classname :  RouteService
 * @description : TODO
 */
@Service
@Slf4j
public class RouteService {
    @Resource
    private UserService userService;
    @Resource
    private PathService pathService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 根据路径获取指定path信息
     * @param route 路径，例如'/'，‘/test’
     * @param userId 用户id，请求头参数
     * @return 路径下的文件
     * @throws JSONException
     */
    public Result handleRoute(String route, Optional<String> userId) {
        Result result = null;
        JSONObject value = null;

        if(!userId.isPresent()){
            return Result.error(500, "缺少userId");
        }
        log.debug("用户{}请求路径{}", userId, route);
        String pathId = userService.getUser(userId.get()).getPathId();

        // redis 构造键值
        String cacheKey = userId.get();

        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String cacheResult = ops.get(cacheKey);

        ObjectMapper objectMapper = new ObjectMapper();
        // 判断是否存在缓存数据
        if (cacheResult != null) {
            log.debug("从缓存获取结果: {}", cacheResult);

            JSONObject directory = null;
            try {
                directory = new JSONObject(cacheResult);
            } catch (JSONException e) {
                log.error("json出错");
            }

            value = directory;

            String[] keys = route.split("/");

            for (String key : keys) {
                if(!key.isEmpty()){
                    value = value.optJSONObject(key);
                    value = value.optJSONObject("content");
                }
            }

            try {
                Object data = objectMapper.readValue(value.toString(), Object.class);
                return Result.success(data);
            }catch (Exception e){
                log.error("处理route异常");
            }

            return Result.success(value);

        }else{
            log.debug("cacheKey: {}不在缓存中", cacheKey);
            updateRedisValue(userId.get());
            return handleRoute(route, userId);
        }

    }

    /**
     * 更新redis缓存
     * @param userId 用户id
     */
    public void updateRedisValue(String userId){
        log.debug("更新redis");
        String pathId = userService.getUser(userId).getPathId();
        redisTemplate.delete(pathId);
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        Result pathResult = pathService.getRoute(pathId, "/");
        // "用户目录："+pathResult.getData());

        ObjectMapper objectMapper = new ObjectMapper();
        String cacheValue = null;
        try {
            // 将 result.getData() 对象序列化成 JSON 字符串
            cacheValue = objectMapper.writeValueAsString(pathResult.getData());
        } catch (JsonProcessingException e) {
            // 异常处理...
            log.error("更新redis缓存异常");
        }

        if (cacheValue != null) {
            ops.set(userId, cacheValue, 5, TimeUnit.MINUTES);
        }

    }

    /**
     * 清空redis缓存
     */
    public void flushRedisDatabase(){
        redisTemplate.execute((RedisConnection connection) -> {
            connection.flushDb();
            return "Flushed DB";
        });
        log.info("清空redis");
    }
}
