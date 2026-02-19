package com.example.skladservicedevelop.dto.response;

import lombok.Data;

@Data
public class EmployeeResponse {
    private Integer id;
    private String fullName;
    private String position;
    private String role;
    private String login;
    private Integer warehouseId;
    private String warehouseName;
}