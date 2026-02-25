package com.example.skladservicedevelop.database.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Data
public class ExpenseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime expenseDate;

    private String description;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private WarehouseModel warehouse;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private EmployeeModel employee;
    @Column(name = "document_number")
    private String documentNumber;
}