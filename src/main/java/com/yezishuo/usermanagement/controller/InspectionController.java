package com.yezishuo.usermanagement.controller;

import com.yezishuo.usermanagement.dto.InspectionDTO;
import com.yezishuo.usermanagement.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/inspections")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class InspectionController {

    @Autowired
    private AnnouncementService announcementService;

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
}