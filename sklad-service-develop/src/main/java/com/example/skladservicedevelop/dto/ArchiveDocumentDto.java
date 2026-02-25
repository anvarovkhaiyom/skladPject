package com.example.skladservicedevelop.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArchiveDocumentDto {
    private Integer id;
    private String type;
    private String documentNumber;
    private LocalDateTime date;
    private String counterparty;
    private BigDecimal amount;
}