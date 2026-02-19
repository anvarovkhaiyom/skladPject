package com.example.skladservicedevelop.database.model;

import javax.persistence.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "supply_history")
@Data
public class SupplyHistoryModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private SupplierModel supplier;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private ProductModel product;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeModel employee;

    @Column(name = "cost_price", nullable = false)
    private BigDecimal costPrice;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(name = "barcode", nullable = false)
    private String barcode;

    @Column(name = "supply_date", nullable = false)
    private LocalDateTime supplyDate;
    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private WarehouseModel warehouse;
}
