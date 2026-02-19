package com.example.skladservicedevelop.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaleItemResponse {
    private Integer productId;
    private String sku;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal boxCount;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}