package com.yezishuo.usermanagement.controller;

import com.yezishuo.usermanagement.dto.AddUserRequest;
import com.yezishuo.usermanagement.dto.LoginRequest;
import com.yezishuo.usermanagement.dto.UserCreateRequest;
import com.yezishuo.usermanagement.entity.StaffMember;
import com.yezishuo.usermanagement.entity.UserData;
import com.yezishuo.usermanagement.repository.StaffMemberRepository;
import com.yezishuo.usermanagement.repository.UserDataRepository;
import com.yezishuo.usermanagement.security.SecurityUtils;
import com.yezishuo.usermanagement.service.AuthService;
import com.yezishuo.usermanagement.service.PrescriptionService;
import com.yezishuo.usermanagement.service.UserManagementService;
import com.yezishuo.usermanagement.util.ImageUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private ImageUploadUtil imageUploadUtil;

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private StaffMemberRepository staffMemberRepository;

    // 兼容旧登录端点（委托给AuthService）
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        Map<String, Object> result = authService.login(request, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/currentUser")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        Map<String, Object> result = authService.getCurrentUser(session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(401).body(result);
    }

    @PostMapping("/createUser")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody UserCreateRequest request) {
        Map<String, Object> result = userManagementService.createUser(request);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/userList")
    public ResponseEntity<Map<String, Object>> getUserList() {
        Map<String, Object> result = userManagementService.getUserList();
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }

    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Integer userId) {
        Map<String, Object> result = userManagementService.deleteUser(userId);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/getData")
    public ResponseEntity<Map<String, Object>> getData(@RequestBody Map<String, String> params) {
        String keyword = params != null ? params.get("search") : null;
        String dateRange = params != null ? params.get("dateRange") : null;
        String startDate = params != null ? params.get("startDate") : null;
        String endDate = params != null ? params.get("endDate") : null;
        String shopName = params != null ? params.get("shopName") : null;
        int page = parseInt(params, "page", 0);
        int pageSize = parseInt(params, "pageSize", 10);
        Map<String, Object> result = prescriptionService.getData(keyword, dateRange, startDate, endDate, page, pageSize, shopName);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(401).body(result);
    }

    private int parseInt(Map<String, String> params, String key, int defaultValue) {
        try { return Integer.parseInt(params.getOrDefault(key, String.valueOf(defaultValue))); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    @PostMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(@RequestBody(required = false) Map<String, String> params) {
        String keyword = params != null ? params.get("search") : null;
        String dateRange = params != null ? params.get("dateRange") : null;
        String startDate = params != null ? params.get("startDate") : null;
        String endDate = params != null ? params.get("endDate") : null;
        String monthStart = params != null ? params.get("monthStart") : null;
        String monthEnd = params != null ? params.get("monthEnd") : null;
        String yearStart = params != null ? params.get("yearStart") : null;
        String yearEnd = params != null ? params.get("yearEnd") : null;
        String shopName = params != null ? params.get("shopName") : null;
        Map<String, Object> result = prescriptionService.getStatistics(keyword, dateRange, startDate, endDate,
                monthStart, monthEnd, yearStart, yearEnd, shopName);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(401).body(result);
    }

    @GetMapping("/salesStars")
    public ResponseEntity<Map<String, Object>> getSalesStars(@RequestParam(required = false) String shopName) {
        Map<String, Object> result = prescriptionService.getSalesStars(shopName);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(401).body(result);
    }

    @GetMapping("/shops")
    public ResponseEntity<Map<String, Object>> getShops() {
        Map<String, Object> result = new HashMap<>();
        if (!SecurityUtils.isSuperAdmin()) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(403).body(result);
        }
        List<String> shops = Arrays.asList(
            "普宁明华体育馆店", "普宁广场店", "普宁国际商品城店",
            "普宁中华新城店", "普宁开心广场店", "普宁万泰新天地店",
            "叶子说-总部", "揭阳进贤门店", "揭阳东山店",
            "潮阳中华路店", "潮阳谷饶店", "普宁大坝店",
            "潮南广祥路店", "潮南两英店", "其他"
        );
        result.put("success", true);
        result.put("data", shops);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/deleteData/{dataId}")
    public ResponseEntity<Map<String, Object>> deleteData(@PathVariable Integer dataId) {
        Map<String, Object> result = prescriptionService.deleteData(dataId);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/addUser")
    public ResponseEntity<Map<String, Object>> addUser(@Valid @RequestBody AddUserRequest request) {
        Map<String, Object> result = prescriptionService.addUserData(request);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/getUser/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Integer id) {
        Map<String, Object> result = prescriptionService.getUserData(id);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    // 兼容旧端点
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
            result.put("realName", session.getAttribute("realName"));
            result.put("shopName", session.getAttribute("shopName"));
            result.put("roleLevel", session.getAttribute("roleLevel"));
        } else {
            result.put("isLogin", false);
        }
        return ResponseEntity.ok(result);
    }

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
        String customerName = request.get("customerName");
        if (customerName == null || customerName.isEmpty()) {
            customerName = "image";
        }

        if (base64Image == null || base64Image.isEmpty()) {
            result.put("success", false);
            result.put("message", "图片数据为空");
            return ResponseEntity.badRequest().body(result);
        }

        String savedPath = imageUploadUtil.saveBase64Image(base64Image, customerName);
        if (savedPath != null) {
            result.put("success", true);
            result.put("path", savedPath);
        } else {
            result.put("success", false);
            result.put("message", "图片保存失败");
        }
        return ResponseEntity.ok(result);
    }

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

        if (!imagesToDelete.isEmpty()) {
            imageUploadUtil.deleteImages(imagesToDelete);
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

    // ==================== 门店店员管理 ====================

    @GetMapping("/staff")
    public ResponseEntity<Map<String, Object>> getStaffMembers(
            @RequestParam(required = false) String shopName, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return ResponseEntity.status(401).body(result);
        }
        List<StaffMember> list;
        if (shopName != null && !shopName.isEmpty()) {
            list = staffMemberRepository.findByShopNameOrderByNameAsc(shopName);
        } else {
            list = staffMemberRepository.findAllByOrderByShopNameAscNameAsc();
        }
        result.put("success", true);
        result.put("data", list);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/staff")
    public ResponseEntity<Map<String, Object>> addStaffMember(@RequestBody Map<String, String> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Integer roleLevel = (Integer) session.getAttribute("roleLevel");
        if (roleLevel == null || roleLevel != 0) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(403).body(result);
        }
        String shopName = body.get("shopName");
        String name = body.get("name");
        if (shopName == null || shopName.isEmpty() || name == null || name.isEmpty()) {
            result.put("success", false);
            result.put("message", "门店和姓名不能为空");
            return ResponseEntity.badRequest().body(result);
        }
        StaffMember member = new StaffMember();
        member.setShopName(shopName);
        member.setName(name);
        staffMemberRepository.save(member);
        result.put("success", true);
        result.put("data", member);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/staff/{id}")
    public ResponseEntity<Map<String, Object>> deleteStaffMember(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Integer roleLevel = (Integer) session.getAttribute("roleLevel");
        if (roleLevel == null || roleLevel != 0) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(403).body(result);
        }
        if (!staffMemberRepository.existsById(id)) {
            result.put("success", false);
            result.put("message", "店员不存在");
            return ResponseEntity.badRequest().body(result);
        }
        staffMemberRepository.deleteById(id);
        result.put("success", true);
        result.put("message", "已删除");
        return ResponseEntity.ok(result);
    }
}
