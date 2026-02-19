package com.example.skladservicedevelop.database.model;

import javax.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "employees")
@Data
public class EmployeeModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String position;
    private String role;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @OneToMany(mappedBy = "employee")
    private List<SaleModel> sales;

    @OneToMany(mappedBy = "employee")
    private List<SupplyHistoryModel> supplyHistory;
    @Column(name = "full_name_short")
    private String fullNameShort;
    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private WarehouseModel warehouse;
}
