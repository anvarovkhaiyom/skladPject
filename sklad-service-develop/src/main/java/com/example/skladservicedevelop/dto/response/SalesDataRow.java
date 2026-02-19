package com.example.skladservicedevelop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SalesDataRow {
    private String date;
    private String documentNumber;
    private String clientName;
    private BigDecimal revenue;
    private BigDecimal cost;
    private BigDecimal profit;
}