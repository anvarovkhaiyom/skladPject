package com.example.skladservicedevelop.database.repository;

import com.example.skladservicedevelop.database.model.SalePaymentModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalePaymentRepository extends JpaRepository<SalePaymentModel, Integer> {
    List<SalePaymentModel> findBySaleId(Integer saleId);
}

