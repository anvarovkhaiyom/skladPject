package com.example.skladservicedevelop.dto.response;

import lombok.Data;

@Data
public class WarehouseResponse {
    private Integer id;
    private String name;
    private String address;
    private Integer baseCurrencyId;
    private String baseCurrencyCode;
}