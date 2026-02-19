package com.example.skladservicedevelop.database.repository;

import com.example.skladservicedevelop.database.model.CurrencyModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<CurrencyModel, Integer> {
    Optional<CurrencyModel> findByCode(String code);
}
