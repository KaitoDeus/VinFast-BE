package com.oem.evwarranty.domain.inventory;


import com.oem.evwarranty.domain.inventory.PartAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PartAllocationRepository extends JpaRepository<PartAllocation, Long> {
    List<PartAllocation> findByServiceCenter(String sc);

    List<PartAllocation> findByStatus(PartAllocation.AllocationStatus status);
}


