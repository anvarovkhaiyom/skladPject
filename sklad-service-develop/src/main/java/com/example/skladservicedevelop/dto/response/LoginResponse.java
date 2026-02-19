package com.example.skladservicedevelop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String role;
    private String fullName;
    private Integer warehouseId;
    private String warehouseName;
}