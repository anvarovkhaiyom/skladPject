package com.example.skladservicedevelop.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    private Integer categoryId;
    private String name;
    private BigDecimal costPrice;
    private String unit;
    private BigDecimal salePrice;
    private BigDecimal  stockQuantity;
    private String barcode;
    private String photoUrl;
    private Double weightBrutto;
    private String sku;
    private BigDecimal itemsInBox;
}
