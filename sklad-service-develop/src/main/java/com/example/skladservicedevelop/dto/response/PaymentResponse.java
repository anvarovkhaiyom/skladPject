package com.example.skladservicedevelop.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentResponse {
    private String method;
    private BigDecimal amount;
    private String currencyCode;
    private BigDecimal exchangeRate;
}
