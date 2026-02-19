package com.example.skladservicedevelop.database.model;

import javax.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "clients")
@Data
public class ClientModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String contacts;

    @OneToMany(mappedBy = "client")
    private List<SaleModel> sales;
    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private WarehouseModel warehouse;
}
