package com.example.usermanagement.service;


import com.example.usermanagement.dto.AddUserRequest;
import com.example.usermanagement.dto.LoginRequest;
import com.example.usermanagement.dto.LoginResponse;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.entity.UserData;


import com.example.usermanagement.repository.UserDataRepository;
import com.example.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /*public LoginResponse login(LoginRequest request, HttpSession session) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());

            return new LoginResponse(true, "登录成功",
                    new LoginResponse.UserInfo(user.getUsername(), user.getRole()));
        }

        return new LoginResponse(false, "用户名或密码错误", null);
    }*/

    public LoginResponse login(LoginRequest request, HttpSession session) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        System.out.println("尝试登录的用户名: " + request.getUsername());
        System.out.println("数据库中找到的用户: " + (user != null ? user.getUsername() : "null"));

        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());

            System.out.println("登录成功: " + user.getUsername());

            return new LoginResponse(true, "登录成功",
                    new LoginResponse.UserInfo(user.getUsername(), user.getRole()));
        }

        System.out.println("登录失败: 用户名或密码错误");
        return new LoginResponse(false, "用户名或密码错误", null);
    }

    public Map<String, Object> getData(String keyword, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (session.getAttribute("userId") == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        List<UserData> data;
        if (keyword != null && !keyword.trim().isEmpty()) {
            data = userDataRepository.searchByKeyword(keyword);
        } else {
            data = userDataRepository.findAll();
        }

        result.put("success", true);
        result.put("data", data);
        return result;
    }

    // 新增：添加用户数据
    public Map<String, Object> addUserData(AddUserRequest request, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 检查是否登录
        if (session.getAttribute("userId") == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        try {
            // 生成序号（格式：自动递增的三位数）
            long count = userDataRepository.count();
            String serialNo = String.format("%03d", count + 1);

            // 创建新用户数据
            UserData userData = new UserData();
            userData.setSerialNo(serialNo);
            userData.setName(request.getName());
            userData.setGender(request.getGender());
            userData.setAge(request.getAge());
            userData.setShopName(request.getShopName());

            // 保存到数据库
            userDataRepository.save(userData);

            result.put("success", true);
            result.put("message", "添加成功");
            result.put("serialNo", serialNo);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败：" + e.getMessage());
        }

        return result;
    }
}