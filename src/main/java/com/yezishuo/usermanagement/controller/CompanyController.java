package com.yezishuo.usermanagement.controller;

import com.yezishuo.usermanagement.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/company")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getCompanyInfo() {
        return ResponseEntity.ok(companyService.getCompanyInfo());
    }

    @PutMapping("/info")
    public ResponseEntity<Map<String, Object>> updateCompanyInfo(@RequestBody Map<String, String> data) {
        Map<String, Object> result = companyService.updateCompanyInfo(data);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }
}
