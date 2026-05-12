package com.yezishuo.usermanagement.controller;

import com.yezishuo.usermanagement.dto.InspectionDTO;
import com.yezishuo.usermanagement.entity.Inspection;
import com.yezishuo.usermanagement.repository.InspectionRepository;
import com.yezishuo.usermanagement.service.AnnouncementService;
import com.yezishuo.usermanagement.util.ImageUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inspections")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class InspectionController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private ImageUploadUtil imageUploadUtil;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllInspections() {
        return ResponseEntity.ok(announcementService.getAllInspections());
    }

    @GetMapping("/period/{period}")
    public ResponseEntity<Map<String, Object>> getInspectionsByPeriod(@PathVariable String period) {
        return ResponseEntity.ok(announcementService.getInspectionsByPeriod(period));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addInspection(@Valid @RequestBody InspectionDTO dto, HttpSession session) {
        Map<String, Object> result = announcementService.addInspection(dto, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateInspection(@PathVariable Integer id, @Valid @RequestBody InspectionDTO dto, HttpSession session) {
        Map<String, Object> result = announcementService.updateInspection(id, dto, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }

    @PutMapping("/{id}/image")
    public ResponseEntity<Map<String, Object>> updateInspectionImage(@PathVariable Integer id, @RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> result = announcementService.updateInspectionImage(id, data.get("imageData"), session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteInspection(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> result = announcementService.deleteInspection(id, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }

    @PutMapping("/note")
    public ResponseEntity<Map<String, Object>> updateInspectNote(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> result = announcementService.updateInspectNote(data.get("content"), session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }

    @PostMapping("/period")
    public ResponseEntity<Map<String, Object>> addInspectPeriod(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 检查权限
        Integer roleLevel = (Integer) session.getAttribute("roleLevel");
        if (roleLevel == null || (roleLevel != 0 && roleLevel != 1)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(403).body(result);
        }

        String period = data.get("period");
        if (period == null || period.isEmpty()) {
            result.put("success", false);
            result.put("message", "请输入批次日期");
            return ResponseEntity.badRequest().body(result);
        }

        // 检查批次是否已存在
        List<Inspection> existing = inspectionRepository.findByPeriod(period);
        if (!existing.isEmpty()) {
            result.put("success", false);
            result.put("message", "批次已存在");
            return ResponseEntity.badRequest().body(result);
        }

        // 创建新批次（不需要实际数据，只需确认批次存在）
        // 可以创建一个占位记录，或者不做任何操作，返回成功即可
        result.put("success", true);
        result.put("message", "添加成功");
        result.put("period", period);
        return ResponseEntity.ok(result);
    }

    // 修改图片上传接口
    @PutMapping("/{id}/inspection-image")
    public ResponseEntity<Map<String, Object>> updateInspectionImages(@PathVariable Integer id, @RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        Integer roleLevel = (Integer) session.getAttribute("roleLevel");
        if (roleLevel == null || (roleLevel != 0 && roleLevel != 1)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(403).body(result);
        }

        Inspection inspection = inspectionRepository.findById(id).orElse(null);
        if (inspection == null) {
            result.put("success", false);
            result.put("message", "记录不存在");
            return ResponseEntity.badRequest().body(result);
        }

        String base64Image = data.get("imageData");
        if (base64Image == null || base64Image.isEmpty()) {
            result.put("success", false);
            result.put("message", "图片数据为空");
            return ResponseEntity.badRequest().body(result);
        }

        // 删除旧图片（物理删除）
        if (inspection.getImageData() != null && !inspection.getImageData().isEmpty()) {
            String oldPath = imageUploadUtil.extractImagePath(inspection.getImageData());
            if (oldPath != null) {
                imageUploadUtil.deleteImage(oldPath);
            }
        }

        // 保存新图片，使用门店名作为文件名
        String savedPath = imageUploadUtil.saveInspectionImage(base64Image, inspection.getStoreName());
        if (savedPath != null) {
            inspection.setImageData(savedPath);
            inspectionRepository.save(inspection);
            result.put("success", true);
            result.put("message", "图片更新成功");
            result.put("path", savedPath);
        } else {
            result.put("success", false);
            result.put("message", "图片保存失败");
        }
        return ResponseEntity.ok(result);
    }

    // 添加图片删除接口
    @DeleteMapping("/{id}/inspection-image")
    public ResponseEntity<Map<String, Object>> deleteInspectionImage(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        Integer roleLevel = (Integer) session.getAttribute("roleLevel");
        if (roleLevel == null || (roleLevel != 0 && roleLevel != 1)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(403).body(result);
        }

        Inspection inspection = inspectionRepository.findById(id).orElse(null);
        if (inspection == null) {
            result.put("success", false);
            result.put("message", "记录不存在");
            return ResponseEntity.badRequest().body(result);
        }

        // 物理删除图片文件
        if (inspection.getImageData() != null && !inspection.getImageData().isEmpty()) {
            String imagePath = imageUploadUtil.extractImagePath(inspection.getImageData());
            if (imagePath != null) {
                imageUploadUtil.deleteImage(imagePath);
            }
            inspection.setImageData(null);
            inspectionRepository.save(inspection);
        }

        result.put("success", true);
        result.put("message", "图片删除成功");
        return ResponseEntity.ok(result);
    }
}