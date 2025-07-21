package com.test.commerce.repositories;

import com.test.commerce.dtos.SaleDTO;
import com.test.commerce.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query("""
              SELECT new com.test.commerce.dtos.SaleDTO(
                    s.id,
                    s.saleNumber,
                    s.quantity,
                    s.unitPrice,
                    s.totalAmount,
                    s.commissionRate,
                    s.commissionAmount,
                    s.retailerAmount,
                    s.saleStatus,
                    s.saleDate,
                    s.returnedDate,
                    s.productName,
                    s.productCategory,
                    s.retailerName,
                    s.customerEmail
                )
                FROM Sale s
                WHERE s.retailer.id = :retailerId
                AND
                s.saleDate BETWEEN :startDate AND :endDate
            """)
    List<SaleDTO> findAllByRetailerId(@Param("retailerId") Long retailerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

}
