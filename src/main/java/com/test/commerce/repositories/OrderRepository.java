package com.test.commerce.repositories;

import com.test.commerce.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    @Query("""
    SELECT o FROM Order o 
    JOIN FETCH o.items i 
    JOIN FETCH i.product p 
    JOIN FETCH p.retailer 
    JOIN FETCH p.category
    WHERE o.id = :orderId
""")
    Optional<Order> findOrderWithItemsAndProduct(@Param("orderId") Long orderId);

    @Query("""
           SELECT COUNT(*)>0 FROM Order o WHERE o.id = :orderId and o.customer.email = :email
            """)
    boolean doesOrderBelongToCustomer(@Param("orderId") Long orderId,@Param("email") String email);

}
