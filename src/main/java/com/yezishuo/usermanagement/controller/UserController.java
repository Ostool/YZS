package com.yezishuo.usermanagement.controller;

import com.yezishuo.usermanagement.dto.AddUserRequest;
import com.yezishuo.usermanagement.dto.LoginRequest;
import com.yezishuo.usermanagement.dto.UserCreateRequest;
import com.yezishuo.usermanagement.service.UserService;
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
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

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
}