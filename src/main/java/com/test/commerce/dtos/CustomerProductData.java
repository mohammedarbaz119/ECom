package com.test.commerce.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerProductData {
    private Long id;
    private String name;
    private String retailerName;
    private String description;
    private Integer stock;
    private BigDecimal mrp;
    private String categoryName;
    private LocalDateTime createdAt;
    private String imageUrl;
}
