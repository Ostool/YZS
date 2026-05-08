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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public LoginResponse login(LoginRequest request, HttpSession session) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());

            return new LoginResponse(true, "登录成功",
                    new LoginResponse.UserInfo(user.getUsername(), user.getRole()));
        }

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

    // 新增或更新用户数据
    public Map<String, Object> addUserData(AddUserRequest request, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (session.getAttribute("userId") == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        try {
            UserData userData;
            boolean isNew = (request.getId() == null);

            if (!isNew) {
                userData = userDataRepository.findById(request.getId()).orElse(null);
                if (userData == null) {
                    result.put("success", false);
                    result.put("message", "数据不存在");
                    return result;
                }
            } else {
                userData = new UserData();
                long count = userDataRepository.count();
                String serialNo = String.format("%03d", count + 1);
                userData.setSerialNo(serialNo);
            }

            // 基本信息
            userData.setName(request.getName());
            userData.setGender(request.getGender());
            userData.setAge(request.getAge());
            userData.setShopName(request.getShopName());
            userData.setPhone(request.getPhone());
            userData.setOccupation(request.getOccupation() != null ? request.getOccupation() : "无");
            userData.setGlassesPurpose(request.getGlassesPurpose());

            // 处理配镜日期
            if (request.getPrescriptionDate() != null && !request.getPrescriptionDate().isEmpty()) {
                try {
                    // 支持多种日期格式
                    String dateStr = request.getPrescriptionDate();
                    if (dateStr.contains("T")) {
                        dateStr = dateStr.replace("T", " ");
                    }
                    if (!dateStr.contains(":")) {
                        dateStr = dateStr + " 00:00:00";
                    }
                    if (dateStr.length() == 16) {
                        dateStr = dateStr + ":00";
                    }
                    LocalDateTime dateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    userData.setPrescriptionDate(dateTime);
                } catch (Exception e) {
                    System.out.println("日期解析失败: " + request.getPrescriptionDate());
                    userData.setPrescriptionDate(LocalDateTime.now());
                }
            } else if (isNew) {
                userData.setPrescriptionDate(LocalDateTime.now());
            }

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
            userData.setFrameActualPrice(request.getFrameActualPrice());
            userData.setLensType(request.getLensType());
            userData.setLensOriginalPrice(request.getLensOriginalPrice());
            userData.setLensActualPrice(request.getLensActualPrice());
            userData.setOtherItems(request.getOtherItems());
            userData.setOtherCost(request.getOtherCost() != null ? request.getOtherCost() : BigDecimal.ZERO);
            userData.setConsultant(request.getConsultant());
            userData.setNeedFollowup(request.getNeedFollowup() != null ? request.getNeedFollowup() : "否");
            userData.setRemark(request.getRemark());

            // 计算总金额
            BigDecimal frameActual = request.getFrameActualPrice() != null ? request.getFrameActualPrice() : BigDecimal.ZERO;
            BigDecimal lensActual = request.getLensActualPrice() != null ? request.getLensActualPrice() : BigDecimal.ZERO;
            BigDecimal otherCost = request.getOtherCost() != null ? request.getOtherCost() : BigDecimal.ZERO;
            BigDecimal total = frameActual.add(lensActual).add(otherCost);
            userData.setTotalAmount(total);

            userDataRepository.save(userData);

            result.put("success", true);
            if (isNew) {
                result.put("message", "添加成功");
                result.put("serialNo", userData.getSerialNo());
            } else {
                result.put("message", "更新成功");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }

        return result;
    }

    // 获取单条数据
    public Map<String, Object> getUserData(Integer id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (session.getAttribute("userId") == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        UserData userData = userDataRepository.findById(id).orElse(null);
        if (userData == null) {
            result.put("success", false);
            result.put("message", "数据不存在");
            return result;
        }

        result.put("success", true);
        result.put("data", userData);
        return result;
    }
}