package com.test.commerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_item_seq")
    @SequenceGenerator(name = "order_item_seq", sequenceName = "order_item_sequence", allocationSize = 20)
    private Long id;

    @ManyToOne
    private Order order;

    @OneToOne(mappedBy = "orderItem", fetch = FetchType.LAZY)
    private Sale sale;

    @ManyToOne
    private Product product;

    private Integer quantity;

    private BigDecimal price;
}