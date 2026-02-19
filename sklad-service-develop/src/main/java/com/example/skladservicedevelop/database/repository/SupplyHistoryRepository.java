package com.example.skladservicedevelop.database.repository;

import com.example.skladservicedevelop.database.model.SupplyHistoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplyHistoryRepository extends JpaRepository<SupplyHistoryModel, Integer> {
}
