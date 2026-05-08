package com.example.usermanagement.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class AddUserRequest {
    // 基本信息
    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "性别不能为空")
    private String gender;

    @NotNull(message = "年龄不能为空")
    private Integer age;

    @NotBlank(message = "店名不能为空")
    private String shopName;

    // 新增基本信息（非必填）
    private String phone;
    private String occupation;
    private String glassesPurpose;

    // 左眼字段（非必填）
    private BigDecimal leftSphere;
    private BigDecimal leftCylinder;
    private Integer leftAxis;
    private BigDecimal leftAdd;
    private String leftUncorrectedVision;
    private String leftCorrectedVision;
    private BigDecimal leftPupilDistance;
    private BigDecimal leftPupilHeight;

    // 右眼字段（非必填）
    private BigDecimal rightSphere;
    private BigDecimal rightCylinder;
    private Integer rightAxis;
    private BigDecimal rightAdd;
    private String rightUncorrectedVision;
    private String rightCorrectedVision;
    private BigDecimal rightPupilDistance;
    private BigDecimal rightPupilHeight;

    // 商品信息（非必填）
    private String frameModel;
    private BigDecimal frameOriginalPrice;
    private BigDecimal frameDiscount;
    private BigDecimal frameFinalPrice;
    private String lensType;
    private BigDecimal lensOriginalPrice;
    private BigDecimal lensDiscount;
    private BigDecimal lensFinalPrice;
    private String otherItems;
    private BigDecimal totalAmount;
    private String consultant;
    private String needFollowup;
    private String remark;
    private BigDecimal otherCost;
    private Integer id;  // 用于编辑时标识记录
}