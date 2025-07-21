package com.test.commerce.repositories;

import com.test.commerce.dtos.CustomerProductData;
import com.test.commerce.dtos.ProductData;
import com.test.commerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> {


    @Query("""
    SELECT new com.test.commerce.dtos.ProductData(
        p.id, p.name, p.description,p.stock, p.mrp, p.cost, p.category.name,p.createdAt,p.imageUrl
    )
    FROM Product p
    WHERE p.retailer.id = :retailerId
     AND (
           :inStock IS NULL OR
           (:inStock = true AND p.stock > 0) OR
           (:inStock = false AND p.stock = 0)
           )
     ORDER BY p.createdAt DESC
""")
    List<ProductData> findAllByRetailorWithCategoryName(@Param("retailerId") Long retailerId,@Param("inStock") boolean inStock);

    @Query("""
            SELECT new com.test.commerce.dtos.CustomerProductData(p.id, p.name, p.retailer.retailerName, p.description,
            p.stock, p.mrp, p.category.name, p.createdAt, p.imageUrl
            )
            FROM Product p
            WHERE p.id = :id 
            """)
    Optional<CustomerProductData> findProjectedById(@Param("id") Long id);
}
