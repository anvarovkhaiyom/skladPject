package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.config.SecurityHelper;
import com.example.skladservicedevelop.database.model.CategoryModel;
import com.example.skladservicedevelop.database.model.EmployeeModel;
import com.example.skladservicedevelop.database.model.ProductModel;
import com.example.skladservicedevelop.database.repository.CategoryRepository;
import com.example.skladservicedevelop.database.repository.ProductRepository;
import com.example.skladservicedevelop.dto.request.ProductRequest;
import com.example.skladservicedevelop.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SecurityHelper securityHelper;

    @Override
    public ProductResponse create(ProductRequest request) {
        EmployeeModel current = securityHelper.getCurrentEmployee();
        ProductModel product = new ProductModel();
        product.setName(request.getName());
        product.setCostPrice(request.getCostPrice());
        product.setUnit(request.getUnit());
        product.setSalePrice(request.getSalePrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setBarcode(request.getBarcode());
        product.setPhotoUrl(request.getPhotoUrl());
        product.setWarehouse(current.getWarehouse());

        if (request.getSku() == null || request.getSku().isEmpty()) {
            Integer lastId = productRepository.findMaxIdByWarehouseId(current.getWarehouse().getId());
            int nextId = (lastId == null) ? 1 : lastId + 1;
            product.setSku(String.format("НФ-%07d", nextId));
        }else {
            product.setSku(request.getSku());
        }

        product.setItemsInBox(request.getItemsInBox());
        product.setWeightBrutto(request.getWeightBrutto());

        if (request.getCategoryId() != null) {
            CategoryModel category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        productRepository.save(product);
        return toResponse(product);
    }

    @Override
    public ProductResponse update(Integer id, ProductRequest request) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setName(request.getName());
        product.setCostPrice(request.getCostPrice());
        product.setUnit(request.getUnit());
        product.setSalePrice(request.getSalePrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setBarcode(request.getBarcode());
        product.setPhotoUrl(request.getPhotoUrl());
        product.setSku(request.getSku());
        product.setItemsInBox(request.getItemsInBox());

        product.setWeightBrutto(request.getWeightBrutto());

        if (request.getCategoryId() != null) {
            CategoryModel category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        productRepository.save(product);
        return toResponse(product);
    }
    @Override
    @Transactional
    public void delete(Integer id) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));
        product.setDeleted(true);
        productRepository.save(product);
    }

    @Override
    public ProductResponse getById(Integer id) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toResponse(product);
    }

    @Override
    public List<ProductResponse> getAll(Integer warehouseId) {
        EmployeeModel current = securityHelper.getCurrentEmployee();
        String role = current.getRole();

        List<ProductModel> products;

        if (role.contains("SUPER_ADMIN")) {
            if (warehouseId != null) {
                products = productRepository.findAllByWarehouseIdAndDeletedFalse(warehouseId);
            } else {
                products = productRepository.findAllByDeletedFalse();
            }
        } else {
            Integer myWarehouseId = current.getWarehouse().getId();
            products = productRepository.findAllByWarehouseIdAndDeletedFalse(myWarehouseId);
        }

        return products.stream().map(this::toResponse).collect(Collectors.toList());
    }
    private ProductResponse toResponse(ProductModel product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());

        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }

        if (product.getWarehouse() != null) {
            response.setWarehouseId(product.getWarehouse().getId());
            response.setWarehouseName(product.getWarehouse().getName());
        }

        response.setName(product.getName());
        response.setSku(product.getSku());
        response.setWeightBrutto(product.getWeightBrutto());
        response.setItemsInBox(product.getItemsInBox());
        response.setCostPrice(product.getCostPrice());
        response.setUnit(product.getUnit());
        response.setSalePrice(product.getSalePrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setBarcode(product.getBarcode());
        response.setPhotoUrl(product.getPhotoUrl());

        return response;
    }
}