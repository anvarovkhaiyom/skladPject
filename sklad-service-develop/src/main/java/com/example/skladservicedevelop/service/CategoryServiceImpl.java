package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.config.SecurityHelper;
import com.example.skladservicedevelop.database.model.CategoryModel;
import com.example.skladservicedevelop.database.model.EmployeeModel;
import com.example.skladservicedevelop.database.repository.CategoryRepository;
import com.example.skladservicedevelop.dto.request.CategoryRequest;
import com.example.skladservicedevelop.dto.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final SecurityHelper securityHelper;

    @Override
    public CategoryResponse create(CategoryRequest request) {
        EmployeeModel current = securityHelper.getCurrentEmployee();
        CategoryModel category = new CategoryModel();
        category.setName(request.getName());
        category.setWarehouse(current.getWarehouse());
        categoryRepository.save(category);
        return toResponse(category);
    }

    @Override
    public List<CategoryResponse> getAll() {
        EmployeeModel current = securityHelper.getCurrentEmployee();
        if ("SUPER_ADMIN".equals(current.getRole())) {
            return categoryRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
        }
        return categoryRepository.findAllByWarehouseId(current.getWarehouse().getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public CategoryResponse update(Integer id, CategoryRequest request) {
        CategoryModel category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(request.getName());
        categoryRepository.save(category);
        return toResponse(category);
    }

    @Override
    public void delete(Integer id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryResponse getById(Integer id) {
        CategoryModel category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return toResponse(category);
    }
    private CategoryResponse toResponse(CategoryModel category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        return response;
    }
}
