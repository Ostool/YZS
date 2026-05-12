package com.yezishuo.usermanagement.controller;

import com.yezishuo.usermanagement.dto.RuleDTO;
import com.yezishuo.usermanagement.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class RulesController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRules() {
        return ResponseEntity.ok(announcementService.getAllRules());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addRule(@Valid @RequestBody RuleDTO dto, HttpSession session) {
        Map<String, Object> result = announcementService.addRule(dto, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRule(@PathVariable Integer id, @Valid @RequestBody RuleDTO dto, HttpSession session) {
        Map<String, Object> result = announcementService.updateRule(id, dto, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRule(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> result = announcementService.deleteRule(id, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }

    @PutMapping("/note")
    public ResponseEntity<Map<String, Object>> updateRulesNote(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> result = announcementService.updateRulesNote(data.get("content"), session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }

    @PutMapping("/version")
    public ResponseEntity<Map<String, Object>> updateRulesVersion(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> result = announcementService.updateRulesVersion(data.get("version"), session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }
}