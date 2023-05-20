package com.jeblove.onList.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * @author : Jeb
 * @date :2023/5/17 23:42
 * @classname :  Path
 * @description : TODO
 */
//@Data
//@Document(collection = "path")
//public class Path {
//    @Id
//    private String id;
//    private Map<String, Map<String, Object>> content;
//
//}
//@Data
//class PathClass {
//    private int type;
//    private Map<String, String> content;
//}

@Data
@Document(collection = "path")
public class Path {
    @Id
    private String id;
    private Map<String, Node> content;

    @Data
    public static class Node{
        private Integer type;
        private String suffix;
        private String fileLinkId;
        private Map<String, Node> content;
    }
}

