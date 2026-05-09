package com.yezishuo.usermanagement.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AddUserRequest {
    private Integer id;
    private String name;
    private String gender;
    private Integer age;
    private String shopName;
    private String phone;
    private String occupation;
    private String glassesPurpose;
    private String prescriptionDate;

    // 左眼
    private BigDecimal leftSphere;
    private BigDecimal leftCylinder;
    private Integer leftAxis;
    private BigDecimal leftAdd;
    private String leftUncorrectedVision;
    private String leftCorrectedVision;
    private BigDecimal leftPupilDistance;
    private BigDecimal leftPupilHeight;

    // 右眼
    private BigDecimal rightSphere;
    private BigDecimal rightCylinder;
    private Integer rightAxis;
    private BigDecimal rightAdd;
    private String rightUncorrectedVision;
    private String rightCorrectedVision;
    private BigDecimal rightPupilDistance;
    private BigDecimal rightPupilHeight;

    // 商品信息
    private String frameModel;
    private BigDecimal frameOriginalPrice;
    private BigDecimal frameActualPrice;
    private String lensType;
    private BigDecimal lensOriginalPrice;
    private BigDecimal lensActualPrice;
    private String otherItems;
    private BigDecimal otherCost;
    private BigDecimal totalAmount;
    private String consultant;
    private String needFollowup;
    private String remark;

    private String prescriptionImages;  // 处方图片路径
    private List<String> imageFiles;    // 上传的图片文件（Base64）

    private List<String> existingImages;  // 保留的已有图片路径
    private List<String> newImages;       // 新上传的Base64图片
}