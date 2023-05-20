package com.jeblove.onList.service;

import com.jeblove.onList.common.Result;
import com.jeblove.onList.entity.Path;
import com.jeblove.onList.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : Jeb
 * @date :2023/5/16 20:41
 * @classname :  UserService
 * @description : TODO
 */
@Service
public class UserService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private PathService pathService;

    @Value("${fileLink.init.filename}")
    private String initFilename;
    @Value("${fileLink.init.fileLinkId}")
    private String initFileLinkId;

    /**
     * 获取用户信息
     * @param id 用户id
     * @return 用户信息
     */
    public User getUser(String id){
        return mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)), User.class);
    }

    public Result login(String username, String password){
        User user = mongoTemplate.findOne(new Query(Criteria.where("username").is(username)), User.class);
        Result result;
        if(username.equals(user.getUsername()) && password.equals(user.getPassword())){
            // 验证通过
            result = Result.success("token");
        }else{
            result = Result.error(502,"用户或密码错误");
        }
        return result;
    }

    public String register(User user) throws Exception{
        // 待判断
        // 时间
//        LocalDateTime currentTime = LocalDateTime.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        String formattedTime = currentTime.format(formatter);
//        user.setSignUpTime(formattedTime);

        Date date = Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC));
        user.setSignUpTime(date);

        // 用户home目录，并添加初始文件
        Path path = pathService.insertPath(initFilename, initFileLinkId);
        user.setPathId(path.getId());

        // 权限
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("disabled",0);
        user.setPermissions(permissions);

        User insert = mongoTemplate.insert(user);
        System.out.println(insert);
        return insert.toString();
    }

}
