package com.example.skladservicedevelop.controllers;

import com.example.skladservicedevelop.dto.request.SaleRequest;
import com.example.skladservicedevelop.dto.response.*;
import com.example.skladservicedevelop.dto.request.SupplyRequest;
import com.example.skladservicedevelop.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final ProductService productService;
    private final ClientService clientService;
    private final SaleService saleService;
    private final CurrencyService currencyService;
    private final WarehouseService warehouseService;
    private final SupplierService supplierService;
    private final EmployeeService employeeService;
    private final SupplyService supplyService;

    @GetMapping("/product")
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) Integer warehouseId) {
        return ResponseEntity.ok(productService.getAll(warehouseId));
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/client")
    public ResponseEntity<List<ClientResponse>> getAllClients(
            @RequestParam(required = false) Integer warehouseId) {
        return ResponseEntity.ok(clientService.getAll(warehouseId));
    }

    @GetMapping("/currency")
    public ResponseEntity<List<CurrencyResponse>> getAllCurrencies() {
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }

    @GetMapping("/warehouse/current")
    public ResponseEntity<WarehouseResponse> getCurrentWarehouse() {
        return ResponseEntity.ok(warehouseService.getCurrentWarehouse());
    }

    @PostMapping("/sale")
    public ResponseEntity<SaleResponse> createSale(@RequestBody SaleRequest request) {
        return ResponseEntity.ok(saleService.createSale(request));
    }

    @GetMapping("/sale/{id}")
    public ResponseEntity<SaleResponse> getSaleById(@PathVariable Integer id) {
        return ResponseEntity.ok(saleService.getById(id));
    }
    @GetMapping("/supplier")
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAll());
    }
    @PostMapping("/supply")
    public ResponseEntity<String> createSupply(@RequestBody SupplyRequest request) {
        supplyService.createSupply(request);
        return ResponseEntity.ok("Supply recorded successfully by admin");
    }
    // В EmployeeController.java
    @GetMapping("/list") // Полный путь будет: /employee/list
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(
            @RequestParam(required = false) Integer warehouseId) {
        return ResponseEntity.ok(employeeService.getAll(warehouseId));
    }
}