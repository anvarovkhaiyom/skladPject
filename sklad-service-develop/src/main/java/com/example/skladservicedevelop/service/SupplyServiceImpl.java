package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.config.SecurityHelper;
import com.example.skladservicedevelop.database.model.*;
import com.example.skladservicedevelop.database.repository.ProductRepository;
import com.example.skladservicedevelop.database.repository.SupplierRepository;
import com.example.skladservicedevelop.database.repository.SupplyHistoryRepository;
import com.example.skladservicedevelop.dto.request.SupplyItemRequest;
import com.example.skladservicedevelop.dto.request.SupplyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SupplyServiceImpl implements SupplyService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final SecurityHelper securityHelper;
    private final SupplyHistoryRepository supplyHistoryRepository;


    @Override
    @Transactional
    public void createSupply(SupplyRequest request) {
        EmployeeModel currentEmployee = securityHelper.getCurrentEmployee();
        WarehouseModel warehouse = currentEmployee.getWarehouse();

        SupplierModel supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Поставщик не найден"));

        String lastDocStr = supplyHistoryRepository.findLastDocumentNumber(warehouse.getId());
        int nextNum = 1;

        if (lastDocStr != null && lastDocStr.startsWith("ПО-")) {
            try {
                String numericPart = lastDocStr.substring(3);
                nextNum = Integer.parseInt(numericPart) + 1;
            } catch (Exception e) {
                nextNum = 1;
            }
        }

        String docNumber = String.format("ПО-%07d", nextNum);

        for (SupplyItemRequest it : request.getItems()) {
            ProductModel product = productRepository.findById(it.getProductId())
                    .orElseThrow(() -> new RuntimeException("Товар ID " + it.getProductId() + " не найден"));

            if (!product.getWarehouse().getId().equals(warehouse.getId())) {
                throw new RuntimeException("Товар " + product.getName() + " принадлежит другому складу!");
            }

            BigDecimal qty = it.getQuantity() != null ? it.getQuantity() : BigDecimal.ZERO;
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Количество товара " + product.getName() + " должно быть > 0");
            }

            BigDecimal currentStock = (product.getStockQuantity() != null) ? product.getStockQuantity() : BigDecimal.ZERO;
            product.setStockQuantity(currentStock.add(qty));

            BigDecimal newCostPrice = it.getCostPrice() != null ? it.getCostPrice() : product.getCostPrice();
            product.setCostPrice(newCostPrice);

            productRepository.save(product);

            SupplyHistoryModel sh = new SupplyHistoryModel();
            sh.setProduct(product);
            sh.setSupplier(supplier);
            sh.setEmployee(currentEmployee);
            sh.setWarehouse(warehouse);
            sh.setQuantity(qty);
            sh.setCostPrice(newCostPrice);
            sh.setBarcode(product.getBarcode());
            sh.setSupplyDate(LocalDateTime.now());
            sh.setDocumentNumber(docNumber);

            supplyHistoryRepository.save(sh);
        }
    }

}

