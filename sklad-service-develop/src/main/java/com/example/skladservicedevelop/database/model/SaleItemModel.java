package com.example.skladservicedevelop.database.model;

import javax.persistence.*;

import lombok.Data;

import java.math.BigDecimal;


@Entity
@Table(name = "sale_items")
@Data
public class SaleItemModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = false)
    private SaleModel sale;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private ProductModel product;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal totalPrice;
    @Column(name = "box_count")
    private BigDecimal boxCount;

    @Column(name = "items_per_box_at_sale")
    private BigDecimal itemsPerBoxAtSale;
}
