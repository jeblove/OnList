package com.jeblove.onList.service;

import com.jeblove.onList.common.Result;
import com.jeblove.onList.entity.Path;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * @author : Jeb
 * @date :2023/5/17 23:53
 * @classname :  PathService
 * @description : TODO
 */
@Service
public class PathService {
    @Autowired
    private MongoTemplate mongoTemplate;

    public Path findById(String id){
        Query query = new Query(Criteria.where("_id").is(id));
        return mongoTemplate.findOne(query, Path.class);
    }

    /**
     * 处理文件后最
     * @param filename 文件名（包括后缀）例如：readme.txt
     * @return 键值对，suffix：文件后缀，filenameWithoutSuffix：无后缀文件名
     */
    public Map<String, String> handleSuffix(String filename){
        Integer lastIndexOfDot = filename.lastIndexOf('.');

        String suffix;
        String filenameWithoutSuffix;
        if(lastIndexOfDot==-1){
            // 无文件后缀（没有找到点号）
            suffix = "";
            filenameWithoutSuffix = filename;
        }else{
            suffix = filename.substring(lastIndexOfDot+1);
            filenameWithoutSuffix = filename.substring(0, lastIndexOfDot);
        }
        Map<String, String> map = new HashMap<>();
        map.put("suffix",suffix);
        map.put("filenameWithoutSuffix",filenameWithoutSuffix);
        return map;
    }

    /**
     * 初始化用户目录
     * @param filename 初始化文件名
     * @param fileLinkId 初始化文件链接id
     * @return 文件信息
     */
    public Path insertPath(String filename, String fileLinkId) {

        Map<String, String> filenameMap = handleSuffix(filename);
        String suffix = filenameMap.get("suffix");
        String filenameWithoutSuffix = filenameMap.get("filenameWithoutSuffix");

        Path.Node node = new Path.Node();
        node.setType(0);
        node.setSuffix(suffix);
        node.setFileLinkId(fileLinkId);
        Map<String, Path.Node> map = new HashMap<>();
        node.setContent(map);

        Path path = new Path();
        Map<String, Path.Node> content = new HashMap<>();
        content.put(filenameWithoutSuffix, node);
        path.setContent(content);

        return mongoTemplate.insert(path);
    }

    /**
     * 点目录处理
     * @param folderName 文件夹名或文件名
     * @param pathList 所在的目录
     * @return 用.连接起来的字符串目录
     */
    public String handleDir(String folderName, List<String> pathList){
        List<String> list = new ArrayList<>(pathList);
        // if判断是否为根目录
        if(list.isEmpty()){
            System.out.println("/");
        }else{
            // 防止传递空字符串[""]列表
            if(list.get(0).equals("")){
                list.remove(0);
            }
            for(int i=0; i<list.size(); i++){
                // 下标偶数
                if(i%2==0){
                    list.add(i, "content");
                }
            }
        }
        list.add("content");
        list.add(folderName);

        // 文件夹
        String dirPath = String.join(".", list);
        System.out.println(dirPath);
        return dirPath;
    }

    /**
     * 创建文件
     * 面向其它服务
     * @param filename 文件名
     * @param pathList 所在路径（不包含）
     * @param pathId 路径id
     * @param type 类型：0为文件，1为文件夹
     * @param suffix 文件后缀，type=0时需要，1时忽略
     * @param fileLinkId 文件链接id，type=0时需要，1时忽略
     * @return 修改条数
     */
    private long createFile(String filename, List<String> pathList, String pathId, Integer type, String suffix, String fileLinkId){
        String dirPath = handleDir(filename, pathList);

        Query query = new Query(Criteria.where("_id").is(pathId));
        // 属性
        String typeKey = dirPath+".type";
        String suffixKey = dirPath+".suffix";
        String fileLinkIdKey = dirPath+".fileLinkId";
        String contentKey = dirPath+".content";

        Update update = new Update()
                .set(typeKey, type)
                .set(suffixKey, suffix)
                .set(fileLinkIdKey, fileLinkId)
                .set(contentKey, new HashMap<>());

        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Path.class);
        System.out.println(updateResult);
        return updateResult.getModifiedCount();
    }

    /**
     * 创建文件夹
     * @param pathId 路径id
     * @param folderName 文件夹名
     * @param pathList 创建目录路径的列表（不含创建名）
     * @return 修改数
     */
    public Result createDir(String pathId, String folderName, List<String> pathList){
        Result result;
        // 判断是否跨文件夹
        Path path = mongoTemplate.findOne(new Query(Criteria.where("_id").is(pathId)), Path.class);
        // 排除根目录
        if(pathList.size()!=0){
            // 非空目录，则检测父目录是否正常
            if(getNodeByIdAPath(path, pathList)==null){
                System.out.println("pathService,createDir");
                return Result.error(500, "父目录异常");
            }
        }

        long modifiedCount = createFile(folderName, pathList, pathId, 1, "", "");
        result = Result.success(modifiedCount);

        return result;
    }

    /**
     * 删除文件夹
     * @param pathId 路径id
     * @param folderName 文件夹名
     * @param pathList 删除目录路径的列表（不含删除名）
     * @return 删除数
     */
    public long deleteDir(String pathId, String folderName, List<String> pathList){
        String dirPath = handleDir(folderName, pathList);

        Query query = new Query(Criteria.where("_id").is(pathId));
        Update update = new Update().unset(dirPath);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Path.class);
        System.out.println(updateResult);
        return updateResult.getModifiedCount();
    }

    /**
     * 添加链接文件到指定目录
     * @param pathId 路径id
     * @param filename 文件名（包含后缀）
     * @param fileLinkId 文件链接id
     * @param pathList 所在路径（不包含）
     * @return 修改条数
     */
    public long addFileToPath(String pathId, String filename, String fileLinkId, List<String> pathList){
        Map<String, String> filenameMap = handleSuffix(filename);
        String suffix = filenameMap.get("suffix");
        String filenameWithoutSuffix = filenameMap.get("filenameWithoutSuffix");

        long modifiedCount = createFile(filenameWithoutSuffix, pathList, pathId, 0, suffix, fileLinkId);
        return modifiedCount;
    }

    /**
     * 获取Path子节点信息
     * @param path path对象
     * @param pathList 目标路径的列表（包括目标）
     * @return 目标子节点的Path信息
     */
    public Path.Node getNodeByIdAPath(Path path, List<String> pathList) {
        // 防止传递空字符串[""]列表
        if(pathList.size()!=0){
            if(pathList.get(0).equals("")){
                pathList.remove(0);
            }
        }
        Boolean isNull = false;

        if (path == null) {
            // 处理找不到document的情况
            isNull = true;
        }
        Map<String, Path.Node> content = path.getContent();

        Path.Node node = null;
        for (String key : pathList) {
            // 如果遇到空节点，则返回默认值或者抛出异常
            if (!content.containsKey(key)) {
                System.out.println("没有这个key:"+key);
                isNull = true;
                break;
            }
            // 按路径获取 Node 对象
            node = content.get(key);
            // 获取子Node集合以便继续遍历
            content = node.getContent();
        }
        if(isNull){
            node = null;
        }
        System.out.println(node);
        return node;
    }

    /**
     * 删除path中的文件（非文件夹）
     * @param pathId 路径path的id
     * @param filename 删除文件名
     * @param pathList 删除文件所在路径（不包含）
     * @return 该文件的fileLinkId
     */
    public String deleteFile(String pathId, String filename, List<String> pathList){
        String result;
        Map<String, String> filenameMap = handleSuffix(filename);
        String suffix = filenameMap.get("suffix");
        String filenameWithoutSuffix = filenameMap.get("filenameWithoutSuffix");

        List<String> newPathList = new ArrayList<>(pathList);
        newPathList.add(filenameWithoutSuffix);

        Query query = new Query(Criteria.where("_id").is(pathId));
        Path path = mongoTemplate.findOne(query, Path.class);
        Path.Node node = getNodeByIdAPath(path, newPathList);
        if(node==null){
            return null;
        }

        // 不是文件夹
        if(node.getSuffix().length()!=0){

            if(node.getSuffix().equals(suffix)){
                System.out.println("文件无误");
            }

            String fileLinkId = node.getFileLinkId();

            String dirPath = handleDir(filenameWithoutSuffix, pathList);
            Update update = new Update().unset(dirPath);
            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Path.class);

            // 确认删除，返回fileLinkId
            if(updateResult.getModifiedCount()>0){
                result = fileLinkId;
            }else{
                result = null;
            }
        }else{
            result = null;
        }
        return result;
    }

}
