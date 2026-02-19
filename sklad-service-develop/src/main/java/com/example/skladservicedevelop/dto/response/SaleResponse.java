package com.example.skladservicedevelop.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaleResponse {
    private Integer id;
    private LocalDateTime saleDate;
    private BigDecimal totalAmount;
    private BigDecimal changeAmount;
    private String status;
    private Integer clientId;
    private String clientName;
    private Integer employeeId;
    private List<SaleItemResponse> items;
    private List<PaymentResponse> payments;
    private String documentNumber;
}