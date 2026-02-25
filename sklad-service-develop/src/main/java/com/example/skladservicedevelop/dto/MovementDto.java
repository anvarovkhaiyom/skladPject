package com.example.skladservicedevelop.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MovementDto {
    private LocalDateTime date;
    private String type; // "ПРИХОД" или "РАСХОД"
    private String docNumber;
    private String counterparty; // Поставщик или Клиент
    private String employee;
    private BigDecimal amount;
    private String warehouse;
}