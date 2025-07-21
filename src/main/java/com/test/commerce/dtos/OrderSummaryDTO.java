package com.test.commerce.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummaryDTO {
    private List<OrderDTO> orders;
    private int totalOrders;
    private BigDecimal totalAmount;
}
