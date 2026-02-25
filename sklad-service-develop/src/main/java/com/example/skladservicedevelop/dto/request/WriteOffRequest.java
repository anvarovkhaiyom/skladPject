package com.example.skladservicedevelop.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WriteOffRequest {
    private Integer productId;
    private BigDecimal quantity;
    private String reason;
}