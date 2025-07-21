package com.test.commerce.dtos;

import com.test.commerce.enums.SaleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaleDTO {
    private Long id;
    private String saleNumber;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private BigDecimal retailerAmount;
    private SaleStatus saleStatus;
    private LocalDateTime saleDate;
    private LocalDateTime returnedDate;
    private String productName;
    private String productCategory;
    private String retailerName;
    private String customerEmail;
}