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

    public Map<String, Object> addUserData(AddUserRequest request, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (session.getAttribute("userId") == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        try {
            long count = userDataRepository.count();
            String serialNo = String.format("%03d", count + 1);

            UserData userData = new UserData();
            // 基本信息
            userData.setSerialNo(serialNo);
            userData.setName(request.getName());
            userData.setGender(request.getGender());
            userData.setAge(request.getAge());
            userData.setShopName(request.getShopName());
            userData.setPhone(request.getPhone());
            userData.setOccupation(request.getOccupation() != null ? request.getOccupation() : "无");
            userData.setGlassesPurpose(request.getGlassesPurpose());

            // 左眼视力
            userData.setLeftSphere(request.getLeftSphere());
            userData.setLeftCylinder(request.getLeftCylinder());
            userData.setLeftAxis(request.getLeftAxis());
            userData.setLeftAdd(request.getLeftAdd());
            userData.setLeftUncorrectedVision(request.getLeftUncorrectedVision());
            userData.setLeftCorrectedVision(request.getLeftCorrectedVision());
            userData.setLeftPupilDistance(request.getLeftPupilDistance());
            userData.setLeftPupilHeight(request.getLeftPupilHeight());

            // 右眼视力
            userData.setRightSphere(request.getRightSphere());
            userData.setRightCylinder(request.getRightCylinder());
            userData.setRightAxis(request.getRightAxis());
            userData.setRightAdd(request.getRightAdd());
            userData.setRightUncorrectedVision(request.getRightUncorrectedVision());
            userData.setRightCorrectedVision(request.getRightCorrectedVision());
            userData.setRightPupilDistance(request.getRightPupilDistance());
            userData.setRightPupilHeight(request.getRightPupilHeight());

            // 商品信息
            userData.setFrameModel(request.getFrameModel());
            userData.setFrameOriginalPrice(request.getFrameOriginalPrice());
            userData.setFrameDiscount(request.getFrameDiscount());
            userData.setFrameFinalPrice(request.getFrameFinalPrice());
            userData.setLensType(request.getLensType());
            userData.setLensOriginalPrice(request.getLensOriginalPrice());
            userData.setLensDiscount(request.getLensDiscount());
            userData.setLensFinalPrice(request.getLensFinalPrice());
            userData.setOtherItems(request.getOtherItems());
            userData.setTotalAmount(request.getTotalAmount());
            userData.setConsultant(request.getConsultant());
            userData.setNeedFollowup(request.getNeedFollowup() != null ? request.getNeedFollowup() : "否");
            userData.setRemark(request.getRemark());

            userDataRepository.save(userData);

            result.put("success", true);
            result.put("message", "添加成功");
            result.put("serialNo", serialNo);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "添加失败：" + e.getMessage());
        }

        return result;
    }
}