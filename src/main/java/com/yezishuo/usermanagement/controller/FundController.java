package com.yezishuo.usermanagement.controller;

import com.yezishuo.usermanagement.dto.FundItemDTO;
import com.yezishuo.usermanagement.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/fund")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class FundController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFundItems() {
        return ResponseEntity.ok(announcementService.getAllFundItems());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addFundItem(@Valid @RequestBody FundItemDTO dto, HttpSession session) {
        Map<String, Object> result = announcementService.addFundItem(dto, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateFundItem(@PathVariable Integer id, @Valid @RequestBody FundItemDTO dto, HttpSession session) {
        Map<String, Object> result = announcementService.updateFundItem(id, dto, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFundItem(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> result = announcementService.deleteFundItem(id, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }

    @PutMapping("/note")
    public ResponseEntity<Map<String, Object>> updateFundNote(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> result = announcementService.updateFundNote(data.get("content"), session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }
}