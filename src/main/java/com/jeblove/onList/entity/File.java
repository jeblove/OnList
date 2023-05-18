package com.jeblove.onList.entity;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


import java.io.Serializable;
import java.util.Date;

/**
 * @author : Jeb
 * @date :2023/5/11 23:31
 * @classname :  File
 * @description : TODO
 */
//@Data
@Document(collation = "fs.files")
public class File implements Serializable {
//    @Id
    @Indexed(unique = true)
    @Field("id")
    private String id;
    @Field("length")
    private long length;
    @Field("chunkSize")
    private Integer chunkSize;
    @Field("uploadDate")
    private Date uploadDate;
    @Field("filename")
    private String filename;
    @Field("metadata")
    private String metadata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public Integer getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "File{" +
                "id='" + id + '\'' +
                ", length=" + length +
                ", chunkSize=" + chunkSize +
                ", uploadDate=" + uploadDate +
                ", filename='" + filename + '\'' +
                ", metadata='" + metadata + '\'' +
                '}';
    }
}
