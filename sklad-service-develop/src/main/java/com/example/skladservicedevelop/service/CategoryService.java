package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.dto.request.CategoryRequest;
import com.example.skladservicedevelop.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(Integer id, CategoryRequest request);
    void delete(Integer id);
    CategoryResponse getById(Integer id);
    List<CategoryResponse> getAll();
}
