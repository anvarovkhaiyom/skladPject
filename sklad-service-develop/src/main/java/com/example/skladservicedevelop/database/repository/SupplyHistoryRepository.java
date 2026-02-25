package com.example.skladservicedevelop.database.repository;

import com.example.skladservicedevelop.database.model.SupplyHistoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SupplyHistoryRepository extends JpaRepository<SupplyHistoryModel, Integer> {

    List<SupplyHistoryModel> findAllBySupplyDateBetween(LocalDateTime start, LocalDateTime end);

    List<SupplyHistoryModel> findAllByWarehouseIdAndSupplyDateBetween(Integer warehouseId, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT s.document_number FROM supply_history s " +
            "WHERE s.warehouse_id = :whId " +
            "ORDER BY s.id DESC LIMIT 1", nativeQuery = true)
    String findLastDocumentNumber(@Param("whId") Integer whId);
}
