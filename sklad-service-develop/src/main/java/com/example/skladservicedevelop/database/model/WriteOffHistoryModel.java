package com.example.skladservicedevelop.database.model;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "write_off_history")
@Data
public class WriteOffHistoryModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private ProductModel product;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeModel employee;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false)
    private String reason;

    @Column(name = "write_off_date", nullable = false)
    private LocalDateTime writeOffDate;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private WarehouseModel warehouse;
    @Column(name = "document_number")
    private String documentNumber;
}