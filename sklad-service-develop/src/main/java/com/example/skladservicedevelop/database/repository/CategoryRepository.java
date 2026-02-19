package com.example.skladservicedevelop.database.repository;

import com.example.skladservicedevelop.database.model.CategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryModel, Integer> {
    List<CategoryModel> findAllByWarehouseId(Integer warehouseId);
}
