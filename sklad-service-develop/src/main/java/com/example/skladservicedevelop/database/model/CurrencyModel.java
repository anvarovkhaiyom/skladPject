package com.example.skladservicedevelop.database.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "currencies")
@Data
public class CurrencyModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double rate = 1.0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
