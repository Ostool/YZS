package com.yezishuo.transfer.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_record")
public class TransferRecord {

    @Id
    @Column(length = 32)
    private String id;

    @Column(name = "transfer_time")
    private LocalDateTime transferTime;

    @Column(nullable = false, length = 10)
    private String direction;

    @Column(name = "direction_detail", nullable = false, length = 100)
    private String directionDetail;

    @Column(name = "pickup_person", nullable = false, length = 50)
    private String pickupPerson;

    @Column(name = "product_type", length = 20)
    private String productType;

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String series;

    @Column(length = 100)
    private String model;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 200)
    private String remark;

    @Column(nullable = false, length = 50)
    private String filler;

    @Column(length = 20)
    private String status = "active";

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "create_by", length = 50)
    private String createBy;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getTransferTime() { return transferTime; }
    public void setTransferTime(LocalDateTime transferTime) { this.transferTime = transferTime; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getDirectionDetail() { return directionDetail; }
    public void setDirectionDetail(String directionDetail) { this.directionDetail = directionDetail; }

    public String getPickupPerson() { return pickupPerson; }
    public void setPickupPerson(String pickupPerson) { this.pickupPerson = pickupPerson; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getFiller() { return filler; }
    public void setFiller(String filler) { this.filler = filler; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
}
