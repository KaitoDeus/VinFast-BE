package com.oem.evwarranty.domain.inventory;


import com.oem.evwarranty.domain.inventory.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Inventory entity operations.
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByServiceCenter(String serviceCenter);

    @Query("SELECT i FROM Inventory i WHERE i.part.id = :partId AND i.serviceCenter = :serviceCenter")
    Optional<Inventory> findByPartIdAndServiceCenter(@Param("partId") Long partId,
            @Param("serviceCenter") String serviceCenter);

    @Query("SELECT i FROM Inventory i WHERE i.quantityOnHand <= i.reorderPoint")
    List<Inventory> findLowStockItems();

    @Query("SELECT i FROM Inventory i WHERE i.serviceCenter = :serviceCenter AND i.quantityOnHand <= i.reorderPoint")
    List<Inventory> findLowStockItemsByServiceCenter(@Param("serviceCenter") String serviceCenter);
}


