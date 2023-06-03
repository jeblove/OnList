package com.jeblove.onList.service;

import com.jeblove.onList.common.Result;
import com.jeblove.onList.entity.Path;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    @Autowired
    @Lazy
    private FileLinkService fileLinkService;

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
     * 删除目录
     * @param username 用户名
     * @param pathId 路径id
     * @param folderName 目录名
     * @param pathList 删除目录路径的列表（不含删除名）
     * @return 删除数
     */
    public long deleteDir(String username, String pathId, String folderName, List<String> pathList){
        String dirPath = handleDir(folderName, pathList);

        // 扫描目录下的文件，更新fileLink
        Map<String, Object> scanFileMap = scanFile(pathId, folderName, pathList);

        Integer count = (Integer) scanFileMap.get("count");

        if(count==-1){
            // 目录异常
            return -1;
        } else if (count==0) {
            // 没有文件
        }else{
            // count为条数
            List<String> fileLinkIdList = (List<String>) scanFileMap.get("fileLinkIdList");
            System.out.println("tt,fileLinkIdList:"+fileLinkIdList);

            for (String fileLinkId : fileLinkIdList) {
                int code = fileLinkService.deleteFileLinkUpdate(fileLinkId, username).getCode();
                if(code != 200){
                    return 0;
                }
            }
        }

        // 执行删除目录
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
        System.out.println("node:"+node);
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

    /**
     * 遍历检测文件记录fileLinkId
     * 递归调用遍历方法，列表的引用（地址）
     * @param node 子节点（内含content）
     * @param fileLinkIdList 记录fileLinkId列表
     */
    private static void traverseFolder(Path.Node node, List<String> fileLinkIdList) {
        Map<String, Path.Node> nodeContent = node.getContent();
        nodeContent.forEach((nodeKey, nodeValue) -> {
            // 文件则记录
            if(nodeValue.getType() == 0) {
                fileLinkIdList.add(nodeValue.getFileLinkId());
            }
            // 文件夹则继续遍历
            else if(nodeValue.getType() == 1) {
                traverseFolder(nodeValue, fileLinkIdList);
            }
        });
    }

    /**
     * 扫描记录目录下指定文件夹下的文件
     * @param pathId 路径path的id
     * @param folderName 目录名
     * @param pathList 目标路径的列表（不包括）
     * @return 目录内文件的fileLinkId列表
     */
    public Map<String, Object> scanFile(String pathId, String folderName, List<String> pathList){
        // 确认删除目录路径，扫描该路径下的文件
        // 删除test，检测test下的content，一层扫描type是否为0，0则addList，
        List<String> fileLinkIdList = new ArrayList<>();
        List<String> newPathList = new ArrayList<>(pathList);

        // 防止传递空字符串[""]列表
        if(newPathList.size()!=0 && newPathList.get(0).equals("")){
            newPathList.remove(0);
        }

        Boolean isRoot;
        // 防止根目录问题
        if(newPathList.isEmpty()){
            System.out.println("根目录添加");
            newPathList.add(folderName);
            isRoot = true;
        } else {
            isRoot = false;
        }

        Path path = findById(pathId);
        // 目标路径
        Path.Node node = getNodeByIdAPath(path, newPathList);

        Map<String, Object> resultMap = new HashMap<>();

        // 目录异常
        if(node==null){
            System.out.println("目录异常");
            resultMap.put("count", -1);
            return resultMap;
        }

        // 目标目录
        Map<String, Path.Node> content = node.getContent();

        // 目录下没有文件
        if(node.getContent().isEmpty()){
            resultMap.put("count", 0);

        }else{
            if(!isRoot){
                content.forEach((key, value) -> {
//                    System.out.println("key:"+key+";"+"value:"+value);
                        // 目标文件夹
                        if(key.equals(folderName) && (value.getSuffix()==null || "".equals(value.getSuffix()))){
                            // 遍历子文件夹，调用方法
                            traverseFolder(value, fileLinkIdList);
                        }
                }); // content forEach

            }else{
                // 根目录，直接开始遍历
                traverseFolder(node, fileLinkIdList);
            }
            resultMap.put("fileLinkIdList", fileLinkIdList);
            resultMap.put("count", fileLinkIdList.size());
        }
        System.out.println("fileLinkIdList:"+fileLinkIdList);
        return resultMap;
    }

    /**
     * 遍历整个根目录检测文件记录fileLinkId
     * 递归调用遍历方法，列表的引用（地址）
     * @param path 路径对象
     * @param fileLinkIdList 记录fileLinkId列表
     */
    private static void traverseRootPath(Path path, List<String> fileLinkIdList) {
        Map<String, Path.Node> content = path.getContent();

        content.forEach((nodeKey, nodeValue) -> {
            // 文件则记录
            if(nodeValue.getType() == 0) {
                fileLinkIdList.add(nodeValue.getFileLinkId());
            }
            // 文件夹则继续遍历
            else if(nodeValue.getType() == 1) {
                traverseFolder(nodeValue, fileLinkIdList);
            }
        });
    }

    /**
     * 删除path文档
     * @param pathId 路径id
     * @param username 所属用户的用户名
     * @return 0则失败，其它成功
     */
    public long removePath(String pathId, String username){
        // 遍历根目录
        Path path = findById(pathId);
        List<String> fileLinkIdList = new ArrayList<>();
        traverseRootPath(path, fileLinkIdList);
        System.out.println(fileLinkIdList);
        // 更新fileLink
        if(fileLinkIdList.size()>1){
            for (String fileLinkId : fileLinkIdList) {
                int code = fileLinkService.deleteFileLinkUpdate(fileLinkId, username).getCode();
                if(code != 200){
                    return 0;
                }
            }
        }
        DeleteResult deleteResult = mongoTemplate.remove(new Query(Criteria.where("_id").is(pathId)), Path.class);
        return deleteResult.getDeletedCount();
    }

    /**
     * 移动and重命名文件or目录
     * @param isMV true则剪切&重命名，false则拷贝
     * @param pathId 路径id
     * @param username 用户名
     * @param filename 文件名/目录名
     * @param pathList 所在目录（不包含）
     * @param newName 剪切/拷贝后新名
     * @param targetPathList 目标目录（不包含）
     * @return 修改条数
     */
    public long copyAMoveFile(Boolean isMV, String pathId, String username, String filename, List<String> pathList, String newName, List<String> targetPathList){
        // 原始名
        Map<String, String> filenameMap = handleSuffix(filename);
        String suffix = filenameMap.get("suffix");
        String filenameWithoutSuffix = filenameMap.get("filenameWithoutSuffix");

        // 新名
        Map<String, String> newFilenameMap = handleSuffix(newName);
        String suffixNew = newFilenameMap.get("suffix");
        String filenameWithoutSuffixNew = newFilenameMap.get("filenameWithoutSuffix");

        if(!filenameWithoutSuffix.equals(filenameWithoutSuffixNew) || !suffix.equals(suffixNew)){
            System.out.println("重命名");
        }

        List<String> newPathList = new ArrayList<>(pathList);

        // 防止根目录问题
        if(newPathList.size()<=1) {
            System.out.println("根目录添加");
            newPathList.add(filenameWithoutSuffix);
        }
        Path path = findById(pathId);
        // 判断相同路径，重命名，路径增加到目标文件
        if(pathList.equals(newPathList)){
            newPathList.add(filenameWithoutSuffix);
        }
        Path.Node node = getNodeByIdAPath(path, newPathList);

        String tarDirPath = handleDir(filenameWithoutSuffixNew, targetPathList);
        System.out.println("PathChange:"+pathList+" -To- "+targetPathList);

        // 目标目录下的文件
        List<String> newTargetPathList = new ArrayList<>(targetPathList);
        // 防止根目录问题
        String originFileLinkId;
        String originFilename;
        System.out.println(newTargetPathList.size());
        System.out.println(newTargetPathList+";"+filenameWithoutSuffixNew);
        Path.Node tarNode = getNodeByIdAPath(path, newTargetPathList);
        if(newTargetPathList.size()<=1) {
            System.out.println("根目录添加");
            newTargetPathList.add(filenameWithoutSuffixNew);
            originFileLinkId = "";
            originFilename = "";
        }else{

//            System.out.println("tarNode:"+tarNode);
            Map<String, Path.Node> contentNode = tarNode.getContent();
            List<String> originFileLinkList = new ArrayList<>();
            contentNode.forEach((nodeKey, nodeValue) -> {
                originFileLinkList.add(nodeValue.getFileLinkId());
                originFileLinkList.add(nodeKey);
            });
            if(originFileLinkList.size()==0){
                originFileLinkId = "";
                originFilename = "";
            }else{
                originFileLinkId = originFileLinkList.get(0);
                originFilename = originFileLinkList.get(1);
                originFileLinkList.clear();
            }

        }

        try {
            // 目录覆盖问题
            if(tarNode.getType()==1){
                System.out.println("扫描删除目标目录");
                deleteDir(username, pathId, filenameWithoutSuffixNew, targetPathList);
            }

            // 原后缀名与新后缀不一致，则修改
            if(!node.getSuffix().equals(suffixNew)){
                node.setSuffix(suffixNew);
                System.out.println("修改后缀名为："+suffixNew);
            }
        }catch (Exception e){
            System.out.println("目标为根目录");
        }

        Query query = new Query(Criteria.where("_id").is(pathId));
        Update update = new Update()
                .set(tarDirPath+".type", node.getType())
                .set(tarDirPath+".suffix", node.getSuffix())
                .set(tarDirPath+".fileLinkId", node.getFileLinkId())
                .set(tarDirPath+".content", node.getContent());

        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Path.class);
        System.out.println(updateResult);

        Boolean isFile = false;

        // 剪切，删除原目录
        // 不需要更新fileLink
        if(isMV){
            System.out.println("剪切");
            if(suffix.length()==0){
                // 目录
//                long deleteDir = deleteDir(username, pathId, filenameWithoutSuffix, pathList);
                // 直接删除源目录
                String dirPath = handleDir(filenameWithoutSuffix, pathList);
                Update updateDir = new Update().unset(dirPath);
                UpdateResult deleteDirResult = mongoTemplate.updateFirst(query, updateDir, Path.class);
                long deleteDir = deleteDirResult.getModifiedCount();

                isFile = false;
            }else{
                // 文件
//                if(filenameWithoutSuffix.equals(filenameWithoutSuffixNew)){
//                     文件名（key）相同，删除原文件

                // 判断两个路径列表是否相等，相等则是重命名，不进行删除操作
//                if(!pathList.equals(targetPathList)){ // 逻辑错误，取消
                    String fileLinkId = deleteFile(pathId, filename, pathList);
                    System.out.println("删除源文件:"+fileLinkId);
                    isFile = true;
//                }

//                }

                // 文件名（不包括后缀）不更改时，忽略
            }
        }else{
            // 拷贝

            // 文件|目录更新fileLink
            if(node.getType()==0){
                // 文件
                String hashCode = fileLinkService.getFileLinkById(node.getFileLinkId()).getHashCode();
                fileLinkService.appendFileLink(hashCode, username);
                isFile = true;
            } else if (node.getType()==1) {
                // 目录
                List<String> fileLinkIdList = new ArrayList<>();
                traverseFolder(node, fileLinkIdList);
                System.out.println(fileLinkIdList);
                for(String id:fileLinkIdList){
                    String hashCode = fileLinkService.getFileLinkById(id).getHashCode();
                    fileLinkService.appendFileLink(hashCode, username);
                }
                isFile = false;
            } // 文件||目录更新fileLink
        }
        // 文件不一致，替换文件后，更新源文件fileLink记录
        if(isFile){
            System.out.println("更新fileLink");
            if(originFilename.equals(filenameWithoutSuffixNew) && suffix.equals(suffixNew) && !node.getFileLinkId().equals(originFileLinkId)){
                fileLinkService.deleteFileLinkUpdate(originFileLinkId, username);
            }
        }

        return updateResult.getModifiedCount();
    }

    /**
     * 根据路径获取指定path信息
     * @param pathId 路径id
     * @param path 路径，字符串，例如"/","/test"
     * @return 统一封装，data中是该路径下的文件信息
     */
    public Result getRoute(String pathId, String path){
        Query query = new Query(Criteria.where("_id").is(pathId));

        if(path.equals("/")){
            // 根目录
            Path result = mongoTemplate.findOne(query, Path.class);
            return Result.success(result.getContent());
        }else{
            String[] arr = path.split("/");

//            排除第一个空字符串元素
            arr = Arrays.copyOfRange(arr, 1, arr.length);
            int len = arr.length;

            String join;
            if(len==1){
                join = "content."+String.join(".", arr);
            }else{
                // 多目录
                join = "content."+String.join(".content.", arr);
            }

            // 确认是否存在
            query.addCriteria(Criteria.where(join).exists(true));
            Path result = mongoTemplate.findOne(query, Path.class);
            if(result==null){
                return null;
            }

            // 获取目标目录（排除content.）
            Path.Node node = getNodeByIdAPath(result, Arrays.asList(arr));
            return Result.success(node.getContent());
        }

    }


}
