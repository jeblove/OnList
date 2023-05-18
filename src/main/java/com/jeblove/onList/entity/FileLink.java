package com.jeblove.onList.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @author : Jeb
 * @date :2023/5/16 17:24
 * @classname :  FileLink
 * @description : TODO
 */
@Data
public class FileLink implements Serializable {
    private String id;
    private String fileId;
    private String hashCode;
    private String hashType;
    private String path;
    private Integer linkNum;
    private List<String> linkUserArray;
}
