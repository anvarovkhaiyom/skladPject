package com.example.skladservicedevelop.database.repository;

import com.example.skladservicedevelop.database.model.SupplierModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRepository extends JpaRepository<SupplierModel, Integer> {
    List<SupplierModel> findAllByWarehouseId(Integer warehouseId);
}
