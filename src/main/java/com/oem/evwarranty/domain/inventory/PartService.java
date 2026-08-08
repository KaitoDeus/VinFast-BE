package com.oem.evwarranty.domain.inventory;


import com.oem.evwarranty.domain.inventory.Part;
import com.oem.evwarranty.domain.inventory.PartRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service for Part management operations.
 */
@Service
@Transactional
public class PartService {

    private final PartRepository partRepository;

    public PartService(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    public List<Part> findAll() {
        return partRepository.findAll();
    }

    public List<Part> findAllActive() {
        return partRepository.findByIsActiveTrue();
    }

    public Optional<Part> findById(@NonNull Long id) {
        return partRepository.findById(id);
    }

    public Optional<Part> findByPartNumber(String partNumber) {
        return partRepository.findByPartNumber(partNumber);
    }

    public Page<Part> findAll(@NonNull Pageable pageable) {
        return partRepository.findAll(pageable);
    }

    public Page<Part> searchParts(String search, @NonNull Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return partRepository.findAll(pageable);
        }
        return partRepository.searchParts(search, pageable);
    }

    public List<Part> findByCategory(Part.PartCategory category) {
        return partRepository.findByCategory(category);
    }

    public Part createPart(Part part) {
        if (partRepository.existsByPartNumber(part.getPartNumber())) {
            throw new IllegalArgumentException("Part number already exists");
        }
        return partRepository.save(part);
    }

    public Part updatePart(@NonNull Long id, Part updatedPart) {
        return partRepository.findById(id)
                .map(part -> {
                    part.setName(updatedPart.getName());
                    part.setDescription(updatedPart.getDescription());
                    part.setCategory(updatedPart.getCategory());
                    part.setPrice(updatedPart.getPrice());
                    part.setWarrantyMonths(updatedPart.getWarrantyMonths());
                    part.setManufacturer(updatedPart.getManufacturer());
                    part.setModelCompatibility(updatedPart.getModelCompatibility());
                    part.setIsActive(updatedPart.getIsActive());
                    part.setMinStockLevel(updatedPart.getMinStockLevel());
                    return partRepository.save(part);
                })
                .orElseThrow(() -> new IllegalArgumentException("Part not found"));
    }

    public void deletePart(@NonNull Long id) {
        partRepository.deleteById(id);
    }

    public void togglePartStatus(@NonNull Long id) {
        partRepository.findById(id).ifPresent(part -> {
            part.setIsActive(!part.getIsActive());
            partRepository.save(part);
        });
    }

    public long count() {
        return partRepository.count();
    }
}


