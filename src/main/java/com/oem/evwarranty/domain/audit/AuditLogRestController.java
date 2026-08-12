package com.oem.evwarranty.domain.audit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit API", description = "REST Endpoints for System Audit Logs and Activity History")
public class AuditLogRestController {

    private final AuditLogService auditLogService;

    public AuditLogRestController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF')")
    @Operation(summary = "Get Paginated Audit Logs", description = "Retrieve all system audit log records with pagination and sorting")
    public ResponseEntity<Page<AuditLog>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLog> logs = auditLogService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/resource/{type}/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF', 'SC_STAFF')")
    @Operation(summary = "Get Resource Audit Trail", description = "Get audit trail entries for a specific resource (e.g., WARRANTY_CLAIM)")
    public ResponseEntity<List<AuditLog>> getResourceLogs(@PathVariable String type, @PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.getLogsForResource(type, id));
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF')")
    @Operation(summary = "Get User Audit Activity", description = "Retrieve audit log entries for a specific username")
    public ResponseEntity<List<AuditLog>> getUserLogs(@PathVariable String username) {
        return ResponseEntity.ok(auditLogService.getLogsForUser(username));
    }
}
