package com.oem.evwarranty.domain.preorder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PreOrderRepository extends JpaRepository<PreOrder, Long> {

    @Query("SELECT p FROM PreOrder p WHERE " +
            "(:status IS NULL OR :status = '' OR p.status = :status) AND " +
            "(:query IS NULL OR :query = '' OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.phone) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<PreOrder> searchPreOrders(@Param("query") String query, @Param("status") String status, Pageable pageable);
}
