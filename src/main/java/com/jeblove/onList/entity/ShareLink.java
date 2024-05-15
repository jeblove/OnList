package com.jeblove.onList.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author : Jeb
 * @date :2024/4/10 14:53
 * @classname :  ShareLink
 * @description : TODO
 */
@Data
public class ShareLink {
    private String id;
    private String fileLinkId;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    // 访问次数限制
    private Integer visitLimit;
    // 已访问次数
    private Integer visits;
    // 创建分享用户
    private String username;
    private String downloadUrl;

    // 是否已过期
    public boolean isExpired() {
        boolean timeAfter = LocalDateTime.now().isAfter(expireTime);
        if (timeAfter || visits >= visitLimit){
            return true;
        }else{
            return false;
        }
//        return LocalDateTime.now().isAfter(expireTime);
    }

    // 是否达到访问上限
    public boolean hasReachedVisitLimit() {
        return visits >= visitLimit;
    }

    // 记录访问次数
    public void incrementVisits() {
        visits++;
    }
}
