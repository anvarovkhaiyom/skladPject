package com.example.skladservicedevelop.controllers;
import com.example.skladservicedevelop.dto.request.CurrencyRequest;
import com.example.skladservicedevelop.dto.request.EmployeeRequest;
import com.example.skladservicedevelop.dto.request.WarehouseRequest;
import com.example.skladservicedevelop.dto.response.*;
import com.example.skladservicedevelop.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/super")
@RequiredArgsConstructor
public class SuperAdminController {

    private final WarehouseService warehouseService;
    private final EmployeeService employeeService;
    private final ReportService reportService;
    private final CurrencyService currencyService;

    // --- УПРАВЛЕНИЕ СКЛАДАМИ ---

    @PostMapping("/warehouses")
    public ResponseEntity<WarehouseResponse> createWarehouse(@RequestBody WarehouseRequest request) {
        return ResponseEntity.ok(warehouseService.create(request));
    }

    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponse>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAll());
    }

    @PutMapping("/warehouses/{id}")
    public ResponseEntity<WarehouseResponse> updateWarehouse(@PathVariable Integer id, @RequestBody WarehouseRequest request) {
        return ResponseEntity.ok(warehouseService.update(id, request));
    }

    @DeleteMapping("/warehouses/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Integer id) {
        warehouseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- УПРАВЛЕНИЕ СОТРУДНИКАМИ (Всех складов) ---

    @PostMapping("/employees")
    public ResponseEntity<EmployeeResponse> createEmployee(@RequestBody EmployeeRequest request) {
        // Здесь важно: в EmployeeRequest должен быть warehouseId
        return ResponseEntity.ok(employeeService.create(request));
    }

    @GetMapping("/employee")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(
            @RequestParam(required = false) Integer warehouseId) {
        return ResponseEntity.ok(employeeService.getAll(warehouseId));
    }
    // --- ГЛОБАЛЬНАЯ ВАЛЮТА ---

    @PostMapping("/currencies")
    public ResponseEntity<CurrencyResponse> createCurrency(@RequestBody CurrencyRequest request) {
        return ResponseEntity.ok(currencyService.createCurrency(request));
    }

    // --- ГЛОБАЛЬНЫЕ ОТЧЕТЫ (По всей сети) ---

    @GetMapping("/reports/summary")
    public ResponseEntity<StockSummaryResponse> getGlobalStockSummary() {
        return ResponseEntity.ok(reportService.getStockSummary(null));
    }

    @GetMapping("/reports/sales")
    public ResponseEntity<SalesSummaryResponse> getGlobalSalesSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Integer warehouseId) {

        return ResponseEntity.ok(reportService.getSalesSummary(start, end, null, warehouseId));
    }

    @GetMapping("/reports/sales/excel")
    public ResponseEntity<byte[]> downloadGlobalSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Integer warehouseId) { // ОБЯЗАТЕЛЬНО ДОБАВИТЬ

        byte[] excel = reportService.generateGeneralSalesReportExcel(start, end, null, warehouseId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }
}