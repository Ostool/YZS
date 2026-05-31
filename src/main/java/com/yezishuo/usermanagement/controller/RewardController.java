package com.yezishuo.usermanagement.controller;

import com.yezishuo.usermanagement.dto.RewardDTO;
import com.yezishuo.usermanagement.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    @Autowired
    private RewardService rewardService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRewards() {
        return ResponseEntity.ok(rewardService.getAllRewards());
    }

    @PutMapping("/config/{key}")
    public ResponseEntity<Map<String, Object>> updateRewardConfig(@PathVariable String key, @RequestBody Map<String, String> data) {
        Map<String, Object> result = rewardService.updateRewardConfig(key, data.get("value"));
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addReward(@Valid @RequestBody RewardDTO dto) {
        Map<String, Object> result = rewardService.addReward(dto);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateReward(@PathVariable Integer id, @Valid @RequestBody RewardDTO dto) {
        Map<String, Object> result = rewardService.updateReward(id, dto);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReward(@PathVariable Integer id) {
        Map<String, Object> result = rewardService.deleteReward(id);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(403).body(result);
    }
}
