package com.example.skladservicedevelop.database.model;

import javax.persistence.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sales")
@Data
public class SaleModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private ClientModel client;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private EmployeeModel employee;

    @Column(nullable = false)
    private String status = "completed";

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItemModel> items;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalePaymentModel> payments;

    @Column(name = "change_amount")
    private BigDecimal changeAmount;
    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "car_mark")
    private String carMark;

    @Column(name = "car_number")
    private String carNumber;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "proxy_number")
    private String proxyNumber;

    @Column(name = "proxy_date")
    private LocalDate proxyDate;
    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private WarehouseModel warehouse;
}

