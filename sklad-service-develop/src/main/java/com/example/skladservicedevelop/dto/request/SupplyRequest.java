package com.example.skladservicedevelop.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SupplyRequest {
    private Integer supplierId;
    private Integer employeeId;
    private List<SupplyItemRequest> items;
}