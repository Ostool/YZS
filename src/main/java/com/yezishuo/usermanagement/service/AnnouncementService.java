package com.yezishuo.usermanagement.service;

import com.yezishuo.usermanagement.dto.*;
import com.yezishuo.usermanagement.entity.*;
import com.yezishuo.usermanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AnnouncementService {

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RulesNoteRepository rulesNoteRepository;

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private RewardConfigRepository rewardConfigRepository;

    @Autowired
    private FundItemRepository fundItemRepository;

    @Autowired
    private FundConfigRepository fundConfigRepository;

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private InspectConfigRepository inspectConfigRepository;

    @Autowired
    private CompanyConfigRepository companyConfigRepository;

    // ==================== 权限检查 ====================

    private boolean isAdmin(HttpSession session) {
        Integer roleLevel = (Integer) session.getAttribute("roleLevel");
        return roleLevel != null && (roleLevel == 0 || roleLevel == 1);
    }

    private boolean isSuperAdmin(HttpSession session) {
        Integer roleLevel = (Integer) session.getAttribute("roleLevel");
        return roleLevel != null && roleLevel == 0;
    }

    // ==================== 公司信息 ====================

    public Map<String, Object> getCompanyInfo() {
        Map<String, Object> result = new HashMap<>();

        Optional<CompanyConfig> desc = companyConfigRepository.findByConfigKey("company_desc");
        Optional<CompanyConfig> vision = companyConfigRepository.findByConfigKey("vision");
        Optional<CompanyConfig> mission = companyConfigRepository.findByConfigKey("mission");
        Optional<CompanyConfig> values = companyConfigRepository.findByConfigKey("values");

        result.put("companyDesc", desc.map(CompanyConfig::getConfigValue).orElse("零售门店主要分布在粤东地区"));
        result.put("vision", vision.map(CompanyConfig::getConfigValue).orElse("做潮汕一家有温度的眼镜公司"));
        result.put("mission", mission.map(CompanyConfig::getConfigValue).orElse("高性价比配镜服务商"));
        result.put("values", values.map(CompanyConfig::getConfigValue).orElse("有型 · 好用 · 不贵 · 让消费者获益"));

        return result;
    }

    @Transactional
    public Map<String, Object> updateCompanyInfo(Map<String, String> data, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        updateCompanyConfig("company_desc", data.get("companyDesc"));
        updateCompanyConfig("vision", data.get("vision"));
        updateCompanyConfig("mission", data.get("mission"));
        updateCompanyConfig("values", data.get("values"));

        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }

    private void updateCompanyConfig(String key, String value) {
        if (value != null) {
            Optional<CompanyConfig> existing = companyConfigRepository.findByConfigKey(key);
            if (existing.isPresent()) {
                CompanyConfig config = existing.get();
                config.setConfigValue(value);
                companyConfigRepository.save(config);
            } else {
                CompanyConfig config = new CompanyConfig();
                config.setConfigKey(key);
                config.setConfigValue(value);
                companyConfigRepository.save(config);
            }
        }
    }

    // ==================== 规章制度 ====================

    public Map<String, Object> getAllRules() {
        Map<String, Object> result = new HashMap<>();
        List<Rule> rules = ruleRepository.findAllOrdered();
        result.put("rules", rules);

        Optional<RulesNote> note = rulesNoteRepository.findById(1);
        result.put("rulesNote", note.map(RulesNote::getContent).orElse(""));
        result.put("rulesVersion", "2026年5月版");

        return result;
    }

    @Transactional
    public Map<String, Object> addRule(RuleDTO dto, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        Rule rule = new Rule();
        rule.setTitle(dto.getTitle());
        rule.setContent(dto.getContent());
        rule.setTag(dto.getTag());
        rule.setVersion(dto.getVersion());
        rule.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        ruleRepository.save(rule);

        result.put("success", true);
        result.put("message", "添加成功");
        result.put("data", rule);
        return result;
    }

    @Transactional
    public Map<String, Object> updateRule(Integer id, RuleDTO dto, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        Rule rule = ruleRepository.findById(id).orElse(null);
        if (rule == null) {
            result.put("success", false);
            result.put("message", "制度不存在");
            return result;
        }

        rule.setTitle(dto.getTitle());
        rule.setContent(dto.getContent());
        rule.setTag(dto.getTag());
        rule.setVersion(dto.getVersion());
        rule.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        ruleRepository.save(rule);

        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }

    @Transactional
    public Map<String, Object> deleteRule(Integer id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isSuperAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足，只有超级管理员可以删除");
            return result;
        }

        if (!ruleRepository.existsById(id)) {
            result.put("success", false);
            result.put("message", "制度不存在");
            return result;
        }

        ruleRepository.deleteById(id);
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }

    @Transactional
    public Map<String, Object> updateRulesNote(String content, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        Optional<RulesNote> noteOpt = rulesNoteRepository.findById(1);
        RulesNote note;
        if (noteOpt.isPresent()) {
            note = noteOpt.get();
            note.setContent(content);
        } else {
            note = new RulesNote();
            note.setId(1);
            note.setContent(content);
        }
        rulesNoteRepository.save(note);

        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }

    @Transactional
    public Map<String, Object> updateRulesVersion(String version, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }

    // ==================== 销售奖励 ====================

    public Map<String, Object> getAllRewards() {
        Map<String, Object> result = new HashMap<>();

        List<Reward> rewards = rewardRepository.findAllOrdered();
        result.put("rewards", rewards);

        Optional<RewardConfig> title = rewardConfigRepository.findByConfigKey("reward_title");
        Optional<RewardConfig> period = rewardConfigRepository.findByConfigKey("reward_period");
        Optional<RewardConfig> remark = rewardConfigRepository.findByConfigKey("reward_remark");
        Optional<RewardConfig> footerLeft = rewardConfigRepository.findByConfigKey("reward_footer_left");
        Optional<RewardConfig> footerRight = rewardConfigRepository.findByConfigKey("reward_footer_right");

        result.put("rewardTitle", title.map(RewardConfig::getConfigValue).orElse("《动态销奖激励方案》"));
        result.put("rewardPeriod", period.map(RewardConfig::getConfigValue).orElse("2026年4月1日~4月30日"));
        result.put("rewardRemark", remark.map(RewardConfig::getConfigValue).orElse(""));
        result.put("rewardFooterLeft", footerLeft.map(RewardConfig::getConfigValue).orElse("✅ 主推提醒：暴龙 | 明月 | 尼康 | NBA光学架"));
        result.put("rewardFooterRight", footerRight.map(RewardConfig::getConfigValue).orElse("📅 销奖周期：2026.4.1 - 2026.4.30"));

        return result;
    }

    @Transactional
    public Map<String, Object> updateRewardConfig(String key, String value, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        Optional<RewardConfig> existing = rewardConfigRepository.findByConfigKey(key);
        RewardConfig config;
        if (existing.isPresent()) {
            config = existing.get();
            config.setConfigValue(value);
        } else {
            config = new RewardConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
        }
        rewardConfigRepository.save(config);

        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }

    @Transactional
    public Map<String, Object> addReward(RewardDTO dto, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        Reward reward = new Reward();
        reward.setTitle(dto.getTitle());
        reward.setContent(dto.getContent());
        reward.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        rewardRepository.save(reward);

        result.put("success", true);
        result.put("message", "添加成功");
        return result;
    }

    @Transactional
    public Map<String, Object> updateReward(Integer id, RewardDTO dto, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        Reward reward = rewardRepository.findById(id).orElse(null);
        if (reward == null) {
            result.put("success", false);
            result.put("message", "奖励不存在");
            return result;
        }

        reward.setTitle(dto.getTitle());
        reward.setContent(dto.getContent());
        reward.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        rewardRepository.save(reward);

        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }

    @Transactional
    public Map<String, Object> deleteReward(Integer id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isSuperAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足，只有超级管理员可以删除");
            return result;
        }

        if (!rewardRepository.existsById(id)) {
            result.put("success", false);
            result.put("message", "奖励不存在");
            return result;
        }

        rewardRepository.deleteById(id);
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }

    // ==================== 团建基金 ====================

    public Map<String, Object> getAllFundItems() {
        Map<String, Object> result = new HashMap<>();

        List<FundItem> items = fundItemRepository.findAllOrdered();
        result.put("fundItems", items);

        Optional<FundConfig> note = fundConfigRepository.findByConfigKey("fund_note");
        result.put("fundNote", note.map(FundConfig::getConfigValue).orElse(""));

        // 计算总余额
        BigDecimal totalBalance = items.stream()
                .map(FundItem::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalBalance", totalBalance);

        return result;
    }

    @Transactional
    public Map<String, Object> addFundItem(FundItemDTO dto, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        FundItem item = new FundItem();
        item.setStoreName(dto.getStoreName());
        item.setDescription(dto.getDescription());
        item.setAmount(dto.getAmount());
        fundItemRepository.save(item);

        result.put("success", true);
        result.put("message", "添加成功");
        return result;
    }

    @Transactional
    public Map<String, Object> updateFundItem(Integer id, FundItemDTO dto, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        FundItem item = fundItemRepository.findById(id).orElse(null);
        if (item == null) {
            result.put("success", false);
            result.put("message", "明细不存在");
            return result;
        }

        item.setStoreName(dto.getStoreName());
        item.setDescription(dto.getDescription());
        item.setAmount(dto.getAmount());
        fundItemRepository.save(item);

        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }

    @Transactional
    public Map<String, Object> deleteFundItem(Integer id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isSuperAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足，只有超级管理员可以删除");
            return result;
        }

        if (!fundItemRepository.existsById(id)) {
            result.put("success", false);
            result.put("message", "明细不存在");
            return result;
        }

        fundItemRepository.deleteById(id);
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }

    @Transactional
    public Map<String, Object> updateFundNote(String content, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        Optional<FundConfig> configOpt = fundConfigRepository.findByConfigKey("fund_note");
        FundConfig config;
        if (configOpt.isPresent()) {
            config = configOpt.get();
            config.setConfigValue(content);
        } else {
            config = new FundConfig();
            config.setConfigKey("fund_note");
            config.setConfigValue(content);
        }
        fundConfigRepository.save(config);

        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }

    // ==================== 巡检记录 ====================

    public Map<String, Object> getAllInspections() {
        Map<String, Object> result = new HashMap<>();

        List<String> periods = inspectionRepository.findAllPeriods();
        result.put("periods", periods);

        if (!periods.isEmpty()) {
            String latestPeriod = periods.get(0);
            List<Inspection> inspections = inspectionRepository.findByPeriod(latestPeriod);
            result.put("currentPeriod", latestPeriod);
            result.put("inspections", inspections);
        } else {
            result.put("currentPeriod", "");
            result.put("inspections", new ArrayList<>());
        }

        Optional<InspectConfig> note = inspectConfigRepository.findByConfigKey("inspect_note");
        result.put("inspectNote", note.map(InspectConfig::getConfigValue).orElse("🧽 点击图片可放大查看详情"));

        return result;
    }

    public Map<String, Object> getInspectionsByPeriod(String period) {
        Map<String, Object> result = new HashMap<>();

        List<Inspection> inspections = inspectionRepository.findByPeriod(period);
        result.put("success", true);
        result.put("data", inspections);
        return result;
    }

    @Transactional
    public Map<String, Object> addInspection(InspectionDTO dto, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        Inspection inspection = new Inspection();
        inspection.setPeriod(dto.getPeriod());
        inspection.setStoreName(dto.getStoreName());
        inspection.setImageData(dto.getImageData());
        inspection.setDeadline(dto.getDeadline() != null ? LocalDate.parse(dto.getDeadline()) : null);
        inspection.setReviewDate(dto.getReviewDate() != null ? LocalDate.parse(dto.getReviewDate()) : null);
        inspection.setResult(dto.getResult());
        inspectionRepository.save(inspection);

        result.put("success", true);
        result.put("message", "添加成功");
        return result;
    }

    @Transactional
    public Map<String, Object> updateInspection(Integer id, InspectionDTO dto, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        Inspection inspection = inspectionRepository.findById(id).orElse(null);
        if (inspection == null) {
            result.put("success", false);
            result.put("message", "记录不存在");
            return result;
        }

        inspection.setStoreName(dto.getStoreName());
        if (dto.getImageData() != null) {
            inspection.setImageData(dto.getImageData());
        }
        inspection.setDeadline(dto.getDeadline() != null ? LocalDate.parse(dto.getDeadline()) : null);
        inspection.setReviewDate(dto.getReviewDate() != null ? LocalDate.parse(dto.getReviewDate()) : null);
        inspection.setResult(dto.getResult());
        inspectionRepository.save(inspection);

        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }

    @Transactional
    public Map<String, Object> updateInspectionImage(Integer id, String imageData, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        Inspection inspection = inspectionRepository.findById(id).orElse(null);
        if (inspection == null) {
            result.put("success", false);
            result.put("message", "记录不存在");
            return result;
        }

        inspection.setImageData(imageData);
        inspectionRepository.save(inspection);

        result.put("success", true);
        result.put("message", "图片更新成功");
        return result;
    }

    @Transactional
    public Map<String, Object> deleteInspection(Integer id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isSuperAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足，只有超级管理员可以删除");
            return result;
        }

        if (!inspectionRepository.existsById(id)) {
            result.put("success", false);
            result.put("message", "记录不存在");
            return result;
        }

        inspectionRepository.deleteById(id);
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }

    @Transactional
    public Map<String, Object> updateInspectNote(String content, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        Optional<InspectConfig> configOpt = inspectConfigRepository.findByConfigKey("inspect_note");
        InspectConfig config;
        if (configOpt.isPresent()) {
            config = configOpt.get();
            config.setConfigValue(content);
        } else {
            config = new InspectConfig();
            config.setConfigKey("inspect_note");
            config.setConfigValue(content);
        }
        inspectConfigRepository.save(config);

        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }
}