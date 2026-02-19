package com.example.skladservicedevelop.database.model;
import javax.persistence.*;

import lombok.Data;

import java.util.List;

@Entity
@Table(name = "warehouses")
@Data
public class WarehouseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    private String address;

    @ManyToOne
    @JoinColumn(name = "base_currency_id")
    private CurrencyModel baseCurrency;

    @OneToMany(mappedBy = "warehouse")
    private List<EmployeeModel> employees;

    @OneToMany(mappedBy = "warehouse")
    private List<ProductModel> products;
}