package com.example.skladservicedevelop.database.repository;

import com.example.skladservicedevelop.database.model.ExpenseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<ExpenseModel, Integer> {
    // Для общего отчета (все склады)
    List<ExpenseModel> findAllByExpenseDateBetween(LocalDateTime start, LocalDateTime end);

    // Для отчета по конкретному складу
    List<ExpenseModel> findAllByWarehouseIdAndExpenseDateBetween(Integer warehouseId, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT e.* FROM expenses e WHERE e.warehouse_id = :warehouseId", nativeQuery = true)
    List<ExpenseModel> findByWarehouseIdNative(@Param("warehouseId") Integer warehouseId);
    @Query("SELECT MAX(e.id) FROM ExpenseModel e WHERE e.warehouse.id = :warehouseId")
    Integer findMaxIdByWarehouseId(@Param("warehouseId") Integer warehouseId);
}