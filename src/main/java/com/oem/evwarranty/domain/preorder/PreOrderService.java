package com.oem.evwarranty.domain.preorder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@Transactional
public class PreOrderService {

    private final PreOrderRepository preOrderRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PreOrderService(PreOrderRepository preOrderRepository) {
        this.preOrderRepository = preOrderRepository;
    }

    public Page<PreOrderDTO.Response> getPreOrders(String query, String status, Pageable pageable) {
        return preOrderRepository.searchPreOrders(query, status, pageable)
                .map(this::toResponse);
    }

    public Optional<PreOrderDTO.Response> getPreOrderById(Long id) {
        return preOrderRepository.findById(id)
                .map(this::toResponse);
    }

    public PreOrderDTO.Response createPreOrder(PreOrderDTO.CreateRequest request) {
        PreOrder preOrder = PreOrder.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .color(request.getColor() != null ? request.getColor() : "Crimson Red")
                .scooterModel(request.getScooterModel())
                .content(request.getContent())
                .status("PENDING")
                .build();

        PreOrder saved = preOrderRepository.save(preOrder);
        return toResponse(saved);
    }

    public PreOrderDTO.Response updateStatus(Long id, String newStatus) {
        PreOrder preOrder = preOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt cọc với ID: " + id));

        preOrder.setStatus(newStatus.toUpperCase());
        return toResponse(preOrderRepository.save(preOrder));
    }

    public PreOrderDTO.Response toResponse(PreOrder p) {
        if (p == null) return null;

        String formattedId = "PO-" + String.format("%03d", p.getId());
        String dateStr = p.getCreatedAt() != null ? p.getCreatedAt().format(FORMATTER) : "N/A";

        return PreOrderDTO.Response.builder()
                .id(formattedId)
                .numericId(p.getId())
                .fullName(p.getFullName())
                .phone(p.getPhone())
                .email(p.getEmail())
                .color(p.getColor())
                .scooterModel(p.getScooterModel())
                .content(p.getContent())
                .status(capitalize(p.getStatus()))
                .createdAt(dateStr)
                .build();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
