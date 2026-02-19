package com.example.skladservicedevelop.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SaleRequest {
    private Integer employeeId;
    private Integer clientId;
    private String documentNumber;

    private String carMark;
    private String carNumber;
    private String driverName;

    private String proxyNumber;
    private LocalDate proxyDate;

    private List<PaymentRequest> payments;
    private List<SaleItemRequest> items;
}