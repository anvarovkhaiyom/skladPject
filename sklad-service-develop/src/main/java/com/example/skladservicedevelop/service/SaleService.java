package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.dto.request.SaleRequest;
import com.example.skladservicedevelop.dto.response.SaleResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleService {
    SaleResponse createSale(SaleRequest request);

    SaleResponse getById(Integer id);

    List<SaleResponse> getAll();

    List<SaleResponse> findSalesByFilters(LocalDateTime start, LocalDateTime end, String client, String employee, Integer warehouseId);
}