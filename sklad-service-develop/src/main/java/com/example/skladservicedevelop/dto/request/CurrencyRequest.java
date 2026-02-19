package com.example.skladservicedevelop.dto.request;

import lombok.Data;

@Data
public class CurrencyRequest {
    private String code;
    private String name;
    private Double rate;
}