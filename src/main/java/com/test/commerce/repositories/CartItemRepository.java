package com.test.commerce.repositories;

import com.test.commerce.model.Cart;
import com.test.commerce.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    Optional<CartItem> findByCartAndProductId(Cart cart, Long productId);
}
