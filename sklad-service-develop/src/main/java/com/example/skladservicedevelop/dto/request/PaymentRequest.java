package com.example.skladservicedevelop.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private BigDecimal amount;
    private String method;
    private String currency;
    private BigDecimal rate;
}
