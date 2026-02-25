package com.example.skladservicedevelop.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseRequest {
    private String category;
    private BigDecimal amount;
    private String description;
}