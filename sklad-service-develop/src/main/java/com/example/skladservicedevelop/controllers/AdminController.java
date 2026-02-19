package com.example.skladservicedevelop.controllers;

import com.example.skladservicedevelop.dto.request.*;
import com.example.skladservicedevelop.dto.response.*;
import com.example.skladservicedevelop.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final ClientService clientService;
    private final EmployeeService employeeService;
    private final SupplierService supplierService;
    private final SalesReportService salesReportService;
    private final SupplyService supplyService;
    private final SaleService saleService;

    private final CurrencyService currencyService;
    private final ReportService reportService;
    private final WarehouseService warehouseService;
    private final DateTimeFormatter fileDateFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");


    //  CATEGORY
    @PostMapping("/category")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest req) {
        return ResponseEntity.ok(categoryService.create(req));
    }

    @PutMapping("/category/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Integer id, @RequestBody CategoryRequest req) {
        return ResponseEntity.ok(categoryService.update(id, req));
    }

    @DeleteMapping("/category/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @GetMapping("/category")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    //  PRODUCT
    @PostMapping("/product")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest req) {
        return ResponseEntity.ok(productService.create(req));
    }

    @PutMapping("/product/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Integer id, @RequestBody ProductRequest req) {
        return ResponseEntity.ok(productService.update(id, req));
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/product")
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) Integer warehouseId) {
        return ResponseEntity.ok(productService.getAll(warehouseId));
    }

    //  CLIENT
    @PostMapping("/client")
    public ResponseEntity<ClientResponse> createClient(@RequestBody ClientRequest req) {
        return ResponseEntity.ok(clientService.create(req));
    }

    @PutMapping("/client/{id}")
    public ResponseEntity<ClientResponse> updateClient(@PathVariable Integer id, @RequestBody ClientRequest req) {
        return ResponseEntity.ok(clientService.update(id, req));
    }

    @DeleteMapping("/client/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Integer id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/client/{id}")
    public ResponseEntity<ClientResponse> getClient(@PathVariable Integer id) {
        return ResponseEntity.ok(clientService.getById(id));
    }

    @GetMapping("/client")
    public ResponseEntity<List<ClientResponse>> getAllClients(Integer warehouseId) {
        return ResponseEntity.ok(clientService.getAll(warehouseId));
    }

    //  EMPLOYEE
    @PostMapping("/employee")
    public ResponseEntity<EmployeeResponse> createEmployee(@RequestBody EmployeeRequest req) {
        return ResponseEntity.ok(employeeService.create(req));
    }

    @PutMapping("/employee/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Integer id, @RequestBody EmployeeRequest req) {
        return ResponseEntity.ok(employeeService.update(id, req));
    }

    @DeleteMapping("/employee/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Integer id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Integer id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @GetMapping("/employee")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(
            @RequestParam(required = false) Integer warehouseId) {
        return ResponseEntity.ok(employeeService.getAll(warehouseId));
    }

    //  SUPPLIER
    @PostMapping("/supplier")
    public ResponseEntity<SupplierResponse> createSupplier(@RequestBody SupplierRequest req) {
        return ResponseEntity.ok(supplierService.create(req));
    }

    @PutMapping("/supplier/{id}")
    public ResponseEntity<SupplierResponse> updateSupplier(@PathVariable Integer id, @RequestBody SupplierRequest req) {
        return ResponseEntity.ok(supplierService.update(id, req));
    }

    @DeleteMapping("/supplier/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Integer id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/supplier/{id}")
    public ResponseEntity<SupplierResponse> getSupplier(@PathVariable Integer id) {
        return ResponseEntity.ok(supplierService.getById(id));
    }

    @GetMapping("/supplier")
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAll());
    }

    //  SALES
    @GetMapping("/sale/{id}")
    public ResponseEntity<SaleResponse> getSaleById(@PathVariable Integer id) {
        return ResponseEntity.ok(saleService.getById(id));
    }

    @GetMapping("/sale")
    public ResponseEntity<List<SaleResponse>> getSales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) Integer warehouseId
    ) {
        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT = end.plusDays(1).atStartOfDay();
        return ResponseEntity.ok(saleService.findSalesByFilters(startDT, endDT, clientName, employeeName, warehouseId));
    }

    @PostMapping("/sale")
    public ResponseEntity<SaleResponse> createSale(@RequestBody SaleRequest request) {
        SaleResponse response = saleService.createSale(request);
        return ResponseEntity.ok(response);
    }

    //  REPORTS
    @GetMapping("/report/sales")
    public ResponseEntity<List<SalesReportResponse>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(salesReportService.getSalesReport(start, end));
    }

    //  SUPPLIES
    @PostMapping("/supply")
    public ResponseEntity<String> createSupply(@RequestBody SupplyRequest request) {
        supplyService.createSupply(request);
        return ResponseEntity.ok("Supply recorded successfully by admin");
    }

    //  CURRENCY
    @PostMapping("/currency")
    public ResponseEntity<CurrencyResponse> createCurrency(@RequestBody CurrencyRequest request) {
        return ResponseEntity.ok(currencyService.createCurrency(request));
    }

    @GetMapping("/currency")
    public ResponseEntity<List<CurrencyResponse>> getAllCurrencies() {
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }

    @DeleteMapping("/currency/{id}")
    public ResponseEntity<Void> deleteCurrency(@PathVariable Integer id) {
        currencyService.deleteCurrency(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/currency/{id}")
    public ResponseEntity<CurrencyResponse> updateCurrency(
            @PathVariable Integer id,
            @RequestBody CurrencyRequest request) {
        return ResponseEntity.ok(currencyService.updateCurrency(id, request));
    }

    @GetMapping("warehouse/current")
    public ResponseEntity<WarehouseResponse> getCurrentWarehouse() {
        return ResponseEntity.ok(warehouseService.getCurrentWarehouse());
    }

    // REPORTS
    @GetMapping("/picking-list/{id}")
    public ResponseEntity<byte[]> getPickingList(@PathVariable Integer id) {
        return createResponse(reportService.generatePickingListExcel(id), "List_Sborshika_" + id);
    }

    @GetMapping("/invoice-boxes/{id}")
    public ResponseEntity<byte[]> getInvoiceWithBoxes(@PathVariable Integer id) {
        return createResponse(reportService.generateInvoiceWithBoxesExcel(id), "Nakladnaya_Korobki_" + id);
    }

    @GetMapping("/z2-report/{id}")
    public ResponseEntity<byte[]> getZ2Report(@PathVariable Integer id) {
        return createResponse(reportService.generateZ2Report(id), "Forma_Z2_" + id);
    }

    @GetMapping("/ttn/{id}")
    public ResponseEntity<byte[]> getTTN(@PathVariable Integer id) {
        return createResponse(reportService.generateTTN(id), "TTN_Report_" + id);
    }

    private ResponseEntity<byte[]> createResponse(byte[] data, String fileName) {
        String fullFileName = fileName + "_" + LocalDateTime.now().format(fileDateFormatter) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fullFileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/sales-summary")
    public ResponseEntity<SalesSummaryResponse> getSalesSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer warehouseId) {
        return ResponseEntity.ok(reportService.getSalesSummary(start, end, categoryId, warehouseId));
    }

    @GetMapping("/stock-summary")
    public ResponseEntity<StockSummaryResponse> getStockSummary(
            @RequestParam(required = false) Integer warehouseId) {
        return ResponseEntity.ok(reportService.getStockSummary(warehouseId));
    }

    @GetMapping("/sales-excel")
    public ResponseEntity<byte[]> downloadGeneralSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer warehouseId) {
        byte[] data = reportService.generateGeneralSalesReportExcel(start, end, categoryId, warehouseId);
        return createResponse(data, "General_Sales_Report");
    }

    @GetMapping("/stock-excel")
    public ResponseEntity<byte[]> downloadStockReport(
            @RequestParam(required = false) Integer warehouseId) {
        byte[] data = reportService.generateStockReportExcel(warehouseId);
        return createResponse(data, "Stock_Inventory_Report");
    }

}
