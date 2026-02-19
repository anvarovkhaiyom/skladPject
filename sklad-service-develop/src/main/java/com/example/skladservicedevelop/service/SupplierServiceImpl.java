package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.config.SecurityHelper;
import com.example.skladservicedevelop.database.model.EmployeeModel;
import com.example.skladservicedevelop.database.model.SupplierModel;
import com.example.skladservicedevelop.database.repository.SupplierRepository;
import com.example.skladservicedevelop.dto.request.SupplierRequest;
import com.example.skladservicedevelop.dto.response.SupplierResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SecurityHelper securityHelper;


    @Override
    public SupplierResponse create(SupplierRequest request) {
        EmployeeModel current = securityHelper.getCurrentEmployee();
        SupplierModel supplier = new SupplierModel();
        supplier.setFullName(request.getFullName());
        supplier.setContacts(request.getContacts());
        supplier.setWarehouse(current.getWarehouse());
        supplierRepository.save(supplier);
        return toResponse(supplier);
    }

    @Override
    public SupplierResponse update(Integer id, SupplierRequest request) {
        EmployeeModel current = securityHelper.getCurrentEmployee();
        SupplierModel supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Поставщик не найден"));
        if (!"SUPER_ADMIN".equals(current.getRole()) &&
                !supplier.getWarehouse().getId().equals(current.getWarehouse().getId())) {
            throw new RuntimeException("Доступ запрещен: это не ваш поставщик");
        }
        supplier.setFullName(request.getFullName());
        supplier.setContacts(request.getContacts());
        supplierRepository.save(supplier);
        return toResponse(supplier);
    }

    @Override
    public void delete(Integer id) {
        supplierRepository.deleteById(id);
    }

    @Override
    public SupplierResponse getById(Integer id) {
        SupplierModel supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        return toResponse(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAll() {
        EmployeeModel current = securityHelper.getCurrentEmployee();
        if ("SUPER_ADMIN".equals(current.getRole())) {
            return supplierRepository.findAll().stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }

        return supplierRepository.findAllByWarehouseId(current.getWarehouse().getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private SupplierResponse toResponse(SupplierModel supplier) {
        SupplierResponse response = new SupplierResponse();
        response.setId(supplier.getId());
        response.setFullName(supplier.getFullName());
        response.setContacts(supplier.getContacts());
        return response;
    }
}