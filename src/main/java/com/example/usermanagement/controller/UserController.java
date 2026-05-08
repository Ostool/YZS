package com.example.usermanagement.controller;

import com.example.usermanagement.dto.AddUserRequest;
import com.example.usermanagement.dto.LoginRequest;
import com.example.usermanagement.dto.LoginResponse;
import com.example.usermanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        LoginResponse response = userService.login(request, session);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/getData")
    public ResponseEntity<Map<String, Object>> getData(@RequestBody(required = false) Map<String, String> params, HttpSession session) {
        String keyword = params != null ? params.get("search") : null;
        Map<String, Object> result = userService.getData(keyword, session);
        if (result.containsKey("success") && (boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(401).body(result);
        }
    }

    @PostMapping("/addUser")
    public ResponseEntity<Map<String, Object>> addUser(@Valid @RequestBody AddUserRequest request, HttpSession session) {
        Map<String, Object> result = userService.addUserData(request, session);
        if (result.containsKey("success") && (boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/getUser/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> result = userService.getUserData(id, session);
        if (result.containsKey("success") && (boolean) result.get("success")) {
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
        } else {
            result.put("isLogin", false);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/updateUser")
    public ResponseEntity<Map<String, Object>> updateUser(@Valid @RequestBody AddUserRequest request, HttpSession session) {
        // 添加日志打印
        System.out.println("========== 收到更新请求 ==========");
        System.out.println("接收到的数据: " + request);
        System.out.println("用户ID: " + request.getId());
        System.out.println("姓名: " + request.getName());

        Map<String, Object> result = userService.addUserData(request, session);
        if (result.containsKey("success") && (boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}