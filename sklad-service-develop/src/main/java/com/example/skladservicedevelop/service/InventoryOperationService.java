package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.database.model.ExpenseModel;
import com.example.skladservicedevelop.database.model.WriteOffHistoryModel;
import com.example.skladservicedevelop.dto.request.ExpenseRequest;
import com.example.skladservicedevelop.dto.request.WriteOffRequest;

import java.util.List;

public interface InventoryOperationService {
    // Списание товара (брак, порча)
    void createWriteOff(WriteOffRequest request);

    // Регистрация операционных расходов (ЗП, аренда и т.д.)
    void createExpense(ExpenseRequest request);
    List<ExpenseModel> getAllExpenses();
    List<WriteOffHistoryModel> getAllWriteOffs();
}