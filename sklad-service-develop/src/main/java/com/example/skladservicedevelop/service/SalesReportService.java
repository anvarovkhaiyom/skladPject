package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.dto.response.SalesReportResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesReportService {
    List<SalesReportResponse> getSalesReport(LocalDateTime start, LocalDateTime end);
}
