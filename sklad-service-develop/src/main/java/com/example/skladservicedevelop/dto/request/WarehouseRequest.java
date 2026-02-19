package com.example.skladservicedevelop.dto.request;

import lombok.Data;

@Data
public class WarehouseRequest {
    private String name;
    private String address;
    private Integer baseCurrencyId;
}