package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.dto.request.SupplierRequest;
import com.example.skladservicedevelop.dto.response.SupplierResponse;

import java.util.List;

public interface SupplierService {
    SupplierResponse create(SupplierRequest request);
    SupplierResponse update(Integer id, SupplierRequest request);
    void delete(Integer id);
    SupplierResponse getById(Integer id);
    List<SupplierResponse> getAll();
}