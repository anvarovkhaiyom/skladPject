package com.example.skladservicedevelop.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponse {
    private Integer id;
    private Integer categoryId;
    private String categoryName;
    private Integer warehouseId;
    private String warehouseName;
    private String name;
    private BigDecimal costPrice;
    private String unit;
    private BigDecimal salePrice;
    private BigDecimal stockQuantity;
    private String barcode;
    private String photoUrl;
    private String sku;
    private BigDecimal itemsInBox;
    private Double weightBrutto;
}