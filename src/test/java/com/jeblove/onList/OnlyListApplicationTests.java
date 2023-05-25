package com.jeblove.onList;

import com.jeblove.onList.entity.Path;
import com.jeblove.onList.service.*;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

//    // 获取当前用户文件目录
//    @Test
//    public void GetCurrentUser(){
//        String userId = "64634275a7e5269494c3bc98";
//        User user = userService.getUser(userId);
//        List<VirtualFolder> list = virtualFolderService.getFolderByOwner(userId);
////        System.out.println(list);
//        String HomePath = "/homes";
//        for(VirtualFolder folders:list){
//            System.out.println(folders);
//            //
//            String index = folders.getIndex();
//            if(index.equals(HomePath)){
//                // 该目录是用户目录
//                String currentPath = HomePath+"/"+folders.getName();
//                List<String> folderList = (List<String>) folders.getContent().get("folder");
//                List<String> fileList = (List<String>) folders.getContent().get("file");
//                System.out.println(currentPath);
//                System.out.println(folderList);
//                System.out.println(fileList);
//            }
//        }
//    }

    @Test
    public void TestPath(){
        System.out.println(pathService.findById("6465002a035c9325f932d224"));

    }

    @Test
    public void findMd5(){
        System.out.println(fileLinkService.fileLinkExists("md5","1e6ac294a3e83a404012edf70bccc1706"));
    }

    @Test
    public void createFolder(){
        List<String> list = Arrays.asList("");
        System.out.println(list);
        pathService.createDir("6468f3ab80aec42ffca256dc","testDir", list);
    }

    @Test
    public void deleteFolder(){
        List<String> list = Arrays.asList("");
        pathService.deleteDir("hans2","6468f3ab80aec42ffca256dc", "rDir", list);
    }

    @Test
    public void getUserById(){
        System.out.println(userService.getUser("6468f3ab80aec42ffca256dd"));
    }

    @Test
    public void addFileTest(){
        List<String> list = Arrays.asList("rDir");
        pathService.addFileToPath("6468f3ab80aec42ffca256dc", "test2.txt", "64687b34528db6326f81d406", list);
    }

    @Test
    public void deleteFile(){
        List<String> list = Arrays.asList("rDir","subDir");
//        List<String> list = Arrays.asList("rDir","subDir");

//        System.out.println(pathService.getTypeValue(list, "6468f3ab80aec42ffca256dc"));

        String s = pathService.deleteFile("6468f3ab80aec42ffca256dc", "test.txt", list);
        System.out.println(s);
    }

    @Test
    public void scanFileTest(){
        String folderName = "test12";
        List<String> list = Arrays.asList("ta1");
        pathService.scanFile("6468f3ab80aec42ffca256dc", folderName, list);

        Map<String, Object> scanFileMap = pathService.scanFile("6468f3ab80aec42ffca256dc", folderName, list);
//        System.out.println(scanFileMap);
    }

    @Test
    public void scanFile(){
        pathService.removePath("6468f3ab80aec42ffca256dc","hans2");
    }

    @Test
    public void copyPath(){
        List<String> list = Arrays.asList("");
        List<String> list2 = Arrays.asList("tar");
        pathService.copyAMoveFile(false, "646e1eba859e82211d3bc5c1","hans2" ,"test", list,"t4", list2);
    }

    @Test
    public void copyFile(){
        List<String> list = Arrays.asList("");
        List<String> list2 = Arrays.asList("");
        pathService.copyAMoveFile(false,"646e1eba859e82211d3bc5c1", "hans2","readme.txt", list,"readme4ta.md", list2);
    }

    @Test
    public void movePath(){
        List<String> list = Arrays.asList("");
        List<String> list2 = Arrays.asList("");
        pathService.copyAMoveFile(true, "646e1eba859e82211d3bc5c1","hans2" ,"testNa", list,"test", list2);
    }

    @Test
    public void moveFile(){
        List<String> list = Arrays.asList("");
        List<String> list2 = Arrays.asList("test");
        pathService.copyAMoveFile(true,"646e1eba859e82211d3bc5c1", "hans2","readme.txt", list,"readme2.txt", list2);
    }


}
