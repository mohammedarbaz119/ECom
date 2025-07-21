package com.test.commerce.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class CartDto {
   private Long id;
   private BigDecimal TotalPrice;
   private Integer quantity;
   private List<CartItemDto> items;

}
