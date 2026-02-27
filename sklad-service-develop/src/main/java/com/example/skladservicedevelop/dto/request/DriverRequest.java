package com.example.skladservicedevelop.dto.request;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class DriverRequest {
    private String fullName;
    private String phone;
    private String carNumber;
    private String carMark;
    private String additionalInfo;
    private BigDecimal salary;
    private Integer warehouseId;
}