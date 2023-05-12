package com.jeblove.onlyList;

import com.jeblove.onlyList.service.FileService;
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

}
