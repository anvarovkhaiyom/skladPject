package com.example.skladservicedevelop.database.repository;

import com.example.skladservicedevelop.database.model.SaleItemModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleItemRepository extends JpaRepository<SaleItemModel, Integer> {
}
