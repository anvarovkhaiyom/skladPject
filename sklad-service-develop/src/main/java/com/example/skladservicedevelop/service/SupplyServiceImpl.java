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

        // Логика генерации номера документа
        String lastDocStr = supplyHistoryRepository.findLastDocumentNumber(warehouse.getId());
        int nextNum = 1;

        if (lastDocStr != null && lastDocStr.startsWith("ПО-")) {
            try {
                // Извлекаем цифровую часть после "ПО-"
                String numericPart = lastDocStr.substring(3);
                nextNum = Integer.parseInt(numericPart) + 1;
            } catch (Exception e) {
                // Если формат номера в базе вдруг нарушен, начинаем с 1
                nextNum = 1;
            }
        }

        // Форматируем новый номер: ПО-0000001
        String docNumber = String.format("ПО-%07d", nextNum);

        for (SupplyItemRequest it : request.getItems()) {
            ProductModel product = productRepository.findById(it.getProductId())
                    .orElseThrow(() -> new RuntimeException("Товар ID " + it.getProductId() + " не найден"));

            if (!product.getWarehouse().getId().equals(warehouse.getId())) {
                throw new RuntimeException("Товар " + product.getName() + " принадлежит другому складу!");
            }

            // Расчет количества
            BigDecimal qty = it.getQuantity() != null ? it.getQuantity() : BigDecimal.ZERO;
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Количество товара " + product.getName() + " должно быть > 0");
            }

            // Обновление остатка
            BigDecimal currentStock = (product.getStockQuantity() != null) ? product.getStockQuantity() : BigDecimal.ZERO;
            product.setStockQuantity(currentStock.add(qty));

            // Обновление закупочной цены
            BigDecimal newCostPrice = it.getCostPrice() != null ? it.getCostPrice() : product.getCostPrice();
            product.setCostPrice(newCostPrice);

            productRepository.save(product);

            // Создание записи в истории
            SupplyHistoryModel sh = new SupplyHistoryModel();
            sh.setProduct(product);
            sh.setSupplier(supplier);
            sh.setEmployee(currentEmployee);
            sh.setWarehouse(warehouse);
            sh.setQuantity(qty);
            sh.setCostPrice(newCostPrice);
            sh.setBarcode(product.getBarcode());
            sh.setSupplyDate(LocalDateTime.now());
            sh.setDocumentNumber(docNumber); // Присваиваем общий номер документа

            supplyHistoryRepository.save(sh);
        }
    }

}

