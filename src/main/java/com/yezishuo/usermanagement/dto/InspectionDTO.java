package com.yezishuo.usermanagement.dto;

import lombok.Data;

@Data
public class InspectionDTO {
    private Integer id;
    private String period;
    private String storeName;
    private String imageData;
    private String deadline;
    private String reviewDate;
    private String result;
}