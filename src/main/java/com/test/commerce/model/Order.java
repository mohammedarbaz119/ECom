package com.test.commerce.model;

import com.test.commerce.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
    @SequenceGenerator(name = "order_seq", sequenceName = "order_sequence", allocationSize = 20)
    private Long id;

    @ManyToOne
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<Sale> sales = new ArrayList<>();

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    private BigDecimal Totalamt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;

    @Column(name = "returned_date")
    private LocalDateTime returnedDate;

    @CreationTimestamp
    @Column(name = "placed_at", nullable = false, updatable = false)
    private LocalDateTime orderPlacedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean canBeCancelled() {
        return status == OrderStatus.SHIPPED || status == OrderStatus.PLACED;
    }

    public boolean canBeReturned() {
        return status == OrderStatus.DELIVERED;
    }

    public void SetReturned(){
        status=OrderStatus.RETURNED;
        returnedDate=LocalDateTime.now();
    }
    public boolean canBeDelivered(){
        return status == OrderStatus.PLACED;
    }
    public void SetDelivered(){
        status = OrderStatus.DELIVERED;
        deliveredDate = LocalDateTime.now();
    }
    public void CancelOrder(){
        status=OrderStatus.CANCELLED;
    }
}