package com.yezishuo.usermanagement.controller;

import com.yezishuo.usermanagement.dto.FundItemDTO;
import com.yezishuo.usermanagement.service.FundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/fund")
public class FundController {

    @Autowired
    private FundService fundService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFundItems() {
        return ResponseEntity.ok(fundService.getAllFundItems());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addFundItem(@Valid @RequestBody FundItemDTO dto) {
        Map<String, Object> result = fundService.addFundItem(dto);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateFundItem(@PathVariable Integer id, @Valid @RequestBody FundItemDTO dto) {
        Map<String, Object> result = fundService.updateFundItem(id, dto);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFundItem(@PathVariable Integer id) {
        Map<String, Object> result = fundService.deleteFundItem(id);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }

    @PutMapping("/note")
    public ResponseEntity<Map<String, Object>> updateFundNote(@RequestBody Map<String, String> data) {
        Map<String, Object> result = fundService.updateFundNote(data.get("content"));
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }
}
