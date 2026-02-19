package com.example.skladservicedevelop.dto.response;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SalesReportResponse {
    private Integer saleId;
    private LocalDateTime saleDate;
    private String employeeName;
    private String clientName;
    private BigDecimal totalAmount;
    private BigDecimal profit;
    private List<SalesReportItem> items;
    private List<PaymentResponse> payments;

    @Data
    public static class SalesReportItem {
        private String productName;
        private String categoryName;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private BigDecimal profit;
    }
}