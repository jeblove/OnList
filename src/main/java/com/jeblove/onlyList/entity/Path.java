package com.jeblove.onlyList.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Map;

/**
 * @author : Jeb
 * @date :2023/5/17 23:42
 * @classname :  Path
 * @description : TODO
 */
@Data
public class Path {
    @Id
    private String id;
    private Map<String, Map<String, Object>> content;

}
//@Data
//class PathClass {
//    private int type;
//    private Map<String, String> content;
//}

