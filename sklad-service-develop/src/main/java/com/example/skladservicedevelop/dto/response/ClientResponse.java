package com.example.skladservicedevelop.dto.response;

import lombok.Data;

@Data
public class ClientResponse {
    private Integer id;
    private String fullName;
    private String contacts;
    private Integer warehouseId;
    private String warehouseName;
}