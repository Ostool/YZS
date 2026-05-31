package com.yezishuo.usermanagement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ImageUploadUtil.class);

    private static final String UPLOAD_ROOT_DIR = System.getProperty("user.dir") + "/uploads/pictures/";
    private static final String INSPECTION_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/promotionpictures/";

    private String getTodayFolderPath() {
        String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return UPLOAD_ROOT_DIR + dateFolder + "/";
    }

    private void ensureDirExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String saveBase64Image(String base64Image, String customerName) {
        try {
            String folderPath = getTodayFolderPath();
            ensureDirExists(folderPath);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String safeName = customerName.replaceAll("[\\\\/:*?\"<>|]", "");
            String fileName = safeName + timestamp + ".jpg";
            String fullPath = folderPath + fileName;

            String base64Data = base64Image;
            if (base64Image.contains(",")) {
                base64Data = base64Image.split(",")[1];
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            Path path = Paths.get(fullPath);
            Files.write(path, imageBytes);

            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            return "/uploads/pictures/" + dateFolder + "/" + fileName;

        } catch (IOException e) {
            log.error("图片保存失败", e);
            return null;
        }
    }

    public String saveBase64Image(String base64Image) {
        return saveBase64Image(base64Image, "image");
    }

    private String getFullPath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        String normalizedPath = Paths.get(relativePath).normalize().toString();
        if (normalizedPath.startsWith("/uploads/")) {
            String fullPath = System.getProperty("user.dir") + normalizedPath;
            // 路径遍历防护：确保最终路径在以 user.dir 开头的目录内
            Path resolvedPath = Paths.get(fullPath).normalize();
            if (!resolvedPath.startsWith(Paths.get(System.getProperty("user.dir")))) {
                log.warn("检测到路径遍历攻击: {}", relativePath);
                return null;
            }
            return resolvedPath.toString();
        }
        String fullPath = System.getProperty("user.dir") + "/uploads/pictures/" + normalizedPath;
        Path resolvedPath = Paths.get(fullPath).normalize();
        if (!resolvedPath.startsWith(Paths.get(System.getProperty("user.dir")))) {
            log.warn("检测到路径遍历攻击: {}", relativePath);
            return null;
        }
        return resolvedPath.toString();
    }

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
                    log.debug("图片已物理删除: {}", fullPath);
                } else {
                    log.warn("图片删除失败: {}", fullPath);
                }
                return deleted;
            } else {
                log.debug("图片文件不存在: {}", fullPath);
            }
        } catch (Exception e) {
            log.error("删除图片异常", e);
        }
        return false;
    }

    public void deleteImages(List<String> imagePaths) {
        if (imagePaths == null || imagePaths.isEmpty()) {
            return;
        }
        for (String path : imagePaths) {
            deleteImage(path);
        }
    }

    public String getImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "";
        }
        return relativePath;
    }

    private String getInspectionTodayFolderPath() {
        String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return INSPECTION_UPLOAD_DIR + dateFolder + "/";
    }

    public String saveInspectionImage(String base64Image, String storeName) {
        try {
            String folderPath = getInspectionTodayFolderPath();
            ensureDirExists(folderPath);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String safeName = storeName.replaceAll("[\\\\/:*?\"<>|]", "");
            String fileName = safeName + timestamp + ".jpg";
            String fullPath = folderPath + fileName;

            String base64Data = base64Image;
            if (base64Image.contains(",")) {
                base64Data = base64Image.split(",")[1];
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            Path path = Paths.get(fullPath);
            Files.write(path, imageBytes);

            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            return "/uploads/promotionpictures/" + dateFolder + "/" + fileName;

        } catch (IOException e) {
            log.error("巡检图片保存失败", e);
            return null;
        }
    }

    public String extractImagePath(String imageData) {
        if (imageData != null && imageData.contains("/uploads/")) {
            int startIndex = imageData.indexOf("/uploads/");
            if (startIndex != -1) {
                return imageData.substring(startIndex);
            }
        }
        return null;
    }
}
