package com.example.skladservicedevelop.database.repository;

import com.example.skladservicedevelop.database.model.ClientModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<ClientModel, Integer> {
    List<ClientModel> findAllByWarehouseId(Integer warehouseId);
}
