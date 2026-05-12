package com.yezishuo.usermanagement.controller;

import com.yezishuo.usermanagement.dto.RewardDTO;
import com.yezishuo.usermanagement.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class RewardController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRewards() {
        return ResponseEntity.ok(announcementService.getAllRewards());
    }

    @PutMapping("/config/{key}")
    public ResponseEntity<Map<String, Object>> updateRewardConfig(@PathVariable String key, @RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> result = announcementService.updateRewardConfig(key, data.get("value"), session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addReward(@Valid @RequestBody RewardDTO dto, HttpSession session) {
        Map<String, Object> result = announcementService.addReward(dto, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateReward(@PathVariable Integer id, @Valid @RequestBody RewardDTO dto, HttpSession session) {
        Map<String, Object> result = announcementService.updateReward(id, dto, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReward(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> result = announcementService.deleteReward(id, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }
}