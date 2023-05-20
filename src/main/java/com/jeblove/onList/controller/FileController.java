package com.jeblove.onList.controller;

import com.jeblove.onList.common.FileCheckHashUtil;
import com.jeblove.onList.common.Result;
import com.jeblove.onList.entity.FileLink;
import com.jeblove.onList.service.FileLinkService;
import com.jeblove.onList.service.FileService;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : Jeb
 * @date :2023/5/11 22:47
 * @classname :  FileController
 * @description : TODO
 */
@RequestMapping("/file/")
@RestController
public class FileController {
    @Autowired
    private FileService fileService;
    @Autowired
    private FileLinkService fileLinkService;

    @Value("${fileLink.hashType}")
    private String hashType;
    @Value("${fileLink.fileLinkPath}")
    private String fileLinkPath;

    /**
     * 获取fs.file文件信息
     * @param name fs.files文件名
     * @return 文件信息
     */
    @RequestMapping("getFileByName")
    public Result getFileByName(String name){
        return fileService.getFileByName(name);
    }

    /**
     * 获取所有fs.files文件信息
     * @return 列表形式的所有文件信息
     */
    @RequestMapping("getFileList")
    public List<GridFSFile> getFileList(){
        return fileService.getFileList();
    }

    /**
     * 文件上传
     * @param uploadFile 文件
     * @param username 上传用户
     * @return data.fileLinkId 文件链接id
     * @throws Exception
     */
    @RequestMapping("upload")
    public Result uploadFile(MultipartFile uploadFile, String username) throws Exception {
        System.out.println(uploadFile.getOriginalFilename()+" 文件大小："+uploadFile.getSize());
        // .getName():uploadFile  getOriginalFilename():文件名.xxx
        String fileLinkId;
        String msg;

        // 检测md5
        String hashCode = FileCheckHashUtil.getHashCode(uploadFile);
        // 已存在检测
        if(!fileLinkService.fileLinkExists(hashType, hashCode)){
            // False: 文件不存在，则上传文件
            Result fileResult = fileService.storeFile(uploadFile, uploadFile.getOriginalFilename());
            String fileId = (String) fileResult.getData();

            System.out.println(fileLinkPath);
            // 添加FileLInk记录
            FileLink fileLink = fileLinkService.insertFileLink(fileId, hashType, hashCode, fileLinkPath, username);
            fileLinkId = fileLink.getId();
            msg = "文件不存在";
        }else{
            // True: 文件存在，查询链接，返回linkId
            FileLink fileLink = fileLinkService.findFileLinkByHashCode(hashCode);
            fileLinkId = fileLink.getId();
            // 添加链接数和用户名
            fileLinkService.appendFileLink(hashCode, username);
            msg = "文件已存在";
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("fileLinkId",fileLinkId);
        map.put("msg",msg);
        return Result.success(map);
    }

    /**
     * 删除链接文件
     * 无链接再删除文件
     * @param fileLinkId 文件链接id
     * @param username 删除文件用户
     * @return data删除条数
     */
    @RequestMapping("deleteFileByFileLinkId")
    public Result deleteFile(String fileLinkId, String username){
        // 先删除用户num
        FileLink fileLink = fileLinkService.getFileLinkById(fileLinkId);
        Map<String, Integer> linkUserMap = fileLink.getLinkUserMap();
        Integer num = linkUserMap.get(username);
        Result result;
        if(num==null){
            // 该用户没有该文件
            result = Result.error(404,"没有该文件");
        }else {
            long count;
            if (num > 1) {
                linkUserMap.put(username, num - 1);
            } else {
                // 该用户链接数为0，删除该用户username
                linkUserMap.remove(username);
            }
            Integer linkNum = fileLink.getLinkNum();
            if (linkNum > 1) {
                fileLink.setLinkNum(linkNum - 1);
                count = fileLinkService.updateFileLinkNum(fileLink.getHashCode(), fileLink.getLinkNum(), linkUserMap);
            } else {
                // 没有其它链接，删除链接文件记录
                count = fileLinkService.deleteFileLink(fileLink.getHashCode());
                // 删除文件
                fileService.deleteFileById(fileLink.getFileId());
            }
            result = Result.success(count);
        }
        return result;
    }

    /**
     * 实际删除file文件
     * @param id fs.file的id
     * @return 返回true则删除成功，返回500代码则失败
     */
    @RequestMapping("deleteFileById")
    public Result deleteFileById(String id){
        return fileService.deleteFileById(id);
    }

    /**
     * 下载文件
     * @param fileLinkId 文件链接id
     * @return 文件流
     * @throws IOException
     */
    @RequestMapping("downloadFileByFileLinkId")
    public ResponseEntity<StreamingResponseBody> downloadFileByFileLinkId(String fileLinkId) throws IOException{
        FileLink fileLink = fileLinkService.getFileLinkById(fileLinkId);
        return fileService.downloadFileById(fileLink.getFileId());
    }

    /**
     * 下载文件
     * @param id fs.file文件id
     * @return 文件流
     * @throws IOException
     */
    @RequestMapping("downloadFileById")
    public ResponseEntity<StreamingResponseBody> downloadFileById(String id) throws IOException {
        return fileService.downloadFileById(id);
    }
}
