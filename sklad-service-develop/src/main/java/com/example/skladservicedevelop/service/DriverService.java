package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.database.model.DriverModel;
import com.example.skladservicedevelop.dto.request.DriverRequest;
import com.example.skladservicedevelop.dto.response.DriverResponse;

import java.util.List;

public interface DriverService {
    List<DriverResponse> findAllNotDeleted(Integer warehouseId); // Возвращаем DTO
    void save(DriverRequest request);
    void update(Long id, DriverRequest request);
    void delete(Long id);
}