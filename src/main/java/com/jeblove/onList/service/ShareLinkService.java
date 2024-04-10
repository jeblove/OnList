package com.jeblove.onList.service;

import com.jeblove.onList.entity.FileLink;
import com.jeblove.onList.entity.ShareLink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @author : Jeb
 * @date :2024/4/10 14:59
 * @classname :  ShareLinkService
 * @description : TODO
 */
@Service
@Slf4j
public class ShareLinkService {
    @Value("${app.share.downloadPrefix}")
    private String downloadPrefix;
    @Resource
    private FileLinkService fileLinkService;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private FileService fileService;

    /**
     * 创建分享链接
     * @param username 用户名
     * @param fileLinkId fileLink的id
     * @param expireDuration 小时，x小时后过期
     * @param visitLimit 可访问次数
     * @return ShareLink信息
     */
    public ShareLink createShareLink(String username, String fileLinkId, Integer expireDuration, Integer visitLimit) {
        ShareLink shareLink = new ShareLink();
        shareLink.setFileLinkId(fileLinkId);
        LocalDateTime localDateTime = LocalDateTime.now();
        shareLink.setCreateTime(localDateTime);
        LocalDateTime expireTime = localDateTime.plusHours(expireDuration);
        shareLink.setExpireTime(expireTime);
        shareLink.setVisitLimit(visitLimit);
        shareLink.setVisits(0);
        shareLink.setUsername(username);
        ShareLink insert = mongoTemplate.insert(shareLink);
        insert.setDownloadUrl("/"+downloadPrefix+"/" + insert.getId());
        // 让数据库自动生成id确认下载链接，再重新写入
        ShareLink save = mongoTemplate.save(insert);
        log.info("用户{}分享了{}文件", username, fileLinkId);
        return save;
    }

    /**
     * 根据shareLinkId查询ShareLink
     * @param shareLinkId ShareLink的id
     * @return ShareLink
     */
    public ShareLink getShareLinkById(String shareLinkId) {
        return mongoTemplate.findById(shareLinkId, ShareLink.class);
    }

    /**
     * 更新ShareLink
     * @param updatedShareLink ShareLink
     */
    public void updateShareLink(ShareLink updatedShareLink) {
        mongoTemplate.save(updatedShareLink);
    }

    /**
     * 处理分享文件下载链接
     * @param shareLinkId shareLink的id
     * @return 文件的字节流
     * @throws IOException
     */
    public ResponseEntity<StreamingResponseBody> processDownloadByShareLinkId(String shareLinkId) throws IOException {
        ShareLink shareLink = getShareLinkById(shareLinkId);
        // 检测文件是否符合分享
        if (shareLink == null || shareLink.isExpired() || shareLink.hasReachedVisitLimit()) {
            return ResponseEntity.notFound().build();
        }
        // 更新访问次数
        shareLink.incrementVisits();
        updateShareLink(shareLink);
        // 以fileLinkId获取fileLink信息
        String fileLinkId = shareLink.getFileLinkId();
        FileLink fileLink = fileLinkService.getFileLinkById(fileLinkId);
        return fileService.downloadFileById(fileLink.getFileId());
    }
}
