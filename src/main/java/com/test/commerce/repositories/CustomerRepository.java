package com.test.commerce.repositories;

import com.test.commerce.model.Customer;
import com.test.commerce.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {
    Optional<Customer> findByEmail(String email);
    @Query("SELECT c FROM Customer c JOIN c.orders o WHERE o.id = :orderId")
    Optional<Customer> findByOrderId(@Param("orderId") Long orderId);

    @Query("""
            SELECT o FROM Customer c
            JOIN c.orders o
            JOIN FETCH o.items i
            JOIN FETCH i.product p
            WHERE c.email = :email            
            """)
    List<Order> getOrders(@Param("email") String email);
}
