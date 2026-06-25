package com.yezishuo.usermanagement.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_data", indexes = {
    @Index(name = "idx_ud_deleted_shop_date", columnList = "is_deleted, shop_name, prescription_date"),
    @Index(name = "idx_ud_deleted_date", columnList = "is_deleted, prescription_date")
})
public class UserData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "serial_no")
    private String serialNo;

    private String name;
    private String gender;
    private Integer age;

    @Column(name = "shop_name")
    private String shopName;

    private String phone;
    private String occupation;

    @Column(name = "glasses_purpose")
    private String glassesPurpose;

    @Column(name = "prescription_date")
    private LocalDateTime prescriptionDate;

    // 左眼字段
    @Column(name = "left_sphere")
    private BigDecimal leftSphere;

    @Column(name = "left_cylinder")
    private BigDecimal leftCylinder;

    @Column(name = "left_axis")
    private Integer leftAxis;

    @Column(name = "left_add")
    private BigDecimal leftAdd;

    @Column(name = "left_uncorrected_vision")
    private String leftUncorrectedVision;

    @Column(name = "left_corrected_vision")
    private String leftCorrectedVision;

    @Column(name = "left_pupil_distance")
    private BigDecimal leftPupilDistance;

    @Column(name = "left_pupil_height")
    private BigDecimal leftPupilHeight;

    // 右眼字段
    @Column(name = "right_sphere")
    private BigDecimal rightSphere;

    @Column(name = "right_cylinder")
    private BigDecimal rightCylinder;

    @Column(name = "right_axis")
    private Integer rightAxis;

    @Column(name = "right_add")
    private BigDecimal rightAdd;

    @Column(name = "right_uncorrected_vision")
    private String rightUncorrectedVision;

    @Column(name = "right_corrected_vision")
    private String rightCorrectedVision;

    @Column(name = "right_pupil_distance")
    private BigDecimal rightPupilDistance;

    @Column(name = "right_pupil_height")
    private BigDecimal rightPupilHeight;

    // 商品信息
    @Column(name = "frame_model")
    private String frameModel;

    @Column(name = "frame_original_price")
    private BigDecimal frameOriginalPrice;

    @Column(name = "frame_actual_price")
    private BigDecimal frameActualPrice;

    @Column(name = "lens_type")
    private String lensType;

    @Column(name = "lens_original_price")
    private BigDecimal lensOriginalPrice;

    @Column(name = "lens_actual_price")
    private BigDecimal lensActualPrice;

    @Column(name = "other_items")
    private String otherItems;

    @Column(name = "other_cost")
    private BigDecimal otherCost;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    private String consultant;

    @Column(name = "need_followup")
    private String needFollowup;

    private String remark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "frame_brand")
    private String frameBrand;

    @Column(name = "frame_series")
    private String frameSeries;

    @Column(name = "lens_brand")
    private String lensBrand;

    @Column(name = "lens_series")
    private String lensSeries;

// 添加对应的 getter/setter (Lombok 会自动生成)

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (occupation == null) occupation = "无";
        if (needFollowup == null) needFollowup = "否";
        if (prescriptionDate == null) prescriptionDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Column(name = "prescription_images")
    private String prescriptionImages;  // 存储图片路径，多个用逗号分隔

    @Column(name = "is_deleted", columnDefinition = "INT DEFAULT 0")
    private Integer isDeleted = 0;  // 0=未删除, 1=已删除

    @Column(name = "redemption_channel", length = 20)
    private String redemptionChannel;

    @Column(name = "has_good_review", columnDefinition = "TINYINT DEFAULT 0")
    private Integer hasGoodReview = 0;  // 0=无好评, 1=有好评
}