package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.config.SecurityHelper;
import com.example.skladservicedevelop.database.model.EmployeeModel;
import com.example.skladservicedevelop.database.model.WarehouseModel;
import com.example.skladservicedevelop.database.repository.EmployeeRepository;
import com.example.skladservicedevelop.database.repository.WarehouseRepository;
import com.example.skladservicedevelop.dto.request.EmployeeRequest;
import com.example.skladservicedevelop.dto.response.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecurityHelper securityHelper;
    private final WarehouseRepository warehouseRepository;


    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        EmployeeModel employee = new EmployeeModel();
        employee.setFullName(request.getFullName());
        employee.setPosition(request.getPosition());
        employee.setRole(request.getRole());
        employee.setLogin(request.getLogin());
        employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        Integer targetWarehouseId = request.getWarehouseId();
        if (targetWarehouseId == null) {
            EmployeeModel creator = securityHelper.getCurrentEmployee();
            if (creator.getWarehouse() != null) {
                targetWarehouseId = creator.getWarehouse().getId();
            }
        }

        if (targetWarehouseId != null) {
            WarehouseModel warehouse = warehouseRepository.findById(targetWarehouseId)
                    .orElseThrow(() -> new RuntimeException("Склад не найден"));
            employee.setWarehouse(warehouse);
        }

        employeeRepository.save(employee);
        return toResponse(employee);
    }

    @Override
    public EmployeeResponse update(Integer id, EmployeeRequest request) {
        EmployeeModel employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setFullName(request.getFullName());
        employee.setPosition(request.getPosition());
        employee.setRole(request.getRole());
        employee.setLogin(request.getLogin());
        employee.setFullName(request.getFullName());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        employeeRepository.save(employee);
        return toResponse(employee);
    }

    @Override
    public void delete(Integer id) {
        employeeRepository.deleteById(id);
    }

    @Override
    public EmployeeResponse getById(Integer id) {
        EmployeeModel employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return toResponse(employee);
    }

    @Override
    public List<EmployeeResponse> getAll(Integer warehouseId) {
        EmployeeModel currentUser = securityHelper.getCurrentEmployee();
        String role = currentUser.getRole();
        List<EmployeeModel> employees;

        if (role.equals("ROLE_SUPER_ADMIN") || role.equals("SUPER_ADMIN")) {
            if (warehouseId != null) {
                employees = employeeRepository.findAllByWarehouseId(warehouseId);
            } else {
                employees = employeeRepository.findAll();
            }
        }
        else if (role.equals("ROLE_ADMIN")) {
            Integer myWarehouseId = currentUser.getWarehouse() != null ? currentUser.getWarehouse().getId() : null;

            if (myWarehouseId != null) {
                employees = employeeRepository.findAllByWarehouseId(myWarehouseId)
                        .stream()
                        .filter(e -> !e.getRole().contains("SUPER_ADMIN"))
                        .collect(Collectors.toList());
            } else {
                employees = Collections.emptyList();
            }
        }
        else {
            employees = Collections.emptyList();
        }

        return employees.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private EmployeeResponse toResponse(EmployeeModel employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setFullName(employee.getFullName());
        response.setPosition(employee.getPosition() != null ? employee.getPosition() : "—");
        response.setRole(employee.getRole());
        response.setLogin(employee.getLogin());

        if (employee.getWarehouse() != null) {
            response.setWarehouseName(employee.getWarehouse().getName());
            response.setWarehouseId(employee.getWarehouse().getId());
        } else {
            response.setWarehouseName("Глобальный доступ");
        }

        return response;
    }
}