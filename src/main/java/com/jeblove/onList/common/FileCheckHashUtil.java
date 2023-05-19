package com.jeblove.onList.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author : Jeb
 * @date :2023/5/18 23:27
 * @classname :  FileCheckHashUtil
 * @description : TODO
 */
@Component
public class FileCheckHashUtil {

    private static String hashType;

    @Value("${fileLink.hashType}")
    public void setHashType(String hashType){
        FileCheckHashUtil.hashType = hashType;
    }

    public static String getHashCode(MultipartFile file) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(hashType);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(hashType+"计算异常", e);
        }

        InputStream is = file.getInputStream();
        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            StringBuilder builder = new StringBuilder();
            for (byte b : md5sum) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } finally {
            is.close();
        }
    }

}
