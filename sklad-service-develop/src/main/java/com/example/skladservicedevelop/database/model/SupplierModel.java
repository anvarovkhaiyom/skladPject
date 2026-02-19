package com.example.skladservicedevelop.database.model;

import javax.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "suppliers")
@Data
public class SupplierModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String contacts;

    @OneToMany(mappedBy = "supplier")
    private List<SupplyHistoryModel> supplyHistory;
    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private WarehouseModel warehouse;
}
