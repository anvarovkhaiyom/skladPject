package com.example.skladservicedevelop.database.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sale_payments")
@Data
public class SalePaymentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sale_id")
    private SaleModel sale;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false)
    private CurrencyModel currency;

    @Column(name = "exchange_rate")
    private BigDecimal exchangeRate;
}
