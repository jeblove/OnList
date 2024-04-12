package com.jeblove.onList.controller;

import com.jeblove.onList.common.Result;
import com.jeblove.onList.service.BRService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author : Jeb
 * @date :2024/4/10 18:53
 * @classname :  BRController
 * @description : TODO
 */
@RestController
@RequestMapping("/br/")
public class BRController {
    @Resource
    private BRService brService;

    @PostMapping("/backup")
    public Result backupDatabase() {
        try {
            String backupResult = brService.backupMongoDatabase();
            if (backupResult.isEmpty()){
                return Result.error(502, "MongoDB备份失败");
            }
            return Result.success(backupResult);
        } catch (IOException | InterruptedException e) {
            // 备份异常
            return Result.error(502, "MongoDB备份异常");
        }
    }

    @PostMapping("/restore")
    public Result restoreDatabase(String filename, boolean drop) {
        try {
            boolean backupSuccess = brService.restoreMongoDatabase(filename, drop);
            return Result.success(backupSuccess);
        } catch (IOException | InterruptedException e) {
            // 备份异常
            return Result.error(502, "MongoDB恢复失败");
        }
    }

    @PostMapping("/backupFileList")
    public Result getAllBackupFiles(){
        return Result.success(brService.getAllBackupFiles());
    }

    @PostMapping("/uploadBackup")
    public boolean uploadBackup(@RequestParam("file") MultipartFile file) {
        try {
            brService.uploadBackup(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/downloadBackup")
    public ResponseEntity<StreamingResponseBody> downloadBackup(@RequestParam("filename") String filename) {
        try {
            byte[] fileContent = brService.downloadBackup(filename);
            StreamingResponseBody streamingResponseBody = outputStream -> {
                outputStream.write(fileContent);
            };
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(streamingResponseBody);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @RequestMapping("/deleteBackup")
    public Result deleteBackup(String filename){
        if (brService.deleteBackup(filename)){
            return Result.success(true);
        }
        return Result.error(502, "删除失败");
    }
}
