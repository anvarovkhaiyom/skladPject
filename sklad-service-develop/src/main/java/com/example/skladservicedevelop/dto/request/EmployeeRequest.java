package com.example.skladservicedevelop.dto.request;


import lombok.Data;

@Data
public class EmployeeRequest {
    private String fullName;
    private String position;
    private String role;
    private String login;
    private String password;
    private Integer warehouseId;
}