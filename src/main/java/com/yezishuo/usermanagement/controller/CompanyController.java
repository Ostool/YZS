package com.yezishuo.usermanagement.controller;

import com.yezishuo.usermanagement.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/company")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class CompanyController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getCompanyInfo() {
        return ResponseEntity.ok(announcementService.getCompanyInfo());
    }

    @PutMapping("/info")
    public ResponseEntity<Map<String, Object>> updateCompanyInfo(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> result = announcementService.updateCompanyInfo(data, session);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body(result);
        }
    }
}