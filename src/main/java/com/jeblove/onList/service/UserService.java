package com.jeblove.onList.service;

import com.jeblove.onList.common.JwtUtils;
import com.jeblove.onList.common.Result;
import com.jeblove.onList.entity.FileLink;
import com.jeblove.onList.entity.Path;
import com.jeblove.onList.entity.User;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : Jeb
 * @date :2023/5/16 20:41
 * @classname :  UserService
 * @description : TODO
 */
@Service
@Slf4j
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
        User resultId = mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)), User.class);
        log.debug("根据用户id: {}获取用户信息{}", id, resultId);
        return resultId;
    }

    /**
     * 根据用户名获取用户id
     * @param username 用户名
     * @return 用户id
     */
    public String getUserIdByUsername(String username){
        String id = mongoTemplate.findOne(new Query(Criteria.where("username").is(username)), User.class).getId();
        log.debug("根据用户名{}获取用户id: {}", username, id);
        return id;
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

            String token = jwtUtils.generateToken(userDetails);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("token", token);
            resultMap.put("userId", getUserIdByUsername(userDetails.getUsername()));
//            List<User> users = mongoTemplate.findAll(User.class);
//            System.out.println(users);
            log.info("用户{}登录成功", userDetails.getUsername());
            return Result.success(resultMap);
        }catch (AuthenticationException e){
            log.warn("用户{}登录失败", userDetails.getUsername());
            return Result.error(502, "登录失败："+e.getMessage());
        }
    }

    /**
     * 用户注册
     * api
     *
     * @param user 用户信息
     * @return 成功条数
     * @throws Exception
     */
    public Result register(User user) throws Exception{
        // 待判断
        Result emptyError = Result.error(500, "信息不能为空！");

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return emptyError;
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return emptyError;
        }

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            String regexEmail = ".+@.+\\..+";
            if (!user.getEmail().matches(regexEmail)) {
                return Result.error(500, "邮箱信息异常！");
            }
        }
//        else {
//            return emptyError;
//        }

        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            String regexPhoneNum = "\\d{11}";
            if (!user.getPhone().matches(regexPhoneNum)) {
                return Result.error(500, "手机号位数不正确！");
            }
        }
//        else {
//            return emptyError;
//        }

        // 时间
//        LocalDateTime currentTime = LocalDateTime.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        String formattedTime = currentTime.format(formatter);
//        user.setSignUpTime(formattedTime);

//        Date date = Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC));
        Date date = new Date();
        user.setSignUpTime(date);

        String getPathId = user.getPathId();
        String pathId;
        if (getPathId !=null && !ObjectUtils.isEmpty(getPathId)) {
            pathId = getPathId;
        }else{
            // 用户home目录，并添加初始文件
            Path path = pathService.insertPath(initFilename, initFileLinkId);
            pathId = path.getId();
        }
        user.setPathId(pathId);

        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 更新fileLink
        FileLink fileLink = fileLinkService.getFileLinkById(initFileLinkId);
        fileLinkService.appendFileLink(fileLink.getHashCode(), user.getUsername());

        Map<String, Object> permissions = user.getPermissions();
        // 权限
        Map<String, Object> newPermissions = new HashMap<>();
        if (permissions!=null && !permissions.isEmpty()) {
            if (permissions.containsKey("role")){
                Integer newRole = Integer.parseInt(permissions.get("role").toString());
                newPermissions.put("role", newRole);
            }

            if (permissions.containsKey("disabled")){
                Integer newDisabled = Integer.parseInt(permissions.get("disabled").toString());
                newPermissions.put("disabled", newDisabled);
            }
        }else{
            // 0为启用
            newPermissions.put("disabled",0);
            // role=0为管理员，1为普通用户
            newPermissions.put("role",1);
        }
        user.setPermissions(newPermissions);


        User insert = mongoTemplate.insert(user);
//        System.out.println(insert);
        log.info("用户{}注册: {}", user.getUsername(), insert);
        return Result.success(insert);
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
            boolean execDeleteUser = execDeleteUser(user);
            if (execDeleteUser) {
                result = Result.success("删除用户成功");
            }
        }else{
//            log.info("删除用户{}失败", id);
            result = login;
        }
        return result;
    }

    /**
     * 修改user [api]
     * 管理员可修改username、password、pathId、permissions.role，需要参数user中包含目标id
     * 用户仅能修改自己username、password
     * @param loginId 登录userId
     * @param user 修改参数
     * @return code200成功条数， 502失败
     */
    public Result modifyUser(String loginId, User user){
        User loginUser = mongoTemplate.findOne(new Query(Criteria.where("_id").is(loginId)), User.class);

        String userId;
        Map<String, Object> loginUserPermissions = loginUser.getPermissions();
        if (loginUserPermissions == null) {
            return Result.error(502, "用户错误");
        }
        if ((Integer)loginUserPermissions.get("role") == 0){
            Query query;
            Update update = new Update();

            Integer loginUserRole = (Integer) loginUser.getPermissions().get("role");
            if (loginUserRole == 0) {
                // 管理员，可修改pathId、role
                String newPathId = user.getPathId();

                // 是否修改permissions
                Map<String, Object> permissions = user.getPermissions();
                if (permissions!=null && !permissions.isEmpty()) {
                    if (permissions.containsKey("role")){
                        Integer newRole = Integer.parseInt(permissions.get("role").toString());
                        update.set("permissions.role", newRole);
                    }

                    if (permissions.containsKey("disabled")){
                        Integer newDisabled = Integer.parseInt(permissions.get("disabled").toString());
                        update.set("permissions.disabled", newDisabled);
                    }
                }
                if (newPathId !=null && !ObjectUtils.isEmpty(newPathId)) {
                    update.set("pathId", newPathId);
                }

                // 管理员账号修改的是根据参数user中的id
                if (user.getId() != null) {
                    userId = user.getId();
                }else{
                    userId = loginUser.getId();
                }
            }else{
                if (loginUser.getId().equals(user.getId())){
                    // 非管理员只能修改自己账号
                    userId = loginUser.getId();
                }else{
                    return Result.error(502, "普通用户，仅能修改自己账号");
                }
            }
            query = new Query(Criteria.where("_id").is(userId));
            String newUsername = user.getUsername();
            String newPassword = user.getPassword();
            if (!ObjectUtils.isEmpty(newUsername)){
                update.set("username", newUsername);
            }
            if (!ObjectUtils.isEmpty(newPassword)) {
                update.set("password", passwordEncoder.encode(newPassword));
            }

            Document updateObject = update.getUpdateObject();
            if (updateObject.isEmpty()){
                log.warn("修改参数为空");
                return Result.error(502, "修改参数为空");
            }
            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
            log.info("修改用户{}成功", userId);
            return Result.success(updateResult.getModifiedCount());
        }else{
            log.warn("用户或密码错误，修改用户失败");
            return Result.error(502, "用户或密码错误");
        }
    }

    /**
     * 获取用户列表 [api]
     * @return List<User>
     */
    public List<User> getAllUserInfo(){
        List<User> users = mongoTemplate.findAll(User.class);
        log.debug("获取用户列表: {}", users);
        return users;
    }

    /**
     * 执行删除用户
     * @param user 用户实体(需要含有id和pathId)
     * @return boolean
     */
    public boolean execDeleteUser(User user){
        DeleteResult deleteResult = mongoTemplate.remove(new Query(Criteria.where("_id").is(user.getId())), User.class);
        long deletedCount = deleteResult.getDeletedCount();
        if(deletedCount>0){
            log.info("删除用户{}成功", user.getUsername());
            // 删除用户目录
            long removePath = pathService.removePath(user.getPathId(), user.getUsername());
            if(removePath!=0){
                log.info("删除用户{}目录成功", user.getUsername());
                return true;
            }
        }
        log.info("删除用户{}失败", user.getUsername());
        return false;
    }

    /**
     * 管理员删除用户
     * @param loginId 管理员userId
     * @param userId 删除目标userId
     * @return boolean
     */
    public boolean adminDeleteUser(String loginId, String userId){
        User loginUser = mongoTemplate.findOne(new Query(Criteria.where("_id").is(loginId)), User.class);

        Map<String, Object> loginUserPermissions = loginUser.getPermissions();
        if (loginUserPermissions == null) {
            return false;
        }
        if ((Integer)loginUserPermissions.get("role") == 0){
            // 管理员
            User user = mongoTemplate.findOne(new Query(Criteria.where("_id").is(userId)), User.class);
            return execDeleteUser(user);
        }
        return false;
    }

    /**
     * 检测该用户是否管理员
     * @param userId 用户ID
     * @return boolean
     */
    public boolean isAdmin(String userId){
        User user = mongoTemplate.findOne(new Query(Criteria.where("_id").is(userId)), User.class);
        Map<String, Object> userPermissions = user.getPermissions();
        if (userPermissions == null) {
            return false;
        }
        if ((Integer)userPermissions.get("role") == 0){
            return true;
        }
        return false;
    }
}
