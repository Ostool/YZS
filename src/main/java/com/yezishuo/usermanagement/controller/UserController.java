package com.yezishuo.usermanagement.controller;

import com.yezishuo.usermanagement.dto.AddUserRequest;
import com.yezishuo.usermanagement.dto.LoginRequest;
import com.yezishuo.usermanagement.dto.UserCreateRequest;
import com.yezishuo.usermanagement.entity.UserData;
import com.yezishuo.usermanagement.repository.UserDataRepository;
import com.yezishuo.usermanagement.repository.UserRepository;
import com.yezishuo.usermanagement.service.UserService;
import com.yezishuo.usermanagement.util.ImageUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ImageUploadUtil imageUploadUtil;

    @Autowired
    private UserDataRepository userDataRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        Map<String, Object> result = userService.login(request, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/currentUser")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        Map<String, Object> result = userService.getCurrentUser(session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(401).body(result);
        }
    }

    @PostMapping("/createUser")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody UserCreateRequest request, HttpSession session) {
        Map<String, Object> result = userService.createUser(request, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/userList")
    public ResponseEntity<Map<String, Object>> getUserList(HttpSession session) {
        Map<String, Object> result = userService.getUserList(session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }

    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Integer userId, HttpSession session) {
        Map<String, Object> result = userService.deleteUser(userId, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/getData")
    public ResponseEntity<Map<String, Object>> getData(@RequestBody Map<String, String> params, HttpSession session) {
        String keyword = params != null ? params.get("search") : null;
        String dateRange = params != null ? params.get("dateRange") : null;
        String startDate = params != null ? params.get("startDate") : null;
        String endDate = params != null ? params.get("endDate") : null;
        Map<String, Object> result = userService.getData(keyword, session, dateRange, startDate, endDate);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(401).body(result);
        }
    }

    @PostMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(@RequestBody(required = false) Map<String, String> params, HttpSession session) {
        String keyword = params != null ? params.get("search") : null;
        String dateRange = params != null ? params.get("dateRange") : null;
        String startDate = params != null ? params.get("startDate") : null;
        String endDate = params != null ? params.get("endDate") : null;
        Map<String, Object> result = userService.getStatistics(session, keyword, dateRange, startDate, endDate);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(401).body(result);
        }
    }

    @DeleteMapping("/deleteData/{dataId}")
    public ResponseEntity<Map<String, Object>> deleteData(@PathVariable Integer dataId, HttpSession session) {
        Map<String, Object> result = userService.deleteData(dataId, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/addUser")
    public ResponseEntity<Map<String, Object>> addUser(@Valid @RequestBody AddUserRequest request, HttpSession session) {
        Map<String, Object> result = userService.addUserData(request, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/getUser/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> result = userService.getUserData(id, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已退出登录");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/checkLogin")
    public ResponseEntity<Map<String, Object>> checkLogin(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (session.getAttribute("userId") != null) {
            result.put("isLogin", true);
            result.put("username", session.getAttribute("username"));
            result.put("roleLevel", session.getAttribute("roleLevel"));
        } else {
            result.put("isLogin", false);
        }
        return ResponseEntity.ok(result);
    }

    // 在启动类或配置类中添加静态资源映射
// 或者直接在 Controller 中添加图片访问接口
    @GetMapping("/uploads/pictures/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        try {
            String filePath = System.getProperty("user.dir") + "/uploads/pictures/" + filename;
            Path path = Paths.get(filePath);
            byte[] imageBytes = Files.readAllBytes(path);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 单独上传图片接口
    @PostMapping("/uploadImage")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestBody Map<String, String> request, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return ResponseEntity.status(401).body(result);
        }

        String base64Image = request.get("image");
        if (base64Image == null || base64Image.isEmpty()) {
            result.put("success", false);
            result.put("message", "图片数据为空");
            return ResponseEntity.badRequest().body(result);
        }

        String savedPath = imageUploadUtil.saveBase64Image(base64Image);
        if (savedPath != null) {
            result.put("success", true);
            result.put("path", savedPath);
        } else {
            result.put("success", false);
            result.put("message", "图片保存失败");
        }
        return ResponseEntity.ok(result);
    }

    // 更新图片接口
    @PostMapping("/updateUserImages")
    public ResponseEntity<Map<String, Object>> updateUserImages(@RequestBody Map<String, Object> request, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return ResponseEntity.status(401).body(result);
        }

        Integer recordId = null;
        if (request.get("id") instanceof Integer) {
            recordId = (Integer) request.get("id");
        } else if (request.get("id") instanceof String) {
            recordId = Integer.parseInt((String) request.get("id"));
        }

        String prescriptionImages = (String) request.get("prescriptionImages");

        // 处理 imagesToDelete - 兼容多种格式
        List<String> imagesToDelete = new ArrayList<>();
        Object deleteObj = request.get("imagesToDelete");
        if (deleteObj instanceof List) {
            List<?> tempList = (List<?>) deleteObj;
            for (Object obj : tempList) {
                if (obj instanceof String) {
                    imagesToDelete.add((String) obj);
                } else if (obj != null) {
                    imagesToDelete.add(obj.toString());
                }
            }
        }

        if (recordId == null) {
            result.put("success", false);
            result.put("message", "记录ID不能为空");
            return ResponseEntity.badRequest().body(result);
        }

        UserData userData = userDataRepository.findById(recordId).orElse(null);
        if (userData == null) {
            result.put("success", false);
            result.put("message", "记录不存在");
            return ResponseEntity.badRequest().body(result);
        }

        // 打印调试信息
        System.out.println("========== 更新图片 ==========");
        System.out.println("recordId: " + recordId);
        System.out.println("prescriptionImages: " + prescriptionImages);
        System.out.println("imagesToDelete: " + imagesToDelete);
        System.out.println("imagesToDelete size: " + imagesToDelete.size());
        for (String path : imagesToDelete) {
            System.out.println("  待删除: " + path);
        }

        // 物理删除不再使用的旧图片
        if (!imagesToDelete.isEmpty()) {
            imageUploadUtil.deleteImages(imagesToDelete);
            System.out.println("已物理删除图片数量: " + imagesToDelete.size());
        }

        userData.setPrescriptionImages(prescriptionImages);
        userDataRepository.save(userData);

        result.put("success", true);
        result.put("message", "图片更新成功");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/deleteImages")
    public ResponseEntity<Map<String, Object>> deleteImages(@RequestBody Map<String, Object> request, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        Integer userId = (Integer) session.getAttribute("userId");
        Integer roleLevel = (Integer) session.getAttribute("roleLevel");

        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return ResponseEntity.status(401).body(result);
        }

        if (roleLevel == null || roleLevel != 0) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(403).body(result);
        }

        // 处理 imagesToDelete - 兼容多种格式
        List<String> imagesToDelete = new ArrayList<>();
        Object deleteObj = request.get("images");
        if (deleteObj instanceof List) {
            List<?> tempList = (List<?>) deleteObj;
            for (Object obj : tempList) {
                if (obj instanceof String) {
                    imagesToDelete.add((String) obj);
                } else if (obj != null) {
                    imagesToDelete.add(obj.toString());
                }
            }
        }

        System.out.println("deleteImages 收到: " + imagesToDelete);

        if (!imagesToDelete.isEmpty()) {
            imageUploadUtil.deleteImages(imagesToDelete);
            result.put("success", true);
            result.put("message", "已删除 " + imagesToDelete.size() + " 张图片");
        } else {
            result.put("success", true);
            result.put("message", "没有需要删除的图片");
        }

        return ResponseEntity.ok(result);
    }

}