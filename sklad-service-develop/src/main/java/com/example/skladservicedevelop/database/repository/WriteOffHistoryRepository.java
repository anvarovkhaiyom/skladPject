package com.example.skladservicedevelop.database.repository;

import com.example.skladservicedevelop.database.model.WriteOffHistoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WriteOffHistoryRepository extends JpaRepository<WriteOffHistoryModel, Integer> {
    List<WriteOffHistoryModel> findAllByWriteOffDateBetween(LocalDateTime start, LocalDateTime end);
    List<WriteOffHistoryModel> findAllByWarehouseIdAndWriteOffDateBetween(Integer warehouseId, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT w.* FROM write_off_history w WHERE w.warehouse_id = :warehouseId", nativeQuery = true)
    List<WriteOffHistoryModel> findByWarehouseIdNative(@Param("warehouseId") Integer warehouseId);

    @Query("SELECT MAX(w.id) FROM WriteOffHistoryModel w WHERE w.warehouse.id = :warehouseId")
    Integer findMaxIdByWarehouseId(@Param("warehouseId") Integer warehouseId);
}