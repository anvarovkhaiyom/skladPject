package com.example.skladservicedevelop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesSummaryResponse {
    private BigDecimal totalRevenue;
    private BigDecimal totalCost;
    private BigDecimal totalProfit;
    private Long totalSalesCount;
    private List<SalesDataRow> details;
    private BigDecimal totalExpenses;
    private BigDecimal totalWriteOffCost;
}