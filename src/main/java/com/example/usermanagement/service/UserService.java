package com.example.usermanagement.service;

import com.example.usermanagement.dto.*;
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
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 登录方法 - 返回用户权限信息
    public Map<String, Object> login(LoginRequest request, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user == null) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }

        if (user.getStatus() == 0) {
            result.put("success", false);
            result.put("message", "账号已被禁用，请联系管理员");
            return result;
        }

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            session.setAttribute("roleLevel", user.getRoleLevel());
            session.setAttribute("realName", user.getRealName());

            result.put("success", true);
            result.put("message", "登录成功");

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("realName", user.getRealName());
            userInfo.put("role", user.getRole());
            userInfo.put("roleLevel", user.getRoleLevel());
            result.put("user", userInfo);
        } else {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
        }

        return result;
    }

    // 获取当前登录用户信息
    public Map<String, Object> getCurrentUser(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        result.put("success", true);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("role", user.getRole());
        result.put("roleLevel", user.getRoleLevel());

        return result;
    }

    // 创建新用户（仅超级管理员可用）
    public Map<String, Object> createUser(UserCreateRequest request, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        Integer currentUserId = (Integer) session.getAttribute("userId");
        Integer currentRoleLevel = (Integer) session.getAttribute("roleLevel");

        if (currentUserId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        // 检查权限：只有超级管理员可以创建用户
        if (currentRoleLevel != null && currentRoleLevel != 0) {
            result.put("success", false);
            result.put("message", "权限不足，只有超级管理员可以创建账号");
            return result;
        }

        // 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }

        try {
            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setRealName(request.getRealName());
            newUser.setRoleLevel(request.getRoleLevel() != null ? request.getRoleLevel() : 2);
            newUser.setStatus(1);
            newUser.setCreatedBy(currentUserId);

            // 设置角色标识
            if (newUser.getRoleLevel() == 0) {
                newUser.setRole("super_admin");
            } else if (newUser.getRoleLevel() == 1) {
                newUser.setRole("normal_user");
            } else {
                newUser.setRole("guest");
            }

            userRepository.save(newUser);

            result.put("success", true);
            result.put("message", "创建用户成功");
            result.put("userId", newUser.getId());

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建失败：" + e.getMessage());
        }

        return result;
    }

    // 获取所有用户列表（仅超级管理员可用）
    public Map<String, Object> getUserList(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        Integer currentUserId = (Integer) session.getAttribute("userId");
        Integer currentRoleLevel = (Integer) session.getAttribute("roleLevel");

        if (currentUserId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        // 只有超级管理员可以查看用户列表
        if (currentRoleLevel != null && currentRoleLevel != 0) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        List<User> users = userRepository.findAllExceptCurrent(currentUserId);
        List<UserInfoResponse> userList = users.stream()
                .map(u -> new UserInfoResponse(u.getId(), u.getUsername(), u.getRealName(), u.getRole(), u.getRoleLevel(), u.getStatus()))
                .collect(Collectors.toList());

        result.put("success", true);
        result.put("data", userList);
        return result;
    }

    // 删除用户（仅超级管理员可用）
    public Map<String, Object> deleteUser(Integer userId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        Integer currentUserId = (Integer) session.getAttribute("userId");
        Integer currentRoleLevel = (Integer) session.getAttribute("roleLevel");

        if (currentUserId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        // 只有超级管理员可以删除用户
        if (currentRoleLevel != null && currentRoleLevel != 0) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        // 不能删除自己
        if (currentUserId.equals(userId)) {
            result.put("success", false);
            result.put("message", "不能删除自己的账号");
            return result;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        userRepository.deleteById(userId);
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }

    // 获取数据（根据权限过滤）
    public Map<String, Object> getData(String keyword, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        Integer currentRoleLevel = (Integer) session.getAttribute("roleLevel");

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

        // 根据权限过滤敏感字段
        boolean isSuperAdmin = (currentRoleLevel != null && currentRoleLevel == 0);
        boolean isNormalUser = (currentRoleLevel != null && currentRoleLevel == 1);
        boolean isGuest = (currentRoleLevel != null && currentRoleLevel == 2);

        result.put("success", true);
        result.put("data", data);
        result.put("userRoleLevel", currentRoleLevel);
        result.put("canDelete", isSuperAdmin);
        result.put("canEdit", isSuperAdmin || isNormalUser);
        result.put("canViewPhone", isSuperAdmin || isNormalUser);
        result.put("canViewAmount", isSuperAdmin || isNormalUser);

        return result;
    }

    // 删除数据（仅超级管理员可用）
    public Map<String, Object> deleteData(Integer dataId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        Integer currentRoleLevel = (Integer) session.getAttribute("roleLevel");

        if (session.getAttribute("userId") == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        // 只有超级管理员可以删除数据
        if (currentRoleLevel == null || currentRoleLevel != 0) {
            result.put("success", false);
            result.put("message", "权限不足，只有超级管理员可以删除数据");
            return result;
        }

        UserData userData = userDataRepository.findById(dataId).orElse(null);
        if (userData == null) {
            result.put("success", false);
            result.put("message", "数据不存在");
            return result;
        }

        userDataRepository.deleteById(dataId);
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }

    // 新增或更新用户数据（根据权限）
    public Map<String, Object> addUserData(AddUserRequest request, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        Integer currentUserId = (Integer) session.getAttribute("userId");
        Integer currentRoleLevel = (Integer) session.getAttribute("roleLevel");

        if (currentUserId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        // 游客不能新增/修改数据
        if (currentRoleLevel != null && currentRoleLevel == 2) {
            result.put("success", false);
            result.put("message", "游客账号无权限修改数据");
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