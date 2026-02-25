package com.example.skladservicedevelop.database.repository;

import com.example.skladservicedevelop.database.model.DriverModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DriverRepository extends JpaRepository<DriverModel, Long> {
    List<DriverModel> findAllByDeletedFalse();
    List<DriverModel> findAllByDeletedFalseAndWarehouseId(Integer warehouseId);

    boolean existsByPhone(String phone);
    boolean existsByCarNumber(String carNumber);

    boolean existsByPhoneAndIdNot(String phone, Long id);
    boolean existsByCarNumberAndIdNot(String carNumber, Long id);
}