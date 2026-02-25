package com.example.skladservicedevelop.database.model;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "drivers")
@Data
public class DriverModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;
    @Column(unique = true)
    private String phone;
    @Column(unique = true)
    private String carNumber;

    private BigDecimal salary;
    private String carMark;
    private String additionalInfo;

    private boolean deleted = false;
    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private WarehouseModel warehouse;
}