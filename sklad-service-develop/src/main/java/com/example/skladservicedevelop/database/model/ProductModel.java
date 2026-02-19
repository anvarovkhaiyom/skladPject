package com.example.skladservicedevelop.database.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "products", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"barcode", "warehouse_id"}),
        @UniqueConstraint(columnNames = {"sku", "warehouse_id"}),
        @UniqueConstraint(columnNames = {"name", "warehouse_id"})
})
@Data
public class ProductModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryModel category;

    @Column(nullable = false)
    private String name;

    @Column(name = "cost_price", nullable = false)
    private BigDecimal costPrice;

    @Column(nullable = false)
    private String unit;

    @Column(name = "sale_price", nullable = false)
    private BigDecimal salePrice;

    @Column(name = "stock_quantity", nullable = false)
    private BigDecimal stockQuantity;

    @Column(nullable = true)
    private String barcode;



    @Column(name = "photo_url")
    private String photoUrl;

    @OneToMany(mappedBy = "product")
    private List<SaleItemModel> saleItems;

    @OneToMany(mappedBy = "product")
    private List<SupplyHistoryModel> supplyHistory;
    @Column(nullable = true)
    private String sku;


    @Column(name = "items_in_box")
    private BigDecimal itemsInBox;

    @Column(name = "weight_brutto")
    private Double weightBrutto;
    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private WarehouseModel warehouse;
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;
}
