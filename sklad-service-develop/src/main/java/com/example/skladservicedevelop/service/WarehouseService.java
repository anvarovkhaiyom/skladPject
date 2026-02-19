package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.dto.request.WarehouseRequest;
import com.example.skladservicedevelop.dto.response.WarehouseResponse;

import java.util.List;

public interface WarehouseService {
    WarehouseResponse create(WarehouseRequest request);
    WarehouseResponse update(Integer id, WarehouseRequest request);
    void delete(Integer id);
    WarehouseResponse getById(Integer id);
    List<WarehouseResponse> getAll();
    WarehouseResponse getCurrentWarehouse();
}