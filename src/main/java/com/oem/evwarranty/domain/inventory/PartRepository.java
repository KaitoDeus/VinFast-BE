package com.oem.evwarranty.domain.inventory;


import com.oem.evwarranty.domain.inventory.Part;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Repository for Part entity operations.
 */
@Repository
public interface PartRepository extends JpaRepository<Part, Long> {

    Optional<Part> findByPartNumber(String partNumber);

    boolean existsByPartNumber(String partNumber);

    List<Part> findByCategory(Part.PartCategory category);

    List<Part> findByIsActiveTrue();

    @Query("SELECT p FROM Part p WHERE " +
            "LOWER(p.partNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Part> searchParts(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Part p WHERE p.modelCompatibility LIKE %:model%")
    List<Part> findCompatibleParts(@Param("model") String model);
}


