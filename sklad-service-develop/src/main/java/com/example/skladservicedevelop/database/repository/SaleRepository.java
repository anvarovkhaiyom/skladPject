package com.example.skladservicedevelop.database.repository;

import com.example.skladservicedevelop.database.model.SaleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<SaleModel, Integer> {
    @Query("SELECT s FROM SaleModel s " +
            "LEFT JOIN s.client c " +
            "LEFT JOIN s.employee e " +
            "WHERE s.saleDate BETWEEN :start AND :end " +
            "AND (:warehouseId IS NULL OR s.warehouse.id = :warehouseId) " +
            "AND (:client = '' OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :client, '%')) OR c.fullName IS NULL) " +
            "AND (:employee = '' OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :employee, '%')) OR e.fullName IS NULL)")
    List<SaleModel> findSalesWithFilters(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("warehouseId") Integer warehouseId,
            @Param("client") String client,
            @Param("employee") String employee
    );

    @Query("SELECT MAX(s.id) FROM SaleModel s WHERE s.warehouse.id = :warehouseId")
    Integer findMaxIdByWarehouseId(@Param("warehouseId") Integer warehouseId);

    List<SaleModel> findAllByWarehouseIdAndSaleDateBetween(Integer warehouseId, LocalDateTime start, LocalDateTime end);

    List<SaleModel> findAllBySaleDateBetween(LocalDateTime start, LocalDateTime end);

    List<SaleModel> findAllBySaleDateBetweenAndClientFullNameContainingIgnoreCaseAndEmployeeFullNameContainingIgnoreCase(
            LocalDateTime start, LocalDateTime end, String client, String employee
    );
}