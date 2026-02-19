package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.dto.request.EmployeeRequest;
import com.example.skladservicedevelop.dto.response.EmployeeResponse;

import java.util.List;

public interface EmployeeService {
    EmployeeResponse create(EmployeeRequest request);
    EmployeeResponse update(Integer id, EmployeeRequest request);
    void delete(Integer id);
    EmployeeResponse getById(Integer id);
    List<EmployeeResponse> getAll(Integer warehouseId);
}