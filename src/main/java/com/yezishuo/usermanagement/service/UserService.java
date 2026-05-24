package com.yezishuo.usermanagement.service;

import com.yezishuo.usermanagement.dto.*;
import com.yezishuo.usermanagement.entity.User;
import com.yezishuo.usermanagement.entity.UserData;
import com.yezishuo.usermanagement.repository.UserDataRepository;
import com.yezishuo.usermanagement.repository.UserRepository;
import com.yezishuo.usermanagement.util.ImageUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ImageUploadUtil imageUploadUtil;

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
            // ========== 原有 session 属性（处方系统使用）==========
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            session.setAttribute("roleLevel", user.getRoleLevel());
            session.setAttribute("realName", user.getRealName());
            session.setAttribute("shopName", user.getShopName());  // 门店名称

            // ========== 新增：调货系统需要的 session 属性 ==========
            // 根据 roleLevel 设置调货系统的角色
            // 0=超级管理员(SUPER), 1=普通用户(NORMAL), 2=游客(GUEST)
            String transferRole = "GUEST";
            Integer roleLevel = user.getRoleLevel();
            if (roleLevel != null) {
                if (roleLevel == 0) {
                    transferRole = "SUPER";     // 超级管理员
                } else if (roleLevel == 1) {
                    transferRole = "NORMAL";    // 普通用户
                } else if (roleLevel == 2) {
                    transferRole = "GUEST";     // 游客
                }
            }
            session.setAttribute("userRole", transferRole);

            // 设置显示名称（优先使用门店名称 shopName，其次真实姓名 realName，最后用户名 username）
            String displayName = user.getShopName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = user.getRealName();
            }
            if (displayName == null || displayName.isEmpty()) {
                displayName = user.getUsername();
            }
            session.setAttribute("userName", displayName);
            session.setAttribute("storeName", displayName);

            // 设置登录状态标志
            session.setAttribute("isLoggedIn", true);

            result.put("success", true);
            result.put("message", "登录成功");

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("realName", user.getRealName());
            userInfo.put("shopName", user.getShopName());
            userInfo.put("role", user.getRole());
            userInfo.put("roleLevel", user.getRoleLevel());
            userInfo.put("transferRole", transferRole);  // 调货系统角色
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
            newUser.setShopName(request.getShopName());

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
    // 修改 getData 方法，增加权限过滤
    public Map<String, Object> getData(String keyword, HttpSession session, String dateRange, String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();

        Integer userId = (Integer) session.getAttribute("userId");
        Integer currentRoleLevel = (Integer) session.getAttribute("roleLevel");
        String currentUserShopName = (String) session.getAttribute("shopName");

        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        // 获取当前用户的店名（用于普通用户过滤）
        User currentUser = userRepository.findById(userId).orElse(null);
        String shopName = currentUser != null ? currentUser.getShopName() : null;

        List<UserData> data = new ArrayList<>();

        try {
            // 处理日期范围
            LocalDateTime start = null;
            LocalDateTime end = null;

            if (dateRange != null && !dateRange.isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                switch (dateRange) {
                    case "today":
                        start = now.withHour(0).withMinute(0).withSecond(0);
                        end = now.withHour(23).withMinute(59).withSecond(59);
                        break;
                    case "month":
                        start = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                        end = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
                        break;
                    case "year":
                        start = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
                        end = now.withDayOfYear(now.toLocalDate().lengthOfYear()).withHour(23).withMinute(59).withSecond(59);
                        break;
                    default:
                        break;
                }
            } else if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
                start = LocalDateTime.parse(startDate + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                end = LocalDateTime.parse(endDate + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }

            // 根据角色和参数查询数据
            if (currentRoleLevel != null && currentRoleLevel == 1) {
                // 普通用户：只能看自己店名的数据
                if (start != null && end != null) {
                    data = userDataRepository.findByDateRangeForNormalUser(start, end, shopName);
                } else if (keyword != null && !keyword.trim().isEmpty()) {
                    data = userDataRepository.searchByKeywordForNormalUser(keyword, shopName);
                } else {
                    data = userDataRepository.findByShopNameOrderByPrescriptionDateDesc(shopName);
                }
            } else {
                // 超级管理员或游客：可以看所有数据
                if (start != null && end != null) {
                    data = userDataRepository.findByDateRange(start, end);
                } else if (keyword != null && !keyword.trim().isEmpty()) {
                    data = userDataRepository.searchByKeyword(keyword);
                } else {
                    data = userDataRepository.findAllByOrderByPrescriptionDateDesc();
                }
            }

            // 计算总金额
            BigDecimal totalAmount = data.stream()
                    .map(UserData::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.put("success", true);
            result.put("data", data);
            result.put("totalAmount", totalAmount);
            result.put("userRoleLevel", currentRoleLevel);
            result.put("canDelete", currentRoleLevel != null && currentRoleLevel == 0);
            result.put("canEdit", currentRoleLevel != null && (currentRoleLevel == 0 || currentRoleLevel == 1));
            result.put("canViewPhone", currentRoleLevel != null && currentRoleLevel != 2);
            result.put("canViewAmount", currentRoleLevel != null && currentRoleLevel != 2);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }

        return result;
    }

    // 添加获取统计信息的方法
    public Map<String, Object> getStatistics(HttpSession session, String keyword, String dateRange, String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();

        Integer userId = (Integer) session.getAttribute("userId");
        Integer currentRoleLevel = (Integer) session.getAttribute("roleLevel");

        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        User currentUser = userRepository.findById(userId).orElse(null);
        String shopName = currentUser != null ? currentUser.getShopName() : null;

        List<UserData> todayData = new ArrayList<>();
        List<UserData> monthData = new ArrayList<>();
        List<UserData> yearData = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = now.withHour(23).withMinute(59).withSecond(59);
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        LocalDateTime yearStart = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime yearEnd = now.withDayOfYear(now.toLocalDate().lengthOfYear()).withHour(23).withMinute(59).withSecond(59);

        // 根据角色查询，并考虑搜索关键字（只统计未删除的数据）
        if (currentRoleLevel != null && currentRoleLevel == 1) {
            // 普通用户：只查自己店名
            if (keyword != null && !keyword.trim().isEmpty()) {
                todayData = userDataRepository.searchByKeywordAndDateRangeForNormalUser(keyword, shopName, todayStart, todayEnd);
                monthData = userDataRepository.searchByKeywordAndDateRangeForNormalUser(keyword, shopName, monthStart, monthEnd);
                yearData = userDataRepository.searchByKeywordAndDateRangeForNormalUser(keyword, shopName, yearStart, yearEnd);
            } else {
                todayData = userDataRepository.findByDateRangeForNormalUser(todayStart, todayEnd, shopName);
                monthData = userDataRepository.findByDateRangeForNormalUser(monthStart, monthEnd, shopName);
                yearData = userDataRepository.findByDateRangeForNormalUser(yearStart, yearEnd, shopName);
            }
        } else {
            // 超级管理员和游客
            if (keyword != null && !keyword.trim().isEmpty()) {
                todayData = userDataRepository.searchByKeywordAndDateRange(keyword, todayStart, todayEnd);
                monthData = userDataRepository.searchByKeywordAndDateRange(keyword, monthStart, monthEnd);
                yearData = userDataRepository.searchByKeywordAndDateRange(keyword, yearStart, yearEnd);
            } else {
                todayData = userDataRepository.findByDateRange(todayStart, todayEnd);
                monthData = userDataRepository.findByDateRange(monthStart, monthEnd);
                yearData = userDataRepository.findByDateRange(yearStart, yearEnd);
            }
        }

        BigDecimal todayTotal = todayData.stream()
                .map(UserData::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal monthTotal = monthData.stream()
                .map(UserData::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal yearTotal = yearData.stream()
                .map(UserData::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.put("success", true);
        result.put("todayCount", todayData.size());
        result.put("todayAmount", todayTotal);
        result.put("monthCount", monthData.size());
        result.put("monthAmount", monthTotal);
        result.put("yearCount", yearData.size());
        result.put("yearAmount", yearTotal);

        return result;
    }
    // 删除数据（软删除）
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

        // 物理删除关联的图片文件
        if (userData.getPrescriptionImages() != null && !userData.getPrescriptionImages().isEmpty()) {
            String[] imagePaths = userData.getPrescriptionImages().split(",");
            imageUploadUtil.deleteImages(Arrays.asList(imagePaths));
        }

        // 软删除：设置删除标识，清空图片路径
        userData.setIsDeleted(1);
        userData.setPrescriptionImages(null);  // 清空图片路径
        userDataRepository.save(userData);

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
            // 保存图片
            if (request.getImageFiles() != null && !request.getImageFiles().isEmpty()) {
                List<String> savedPaths = new ArrayList<>();
                for (String base64Image : request.getImageFiles()) {
                    if (base64Image != null && !base64Image.isEmpty()) {
                        String savedPath = imageUploadUtil.saveBase64Image(base64Image);
                        if (savedPath != null) {
                            savedPaths.add(savedPath);
                        }
                    }
                }
                if (!savedPaths.isEmpty()) {
                    userData.setPrescriptionImages(String.join(",", savedPaths));
                }
            }
            // 处理图片：比较新旧图片，删除被移除的图片
            List<String> oldImagePaths = null;
            if (userData.getPrescriptionImages() != null && !userData.getPrescriptionImages().isEmpty()) {
                oldImagePaths = Arrays.asList(userData.getPrescriptionImages().split(","));
            }

            List<String> finalImagePaths = new ArrayList<>();

            // 1. 保留已有的图片（传入的 existingImages）
            if (request.getExistingImages() != null && !request.getExistingImages().isEmpty()) {
                finalImagePaths.addAll(request.getExistingImages());
            }

            // 2. 保存新上传的图片
            if (request.getNewImages() != null && !request.getNewImages().isEmpty()) {
                for (String base64Image : request.getNewImages()) {
                    if (base64Image != null && !base64Image.isEmpty()) {
                        String savedPath = imageUploadUtil.saveBase64Image(base64Image);
                        if (savedPath != null) {
                            finalImagePaths.add(savedPath);
                        }
                    }
                }
            }

            // 3. 删除不再使用的旧图片
            if (oldImagePaths != null && !oldImagePaths.isEmpty()) {
                List<String> pathsToDelete = new ArrayList<>(oldImagePaths);
                pathsToDelete.removeAll(finalImagePaths);
                if (!pathsToDelete.isEmpty()) {
                    imageUploadUtil.deleteImages(pathsToDelete);
                }
            }

            // 4. 保存到数据库
            if (!finalImagePaths.isEmpty()) {
                userData.setPrescriptionImages(String.join(",", finalImagePaths));
            } else {
                userData.setPrescriptionImages(null);
            }

            // 基本信息
            userData.setName(request.getName());
            userData.setGender(request.getGender());
            userData.setAge(request.getAge());
            userData.setShopName(request.getShopName());
            userData.setPhone(request.getPhone());
            userData.setOccupation(request.getOccupation() != null ? request.getOccupation() : "无");
            userData.setGlassesPurpose(request.getGlassesPurpose());
            userData.setFrameBrand(request.getFrameBrand());
            userData.setLensBrand(request.getLensBrand());

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

            // 在保存用户数据之前，处理图片
            if (request.getImageFiles() != null && !request.getImageFiles().isEmpty()) {
                List<String> savedPaths = new ArrayList<>();
                for (String base64Image : request.getImageFiles()) {
                    if (base64Image != null && !base64Image.isEmpty()) {
                        String savedPath = imageUploadUtil.saveBase64Image(base64Image);
                        if (savedPath != null) {
                            savedPaths.add(savedPath);
                        }
                    }
                }
                if (!savedPaths.isEmpty()) {
                    userData.setPrescriptionImages(String.join(",", savedPaths));
                }
            }
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