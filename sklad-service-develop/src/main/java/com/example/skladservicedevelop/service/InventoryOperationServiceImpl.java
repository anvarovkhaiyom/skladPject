package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.config.SecurityHelper;
import com.example.skladservicedevelop.database.model.*;
import com.example.skladservicedevelop.database.repository.ExpenseRepository;
import com.example.skladservicedevelop.database.repository.ProductRepository;
import com.example.skladservicedevelop.database.repository.WriteOffHistoryRepository;
import com.example.skladservicedevelop.dto.request.ExpenseRequest;
import com.example.skladservicedevelop.dto.request.WriteOffRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryOperationServiceImpl implements InventoryOperationService {

    private final ProductRepository productRepository;
    private final WriteOffHistoryRepository writeOffRepository;
    private final ExpenseRepository expenseRepository;
    private final SecurityHelper securityHelper;

    @Override
    @Transactional
    public void createWriteOff(WriteOffRequest request) {
        EmployeeModel currentEmployee = securityHelper.getCurrentEmployee();
        WarehouseModel warehouse = currentEmployee.getWarehouse();

        ProductModel product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        if (!product.getWarehouse().getId().equals(warehouse.getId())) {
            throw new RuntimeException("Списание отклонено: чужой склад.");
        }

        BigDecimal currentStock = product.getStockQuantity();
        if (currentStock.compareTo(request.getQuantity()) < 0) {
            throw new RuntimeException("Недостаточно товара! На складе: " + currentStock);
        }

        product.setStockQuantity(currentStock.subtract(request.getQuantity()));
        productRepository.save(product);

        WriteOffHistoryModel history = new WriteOffHistoryModel();
        history.setProduct(product);
        history.setEmployee(currentEmployee);
        history.setWarehouse(warehouse);
        history.setQuantity(request.getQuantity());
        history.setReason(request.getReason());
        history.setWriteOffDate(LocalDateTime.now());

        Integer lastId = writeOffRepository.findMaxIdByWarehouseId(warehouse.getId());
        int nextId = (lastId == null) ? 1 : lastId + 1;
        history.setDocumentNumber(String.format("СП-%07d", nextId));

        writeOffRepository.save(history);
    }

    @Override
    @Transactional
    public void createExpense(ExpenseRequest request) {
        EmployeeModel currentEmployee = securityHelper.getCurrentEmployee();
        WarehouseModel warehouse = currentEmployee.getWarehouse();

        ExpenseModel expense = new ExpenseModel();
        expense.setCategory(request.getCategory());
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(LocalDateTime.now());
        expense.setWarehouse(warehouse);
        expense.setEmployee(currentEmployee);

        Integer lastId = expenseRepository.findMaxIdByWarehouseId(warehouse.getId());
        int nextId = (lastId == null) ? 1 : lastId + 1;
        expense.setDocumentNumber(String.format("РХ-%07d", nextId));

        expenseRepository.save(expense);
    }
    @Override
    public List<ExpenseModel> getAllExpenses() {
        EmployeeModel currentEmployee = securityHelper.getCurrentEmployee();
        Integer warehouseId = currentEmployee.getWarehouse().getId();

        List<ExpenseModel> expenses = expenseRepository.findByWarehouseIdNative(warehouseId);

        expenses.forEach(e -> {
            e.setWarehouse(null);
            if (e.getEmployee() != null) {
                e.getEmployee().setSales(null);
                e.getEmployee().setWarehouse(null);
            }
        });
        return expenses;
    }

    @Override
    public List<WriteOffHistoryModel> getAllWriteOffs() {
        EmployeeModel currentEmployee = securityHelper.getCurrentEmployee();
        Integer warehouseId = currentEmployee.getWarehouse().getId();

        List<WriteOffHistoryModel> writeOffs = writeOffRepository.findByWarehouseIdNative(warehouseId);

        writeOffs.forEach(w -> {
            w.setWarehouse(null);
            if (w.getEmployee() != null) {
                w.getEmployee().setSales(null);
                w.getEmployee().setWarehouse(null);
            }
            if (w.getProduct() != null) {
                w.getProduct().setWarehouse(null);
            }
        });
        return writeOffs;
    }
}