package com.test.commerce.repositories;

import com.test.commerce.model.SalesReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesReportRepository extends JpaRepository<SalesReport,Long> {

    @Query("""
           SELECT COUNT(*)>0 FROM SalesReport s WHERE s.id = :reportId and s.retailer.email = :email
            """)
    boolean doesReportBelongToRetailer(@Param("reportId") Long reportId, @Param("email") String email);
}
