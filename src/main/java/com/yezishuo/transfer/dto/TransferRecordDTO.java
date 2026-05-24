package com.yezishuo.transfer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRecordDTO {
    private String id;
    private LocalDateTime transferTime;
    private String direction;
    private String directionDetail;
    private String pickupPerson;
    private String productName;
    private Integer quantity;
    private String remark;
    private String filler;
}