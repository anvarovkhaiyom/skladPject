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

        SupplierModel supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("Поставщик не найден"));
        }

        for (SupplyItemRequest it : request.getItems()) {
            ProductModel product;

            // ПОИСК: Сначала по ID (если выбрали в списке), иначе по баркоду или артикулу
            if (it.getProductId() != null) {
                product = productRepository.findById(it.getProductId())
                        .orElseThrow(() -> new RuntimeException("Товар не найден"));
            } else {
                product = productRepository.findByBarcodeAndWarehouseId(it.getBarcode(), warehouse.getId())
                        .orElseGet(() -> productRepository.findBySkuAndWarehouseId(it.getSku(), warehouse.getId())
                                .orElseThrow(() -> new RuntimeException("Товар не найден на вашем складе")));
            }

            BigDecimal qty = it.getQuantity() != null ? it.getQuantity() : BigDecimal.ZERO;
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Количество должно быть больше 0");
            }

            BigDecimal costPrice = it.getCostPrice() != null ? it.getCostPrice() : product.getCostPrice();

            BigDecimal currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : BigDecimal.ZERO;
            product.setStockQuantity(currentStock.add(qty));

            // Обновляем закупочную цену товара, если она изменилась при приходе
            product.setCostPrice(costPrice);
            productRepository.save(product);

            SupplyHistoryModel sh = new SupplyHistoryModel();
            sh.setProduct(product);
            sh.setSupplier(supplier);
            sh.setEmployee(currentEmployee);
            sh.setWarehouse(warehouse);
            sh.setQuantity(qty);
            sh.setCostPrice(costPrice);
            sh.setBarcode(product.getBarcode());
            sh.setSupplyDate(LocalDateTime.now());

            supplyHistoryRepository.save(sh);
        }
    }
}
