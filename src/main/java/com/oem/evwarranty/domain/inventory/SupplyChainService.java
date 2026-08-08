package com.oem.evwarranty.domain.inventory;

import com.oem.evwarranty.domain.audit.AuditLogService;

import com.oem.evwarranty.domain.inventory.PartAllocation;
import com.oem.evwarranty.domain.inventory.PartAllocationRepository;
import com.oem.evwarranty.common.exception.ResourceNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Supply Chain Service for managing part allocations and inventory
 * distribution.
 */
@Service
@Transactional
public class SupplyChainService {

    private final PartAllocationRepository allocationRepository;
    private final AuditLogService auditLogService;

    public SupplyChainService(PartAllocationRepository allocationRepository, AuditLogService auditLogService) {
        this.allocationRepository = allocationRepository;
        this.auditLogService = auditLogService;
    }

    public PartAllocation createRequest(PartAllocation allocation) {
        allocation.setStatus(PartAllocation.AllocationStatus.PENDING);
        return allocationRepository.save(allocation);
    }

    public PartAllocation ship(@NonNull Long id, String trackingNumber) {
        return allocationRepository.findById(id)
                .map(a -> {
                    a.setStatus(PartAllocation.AllocationStatus.SHIPPED);
                    a.setTrackingNumber(trackingNumber);
                    a.setShippedAt(LocalDateTime.now());
                    auditLogService.log("EVM_STAFF", "SHIP_PART", "ALLOCATION", id,
                            "Shipped with tracking: " + trackingNumber);
                    return allocationRepository.save(a);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found: " + id));
    }

    public PartAllocation receive(@NonNull Long id) {
        return allocationRepository.findById(id)
                .map(a -> {
                    a.setStatus(PartAllocation.AllocationStatus.RECEIVED);
                    a.setReceivedAt(LocalDateTime.now());
                    auditLogService.log("SC_STAFF", "RECEIVE_PART", "ALLOCATION", id,
                            "Part received at service center");
                    return allocationRepository.save(a);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found: " + id));
    }

    public List<PartAllocation> getPendingAllocations() {
        return allocationRepository.findByStatus(PartAllocation.AllocationStatus.PENDING);
    }
}



