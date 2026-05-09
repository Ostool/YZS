package com.yezishuo.usermanagement.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class ImageUploadUtil {

    // 图片存储目录 - 根据你的项目结构调整
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/pictures/";

    static {
        // 确保目录存在
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 保存 Base64 图片
     * @param base64Image Base64编码的图片
     * @return 保存的文件路径
     */
    public String saveBase64Image(String base64Image) {
        try {
            // 生成文件名：年月日时分秒毫秒
            String fileName = generateFileName();
            String filePath = UPLOAD_DIR + fileName + ".jpg";

            // 去掉 Base64 前缀（如果有）
            String base64Data = base64Image;
            if (base64Image.contains(",")) {
                base64Data = base64Image.split(",")[1];
            }

            // 解码并保存
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            Path path = Paths.get(filePath);
            Files.write(path, imageBytes);

            // 返回相对路径
            return "/uploads/pictures/" + fileName + ".jpg";

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 保存 MultipartFile 图片
     * @param file 上传的文件
     * @return 保存的文件路径
     */
    public String saveMultipartFile(MultipartFile file) {
        try {
            String fileName = generateFileName();
            String filePath = UPLOAD_DIR + fileName + ".jpg";
            file.transferTo(new File(filePath));
            return "/uploads/pictures/" + fileName + ".jpg";
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成文件名：年月日时分秒毫秒
     */
    private String generateFileName() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    /**
     * 删除图片
     * @param imagePath 图片路径
     */
    public void deleteImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            String fullPath = System.getProperty("user.dir") + imagePath;
            File file = new File(fullPath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 批量删除图片
     * @param imagePaths 图片路径列表
     */
    public void deleteImages(List<String> imagePaths) {
        if (imagePaths != null) {
            for (String path : imagePaths) {
                deleteImage(path);
            }
        }
    }
}