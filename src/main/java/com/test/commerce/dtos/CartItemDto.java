package com.test.commerce.dtos;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CartItemDto {
   private Long id;
   private Integer productQuantity;
   private CustomerProductData productData;
}
