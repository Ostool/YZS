package com.yezishuo.usermanagement.controller;

import com.yezishuo.usermanagement.dto.RuleDTO;
import com.yezishuo.usermanagement.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
public class RulesController {

    @Autowired
    private RuleService ruleService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRules() {
        return ResponseEntity.ok(ruleService.getAllRules());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addRule(@Valid @RequestBody RuleDTO dto) {
        Map<String, Object> result = ruleService.addRule(dto);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRule(@PathVariable Integer id, @Valid @RequestBody RuleDTO dto) {
        Map<String, Object> result = ruleService.updateRule(id, dto);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRule(@PathVariable Integer id) {
        Map<String, Object> result = ruleService.deleteRule(id);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }

    @PutMapping("/note")
    public ResponseEntity<Map<String, Object>> updateRulesNote(@RequestBody Map<String, String> data) {
        Map<String, Object> result = ruleService.updateRulesNote(data.get("content"));
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }

    @PutMapping("/version")
    public ResponseEntity<Map<String, Object>> updateRulesVersion(@RequestBody Map<String, String> data) {
        Map<String, Object> result = ruleService.updateRulesVersion(data.get("version"));
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }
}
