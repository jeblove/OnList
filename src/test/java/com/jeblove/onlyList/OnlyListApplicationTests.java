package com.jeblove.onlyList;

import com.jeblove.onlyList.entity.User;
import com.jeblove.onlyList.entity.VirtualFolder;
import com.jeblove.onlyList.service.*;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class OnlyListApplicationTests {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private FileService fileService;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private FileLinkService fileLinkService;

    @Autowired
    private UserService userService;

    @Autowired
    private VirtualFolderService virtualFolderService;

    @Autowired
    private PathService pathService;


    @Test
    void contextLoads() {
    }

    @Test
    public void findId(){

        // 正常查询
//        GridFSFile gridFSFile = fileService.getFileByName("Truenas_docker.mp4");
//        System.out.println(gridFSFile);

//        GridFsResource[] txtFiles = gridFsTemplate.getResources("*");
//        for(GridFsResource txtFile : txtFiles){
//            System.out.println(txtFile.getFilename());
//        }

        List<GridFSFile> fileList = new ArrayList<>();
        gridFsTemplate.find(new Query()).into(fileList);
        System.out.println(fileList);


    }

    @Test
    public void FileLink(){
        System.out.println(fileLinkService.findAll().toString());
    }

    // 获取当前用户文件目录
    @Test
    public void GetCurrentUser(){
        String userId = "64634275a7e5269494c3bc98";
        User user = userService.getUser(userId);
        List<VirtualFolder> list = virtualFolderService.getFolderByOwner(userId);
//        System.out.println(list);
        String HomePath = "/homes";
        for(VirtualFolder folders:list){
            System.out.println(folders);
            //
            String index = folders.getIndex();
            if(index.equals(HomePath)){
                // 该目录是用户目录
                String currentPath = HomePath+"/"+folders.getName();
                List<String> folderList = (List<String>) folders.getContent().get("folder");
                List<String> fileList = (List<String>) folders.getContent().get("file");
                System.out.println(currentPath);
                System.out.println(folderList);
                System.out.println(fileList);
            }
        }
    }

    @Test
    public void TestPath(){
        System.out.println(pathService.findById("6465002a035c9325f932d224"));

    }

}
