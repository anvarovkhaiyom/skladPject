package com.example.skladservicedevelop.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SupplyItemRequest {
    private Integer productId;
    private String barcode;
    private BigDecimal quantity;
    private BigDecimal costPrice;
    private String sku;
}
