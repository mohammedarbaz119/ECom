package com.test.commerce.repositories;

import com.test.commerce.model.Retailer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RetailorRepository extends JpaRepository<Retailer,Long> {

    Optional<Retailer> findByEmail(String email);
}
