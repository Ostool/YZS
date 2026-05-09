package com.yezishuo.usermanagement.util;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Component
public class ImageUploadUtil {

    // 图片存储根目录
    private static final String UPLOAD_ROOT_DIR = System.getProperty("user.dir") + "/uploads/pictures/";

    /**
     * 获取当天的子文件夹路径（格式：yyyyMMdd）
     */
    private String getTodayFolderPath() {
        String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return UPLOAD_ROOT_DIR + dateFolder + "/";
    }

    /**
     * 确保目录存在
     */
    private void ensureDirExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 生成文件名：年月日时分秒毫秒
     */
    private String generateFileName() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    /**
     * 保存 Base64 图片，按日期分文件夹存储
     * @param base64Image Base64编码的图片
     * @return 保存的相对路径（例如：/uploads/pictures/20260509/20260509143025123.jpg）
     */
    public String saveBase64Image(String base64Image) {
        try {
            // 获取当天文件夹路径
            String folderPath = getTodayFolderPath();
            ensureDirExists(folderPath);

            // 生成文件名
            String fileName = generateFileName() + ".jpg";
            String fullPath = folderPath + fileName;

            // 去掉 Base64 前缀（如果有）
            String base64Data = base64Image;
            if (base64Image.contains(",")) {
                base64Data = base64Image.split(",")[1];
            }

            // 解码并保存
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            Path path = Paths.get(fullPath);
            Files.write(path, imageBytes);

            // 返回相对路径（用于前端访问）
            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            return "/uploads/pictures/" + dateFolder + "/" + fileName;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据相对路径获取完整的文件路径
     * @param relativePath 相对路径（例如：/uploads/pictures/20260509/xxx.jpg）
     * @return 完整文件路径
     */
    private String getFullPath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        // 相对路径格式：/uploads/pictures/20260509/xxx.jpg
        // 转换为绝对路径：项目根目录 + /uploads/pictures/20260509/xxx.jpg
        if (relativePath.startsWith("/uploads/")) {
            return System.getProperty("user.dir") + relativePath;
        }
        return System.getProperty("user.dir") + "/uploads/pictures/" + relativePath;
    }

    /**
     * 删除单张图片
     * @param imagePath 图片相对路径
     * @return 是否删除成功
     */
    public boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }
        try {
            String fullPath = getFullPath(imagePath);
            if (fullPath == null) {
                return false;
            }
            File file = new File(fullPath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    System.out.println("图片已删除: " + fullPath);
                } else {
                    System.out.println("图片删除失败: " + fullPath);
                }
                return deleted;
            }
        } catch (Exception e) {
            System.err.println("删除图片异常: " + e.getMessage());
        }
        return false;
    }

    /**
     * 批量删除图片
     * @param imagePaths 图片路径列表
     */
    public void deleteImages(List<String> imagePaths) {
        if (imagePaths == null || imagePaths.isEmpty()) {
            return;
        }
        for (String path : imagePaths) {
            deleteImage(path);
        }
    }

    /**
     * 获取图片的完整URL（用于前端显示）
     * @param relativePath 相对路径
     * @return 完整URL
     */
    public String getImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "";
        }
        // 返回相对路径，前端会自动拼接 API_BASE
        return relativePath;
    }
}