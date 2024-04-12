package com.jeblove.onList.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : Jeb
 * @date :2024/4/10 18:41
 * @classname :  BRService
 * @description : TODO
 */
@Service
@Slf4j
public class BRService {
    @Value("${app.backup.dir}")
    private String backupDir;

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String database;

    /**
     * 备份mongo数据库
     * 生成文件处于项目根目录下app.backup.dir中
     * @return true or false
     * @throws IOException
     * @throws InterruptedException
     */
    public String backupMongoDatabase() throws IOException, InterruptedException {
        LocalDateTime currentDateTime = LocalDateTime.now(); // 获取当前日期和时间
        // 格式化日期时间
        String formattedDateTime = currentDateTime.format(DateTimeFormatter.ofPattern("yyMMdd_HH-mm-ss"));
        String backupFileName = "mongo_backup_" + formattedDateTime + ".gz";
        // 备份路径
        Path backupFilePath = Paths.get(System.getProperty("user.dir"), backupDir, backupFileName);
        // 检查目录是否存在，如果不存在则创建
        Files.createDirectories(backupFilePath.getParent());

        ProcessBuilder pb = new ProcessBuilder(Arrays.asList(
                "mongodump",
                "--uri", mongoUri,
                "--db", database,
                "--archive="+ backupFilePath,
                "--gzip"
        ));

        pb.redirectErrorStream(true);
        Process process = pb.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("MongoDB备份失败: {}", exitCode);
            return "";
        }
        log.info("MongoDB备份成功: {}", backupFilePath);
        return backupFileName;
    }

    /**
     * 恢复mongo数据库
     * @param backupFileName 备份文件名
     * @param drop boolean 是否删除所有数据再恢复
     * @return true or false
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean restoreMongoDatabase(String backupFileName, boolean drop) throws IOException, InterruptedException {
        Path backupFilePath = Paths.get(System.getProperty("user.dir"), backupDir, backupFileName);

        // 检查备份文件是否存在
        if (!Files.exists(backupFilePath)) {
            log.error("指定的备份文件不存在: {}", backupFileName);
            return false;
        }
        List<String> commandArguments = new ArrayList<>(Arrays.asList(
                "mongorestore",
                "--uri", mongoUri,
                "--db", database,
                "--gzip",
                "--archive=" + backupFilePath
        ));
        if (drop) {
            commandArguments.add("--drop");
        }

        ProcessBuilder pb = new ProcessBuilder(commandArguments);
        Process process = pb.start();

        // 输出打印
        InputStreamReader isr = new InputStreamReader(process.getErrorStream());
        BufferedReader errorReader = new BufferedReader(isr);
        String errorLine;
        String lastErrorLine = null;
        while ((errorLine = errorReader.readLine()) != null) {
            lastErrorLine = errorLine;
        }
        int successCount = 0;
        int failureCount = 0;
        if (lastErrorLine != null) {
            log.debug(lastErrorLine);
            // 使用正则表达式匹配成功和失败记录数
            Pattern successPattern = Pattern.compile("(\\d+) document\\(s\\) restored successfully");
            Pattern failurePattern = Pattern.compile("(\\d+) document\\(s\\) failed to restore");
            Matcher successMatcher = successPattern.matcher(lastErrorLine);
            Matcher failureMatcher = failurePattern.matcher(lastErrorLine);
            if (successMatcher.find()) {
                successCount = Integer.parseInt(successMatcher.group(1));
            }
            if (failureMatcher.find()) {
                failureCount = Integer.parseInt(failureMatcher.group(1));
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("MongoDB恢复失败: {}", exitCode);
            return false;
        }
        log.info("从文件{}|drop参数{}, 恢复MongoDB: {}文档成功, {}文档失败", backupFileName, drop, successCount, failureCount);
        return true;
    }

    /**
     * 获取所有备份文件
     * @return 备份文件名列表
     */
    public List<String> getAllBackupFiles() {
        String rootPath = System.getProperty("user.dir");
        String backupFolderPath = rootPath + File.separator + backupDir;
        List<String> filenames = new ArrayList<>();

        File backupFolder = new File(backupFolderPath);
        if (backupFolder.exists() && backupFolder.isDirectory()) {
            // 获取文件夹中的所有文件
            File[] files = backupFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        filenames.add(file.getName());
                    }
                }
            }
        } else {
            log.error("备份文件夹不存在或者不是一个目录");
        }
        return filenames;
    }

    /**
     * 上传文件到备份目录
     * @param file 文件
     * @throws IOException
     */
    public void uploadBackup(MultipartFile file) throws IOException {
        String rootPath = System.getProperty("user.dir");
        String backupFolderPath = rootPath + File.separator + backupDir;

        File backupFolder = new File(backupFolderPath);
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }

        // 获取上传文件的原始名称
        String originalFileName = file.getOriginalFilename();

        File destFile = new File(backupFolderPath + File.separator + originalFileName);
        log.info("上传备份文件{}", originalFileName);
        file.transferTo(destFile);
    }

    /**
     * 下载备份文件
     * @param filename 文件名
     * @return
     * @throws IOException
     */
    public byte[] downloadBackup(String filename) throws IOException {
        String rootPath = System.getProperty("user.dir");
        String backupFolderPath = rootPath + File.separator + backupDir;
        File file = new File(backupFolderPath + File.separator + filename);

        if (!file.exists()) {
            log.error("没有该文件{}", filename);
            throw new IOException( filename);
        }
        // 读取文件内容并返回
        try (FileInputStream fis = new FileInputStream(file)) {
            return FileCopyUtils.copyToByteArray(fis);
        }
    }

    /**
     * 删除备份文件
     * @param filename 文件名
     * @return boolean
     */
    public boolean deleteBackup(String filename) {
        String rootPath = System.getProperty("user.dir");
        String backupFolderPath = rootPath + File.separator + backupDir;
        File targetFile = new File(backupFolderPath, filename);

        if (targetFile.exists() && targetFile.isFile()) {
            log.info("删除备份{}成功", filename);
            return targetFile.delete();
        } else {
            log.error("指定的备份文件 {} 不存在", filename);
            return false;
        }
    }


}
