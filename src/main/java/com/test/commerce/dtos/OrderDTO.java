package com.test.commerce.dtos;

import com.test.commerce.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long orderId;
    private String deliveryAddress;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime orderPlacedAt;
    private LocalDateTime deliveredDate;
    private LocalDateTime returnedDate;
    private List<OrderItemDTO> items;
}
