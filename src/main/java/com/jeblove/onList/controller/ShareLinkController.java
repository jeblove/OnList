package com.jeblove.onList.controller;

import com.jeblove.onList.common.Result;
import com.jeblove.onList.entity.ShareLink;
import com.jeblove.onList.service.ShareLinkService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author : Jeb
 * @date :2024/4/10 15:20
 * @classname :  ShareLinkController
 * @description : TODO
 */
@RequestMapping("/share/")
@RestController
public class ShareLinkController {
    @Value("${app.share.downloadPrefix}")
    private String downloadPrefix;
    @Resource
    private ShareLinkService shareLinkService;

    @RequestMapping("createShareLink")
    public Result createShareLink(String fileLinkId, Integer expireDuration, Integer visitLimit, HttpServletRequest request) {
        String username = request.getHeader("username");
        ShareLink shareLink = shareLinkService.createShareLink(username, fileLinkId, expireDuration, visitLimit);

        return Result.success(shareLink);

    }

    @GetMapping("/{prefix}/{shareLinkId}")
    public ResponseEntity<StreamingResponseBody> downloadFileByShareLinkId(@PathVariable String shareLinkId, @PathVariable String prefix) throws IOException {
        // 验证前缀是否正确
        if (!prefix.equals(downloadPrefix)) {
            return ResponseEntity.notFound().build();
        }

        // 将下载处理逻辑委托给ShareLinkService
        ResponseEntity<StreamingResponseBody> downloadResponse = shareLinkService.processDownloadByShareLinkId(shareLinkId);
        return downloadResponse;
    }


}
