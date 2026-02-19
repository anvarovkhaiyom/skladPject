package com.example.skladservicedevelop.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class StockSummaryResponse {
    private BigDecimal totalItems;
    private BigDecimal totalCostValue;
    private BigDecimal totalSaleValue;
    private BigDecimal potentialProfit;
}