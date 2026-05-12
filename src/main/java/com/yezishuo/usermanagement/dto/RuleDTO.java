package com.yezishuo.usermanagement.dto;

import lombok.Data;

@Data
public class RuleDTO {
    private Integer id;
    private String title;
    private String content;
    private String tag;
    private String version;
    private Integer sortOrder;
}