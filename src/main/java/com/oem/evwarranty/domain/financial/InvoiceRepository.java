package com.oem.evwarranty.domain.financial;

import com.oem.evwarranty.common.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByClientId(Long clientId);

    @Query("SELECT i FROM Invoice i WHERE (:status IS NULL OR i.status = :status)")
    Page<Invoice> findByStatusWithPagination(@Param("status") InvoiceStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.status = 'COMPLETED'")
    BigDecimal sumTotalCompletedRevenue();

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.status = 'PENDING'")
    BigDecimal sumTotalPendingPayments();
}
