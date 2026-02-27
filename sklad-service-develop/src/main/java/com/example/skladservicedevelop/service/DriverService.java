package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.dto.request.DriverRequest;
import com.example.skladservicedevelop.dto.response.DriverResponse;

import java.util.List;

public interface DriverService {
    List<DriverResponse> findAllNotDeleted(Integer warehouseId);
    void save(DriverRequest request);
    void update(Long id, DriverRequest request);
    void delete(Long id);
}