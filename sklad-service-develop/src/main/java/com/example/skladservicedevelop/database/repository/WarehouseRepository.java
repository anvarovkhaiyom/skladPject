package com.example.skladservicedevelop.database.repository;

import com.example.skladservicedevelop.database.model.WarehouseModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<WarehouseModel, Integer> {
}