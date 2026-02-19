package com.example.skladservicedevelop.database.model;


import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
public class CategoryModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "category")
    private List<ProductModel> products;
    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private WarehouseModel warehouse;
}
