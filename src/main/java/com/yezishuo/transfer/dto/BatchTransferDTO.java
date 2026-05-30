package com.yezishuo.transfer.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BatchTransferDTO {
    private LocalDateTime transferTime;
    private String direction;
    private String directionDetail;
    private String pickupPerson;
    private String remark;
    private String filler;
    private List<BatchItemDTO> items;

    // Getters and Setters
    public LocalDateTime getTransferTime() { return transferTime; }
    public void setTransferTime(LocalDateTime transferTime) { this.transferTime = transferTime; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getDirectionDetail() { return directionDetail; }
    public void setDirectionDetail(String directionDetail) { this.directionDetail = directionDetail; }
    public String getPickupPerson() { return pickupPerson; }
    public void setPickupPerson(String pickupPerson) { this.pickupPerson = pickupPerson; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getFiller() { return filler; }
    public void setFiller(String filler) { this.filler = filler; }
    public List<BatchItemDTO> getItems() { return items; }
    public void setItems(List<BatchItemDTO> items) { this.items = items; }
}