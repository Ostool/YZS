package com.yezishuo.usermanagement.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FundItemDTO {
    private Integer id;
    private String storeName;
    private String description;
    private BigDecimal amount;
}