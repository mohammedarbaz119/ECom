package com.test.commerce.repositories;

import com.test.commerce.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {


    @Query("""
            SELECT c from Cart c
            LEFT JOIN FETCH c.items ci
            LEFT JOIN FETCH ci.product p
            WHERE c.customer.ID = :customerId
            """)
    Optional<Cart> findCartwithfetch(@Param("customerId") Long customerId);

    Optional<Cart> findByCustomerID(Long customerId);
}
