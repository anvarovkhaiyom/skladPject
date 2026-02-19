package com.example.skladservicedevelop.database.repository;

import com.example.skladservicedevelop.database.model.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductModel, Integer> {
    List<ProductModel> findAllByWarehouseId(Integer warehouseId);
    @Query("SELECT p FROM ProductModel p WHERE p.warehouse.id = :warehouseId AND p.deleted = false")
    List<ProductModel> findAllActiveByWarehouseId(@Param("warehouseId") Integer warehouseId);
    Optional<ProductModel> findByBarcodeAndWarehouseId(String barcode, Integer warehouseId);
    Optional<ProductModel> findBySkuAndWarehouseId(String sku, Integer warehouseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductModel p WHERE p.id = :id AND p.warehouse.id = :warehouseId")
    Optional<ProductModel> findByIdForUpdateAndWarehouseId(@Param("id") Integer id, @Param("warehouseId") Integer warehouseId);

    @Query("SELECT MAX(p.id) FROM ProductModel p WHERE p.warehouse.id = :warehouseId")
    Integer findMaxIdByWarehouseId(@Param("warehouseId") Integer warehouseId);
    List<ProductModel> findAllByWarehouseIdAndDeletedFalse(Integer warehouseId);

    List<ProductModel> findAllByDeletedFalse();
}