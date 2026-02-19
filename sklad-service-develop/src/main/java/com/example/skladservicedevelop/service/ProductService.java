package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.dto.request.ProductRequest;
import com.example.skladservicedevelop.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse create(ProductRequest request);
    ProductResponse update(Integer id, ProductRequest request);
    void delete(Integer id);
    ProductResponse getById(Integer id);
    List<ProductResponse> getAll(Integer warehouseId);
}