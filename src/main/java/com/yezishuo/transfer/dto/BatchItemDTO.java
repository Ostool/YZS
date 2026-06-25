package com.yezishuo.transfer.dto;

public class BatchItemDTO {
    private String productType;
    private String brand;
    private String series;
    private String model;
    private String productName;
    private Integer quantity;

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
}
