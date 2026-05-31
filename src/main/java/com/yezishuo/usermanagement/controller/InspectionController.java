package com.yezishuo.usermanagement.controller;

import com.yezishuo.usermanagement.dto.InspectionDTO;
import com.yezishuo.usermanagement.entity.Inspection;
import com.yezishuo.usermanagement.repository.InspectionRepository;
import com.yezishuo.usermanagement.service.InspectionService;
import com.yezishuo.usermanagement.util.ImageUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inspections")
public class InspectionController {

    @Autowired
    private InspectionService inspectionService;

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private ImageUploadUtil imageUploadUtil;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllInspections() {
        return ResponseEntity.ok(inspectionService.getAllInspections());
    }

    @GetMapping("/period/{period}")
    public ResponseEntity<Map<String, Object>> getInspectionsByPeriod(@PathVariable String period) {
        return ResponseEntity.ok(inspectionService.getInspectionsByPeriod(period));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addInspection(@Valid @RequestBody InspectionDTO dto) {
        Map<String, Object> result = inspectionService.addInspection(dto);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateInspection(@PathVariable Integer id, @Valid @RequestBody InspectionDTO dto) {
        Map<String, Object> result = inspectionService.updateInspection(id, dto);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }

    @PutMapping("/{id}/image")
    public ResponseEntity<Map<String, Object>> updateInspectionImage(@PathVariable Integer id, @RequestBody Map<String, String> data) {
        Map<String, Object> result = inspectionService.updateInspectionImage(id, data.get("imageData"));
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteInspection(@PathVariable Integer id) {
        Map<String, Object> result = inspectionService.deleteInspection(id);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }

    @PutMapping("/note")
    public ResponseEntity<Map<String, Object>> updateInspectNote(@RequestBody Map<String, String> data) {
        Map<String, Object> result = inspectionService.updateInspectNote(data.get("content"));
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }

    @PostMapping("/period")
    @PreAuthorize("hasAnyRole('SUPER','NORMAL')")
    public ResponseEntity<Map<String, Object>> addInspectPeriod(@RequestBody Map<String, String> data) {
        Map<String, Object> result = new HashMap<>();

        String period = data.get("period");
        if (period == null || period.isEmpty()) {
            result.put("success", false);
            result.put("message", "请输入批次日期");
            return ResponseEntity.badRequest().body(result);
        }

        List<Inspection> existing = inspectionRepository.findByPeriod(period);
        if (!existing.isEmpty()) {
            result.put("success", false);
            result.put("message", "批次已存在");
            return ResponseEntity.badRequest().body(result);
        }

        result.put("success", true);
        result.put("message", "添加成功");
        result.put("period", period);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/inspection-image")
    @PreAuthorize("hasAnyRole('SUPER','NORMAL')")
    public ResponseEntity<Map<String, Object>> updateInspectionImages(@PathVariable Integer id, @RequestBody Map<String, String> data) {
        Map<String, Object> result = new HashMap<>();

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

        if (inspection.getImageData() != null && !inspection.getImageData().isEmpty()) {
            String oldPath = imageUploadUtil.extractImagePath(inspection.getImageData());
            if (oldPath != null) {
                imageUploadUtil.deleteImage(oldPath);
            }
        }

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

    @DeleteMapping("/{id}/inspection-image")
    @PreAuthorize("hasAnyRole('SUPER','NORMAL')")
    public ResponseEntity<Map<String, Object>> deleteInspectionImage(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();

        Inspection inspection = inspectionRepository.findById(id).orElse(null);
        if (inspection == null) {
            result.put("success", false);
            result.put("message", "记录不存在");
            return ResponseEntity.badRequest().body(result);
        }

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
