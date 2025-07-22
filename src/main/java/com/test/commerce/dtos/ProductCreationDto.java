package com.test.commerce.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class ProductCreationDto {
    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Cost (buying price) is required")
    @DecimalMin(value = "0.01", message = "Cost must be greater than 0")
    private Double cost;

    @NotNull(message = "MRP (selling price) is required")
    @DecimalMin(value = "0.01", message = "MRP must be greater than 0")
    private Double mrp;

    @Min(value = 0,message = "stock should be positive when adding a product")
    private int stock;

    @NotBlank(message = "Product category is required")
    private String category;

    @NotBlank(message = "url key must be present")
    private String imageUrl;
}
