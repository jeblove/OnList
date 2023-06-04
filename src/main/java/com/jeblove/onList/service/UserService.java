package com.jeblove.onList.service;

import com.jeblove.onList.common.JwtUtils;
import com.jeblove.onList.common.Result;
import com.jeblove.onList.common.SecurityConfig;
import com.jeblove.onList.entity.FileLink;
import com.jeblove.onList.entity.Path;
import com.jeblove.onList.entity.User;
import com.mongodb.client.result.DeleteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    @Autowired
    @Lazy
    private FileLinkService fileLinkService;
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;

    @Value("${fileLink.init.filename}")
    private String initFilename;
    @Value("${fileLink.init.fileLinkId}")
    private String initFileLinkId;

    /**
     * 根据用户id获取用户信息
     * @param id 用户id
     * @return 用户信息
     */
    public User getUser(String id){
        return mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)), User.class);
    }

    /**
     * 根据用户名获取用户id
     * @param username 用户名
     * @return 用户id
     */
    public String getUserIdByUsername(String username){
        return mongoTemplate.findOne(new Query(Criteria.where("username").is(username)), User.class).getId();
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

    /**
     * 安全登录
     * api
     * @param username 用户名
     * @param password 密码
     * @return token，userId
     */
    public Result loginSecurity(String username, String password){
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails.getUsername(), password);
        try {
            authenticationManager.authenticate(authenticationToken);
            System.out.println("用户："+userDetails.getUsername()+"，登录成功");

            String token = jwtUtils.generateToken(userDetails);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("token", token);
            resultMap.put("userId", getUserIdByUsername(userDetails.getUsername()));

            return Result.success(resultMap);
        }catch (AuthenticationException e){
            return Result.error(502, "登录失败："+e.getMessage());
        }

    }

    /**
     * 用户注册
     * api
     * @param user 用户信息
     * @return 成功条数
     * @throws Exception
     */
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

        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 更新fileLink
        FileLink fileLink = fileLinkService.getFileLinkById(initFileLinkId);
        fileLinkService.appendFileLink(fileLink.getHashCode(), user.getUsername());

        // 权限
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("disabled",0);
        user.setPermissions(permissions);

        User insert = mongoTemplate.insert(user);
        System.out.println(insert);
        return insert.toString();
    }

    /**
     * 删除用户
     * @param id 用户id
     * @param password 用户密码
     * @return code：成功则200，失败则502
     */
    public Result deleteUser(String id, String password){
        Result result = Result.error(502, "ID或密码错误");
        User user = getUser(id);
        if(user==null){
            return result;
        }
        Result login = login(user.getUsername(), password);
        if(login.getCode() == 200){
            DeleteResult deleteResult = mongoTemplate.remove(new Query(Criteria.where("_id").is(id)), User.class);
            long deletedCount = deleteResult.getDeletedCount();
            if(deletedCount>0){
                // 删除用户目录
                long removePath = pathService.removePath(user.getPathId(), user.getUsername());
                if(removePath!=0){
                    result = Result.success(deletedCount);
                }
            }

        }else{
            result = login;
        }
        return result;

    }

}
