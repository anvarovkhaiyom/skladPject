package com.example.skladservicedevelop.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DriverResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String carNumber;
    private String carMark;
    private String additionalInfo;
    private BigDecimal salary;
    private Integer warehouseId;
    private String warehouseName;
}