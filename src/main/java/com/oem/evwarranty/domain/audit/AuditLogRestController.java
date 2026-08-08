package com.oem.evwarranty.domain.audit;

import com.oem.evwarranty.domain.user.User;

import com.oem.evwarranty.domain.user.UserService;

import com.oem.evwarranty.domain.audit.AuditLog;
import com.oem.evwarranty.domain.audit.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit API", description = "Endpoints for system audit logs and history")
public class AuditLogRestController {

    private final AuditLogService auditLogService;

    public AuditLogRestController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/resource/{type}/{id}")
    @Operation(summary = "Get audit trail for a specific resource (e.g., WARRANTY_CLAIM)")
    public ResponseEntity<List<AuditLog>> getResourceLogs(@PathVariable String type, @PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.getLogsForResource(type, id));
    }
}



