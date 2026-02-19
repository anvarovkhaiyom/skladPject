package com.example.skladservicedevelop.config;

import com.example.skladservicedevelop.database.model.EmployeeModel;
import com.example.skladservicedevelop.database.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityHelper {
    private final EmployeeRepository employeeRepository;

    public EmployeeModel getCurrentEmployee() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return employeeRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Текущий пользователь не найден"));
    }

    public Integer getCurrentWarehouseId() {
        EmployeeModel employee = getCurrentEmployee();
        return (employee.getWarehouse() != null) ? employee.getWarehouse().getId() : null;
    }
}