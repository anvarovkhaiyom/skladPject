package com.example.skladservicedevelop.database.repository;

import com.example.skladservicedevelop.database.model.EmployeeModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<EmployeeModel, Integer> {
    Optional<EmployeeModel> findByLogin(String login);

    List<EmployeeModel> findAllByWarehouseId(Integer warehouseId);
}