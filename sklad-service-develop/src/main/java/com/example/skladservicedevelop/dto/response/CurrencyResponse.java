package com.example.skladservicedevelop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyResponse {
    private Integer id;
    private String code;
    private String name;
    private Double rate;
    private LocalDateTime createdAt;
}