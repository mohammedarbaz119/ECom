package com.test.commerce.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductData {
    private Long id;
    private String name;
    private String description;
    private Integer stock;
    private BigDecimal mrp;
    private BigDecimal cost;
    private String categoryName;
    private LocalDateTime createdAt;
    private String imageUrl;

    public ProductData(Long id, String name, String description, Integer stock,
                       BigDecimal mrp, BigDecimal cost, String categoryName,
                       LocalDateTime createdAt, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.stock = stock;
        this.mrp = mrp;
        this.cost = cost;
        this.categoryName = categoryName;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
    }


}