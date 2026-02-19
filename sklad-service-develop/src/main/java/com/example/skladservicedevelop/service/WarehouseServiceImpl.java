package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.config.SecurityHelper;
import com.example.skladservicedevelop.database.model.CurrencyModel;
import com.example.skladservicedevelop.database.model.EmployeeModel;
import com.example.skladservicedevelop.database.model.WarehouseModel;
import com.example.skladservicedevelop.database.repository.CurrencyRepository;
import com.example.skladservicedevelop.database.repository.WarehouseRepository;
import com.example.skladservicedevelop.dto.request.WarehouseRequest;
import com.example.skladservicedevelop.dto.response.WarehouseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final CurrencyRepository currencyRepository;
    private final SecurityHelper securityHelper;

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getCurrentWarehouse() {
        EmployeeModel employee = securityHelper.getCurrentEmployee();

        if (employee.getWarehouse() == null) {
            throw new RuntimeException("Сотрудник не привязан к конкретному складу");
        }
        return toResponse(employee.getWarehouse());
    }

    @Override
    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        WarehouseModel warehouse = new WarehouseModel();
        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());

        if (request.getBaseCurrencyId() != null) {
            CurrencyModel currency = currencyRepository.findById(request.getBaseCurrencyId())
                    .orElseThrow(() -> new RuntimeException("Валюта не найдена"));
            warehouse.setBaseCurrency(currency);
        }

        warehouseRepository.save(warehouse);
        return toResponse(warehouse);
    }

    @Override
    @Transactional
    public WarehouseResponse update(Integer id, WarehouseRequest request) {
        WarehouseModel warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Склад не найден"));

        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());

        if (request.getBaseCurrencyId() != null) {
            CurrencyModel currency = currencyRepository.findById(request.getBaseCurrencyId())
                    .orElseThrow(() -> new RuntimeException("Валюта не найдена"));
            warehouse.setBaseCurrency(currency);
        }

        warehouseRepository.save(warehouse);
        return toResponse(warehouse);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        warehouseRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getById(Integer id) {
        WarehouseModel warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Склад не найден"));
        return toResponse(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAll() {
        return warehouseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private WarehouseResponse toResponse(WarehouseModel warehouse) {
        WarehouseResponse response = new WarehouseResponse();
        response.setId(warehouse.getId());
        response.setName(warehouse.getName());
        response.setAddress(warehouse.getAddress());

        if (warehouse.getBaseCurrency() != null) {
            response.setBaseCurrencyId(warehouse.getBaseCurrency().getId());
            response.setBaseCurrencyCode(warehouse.getBaseCurrency().getCode());
        }
        return response;
    }
}