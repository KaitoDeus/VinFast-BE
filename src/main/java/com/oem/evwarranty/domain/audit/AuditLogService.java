package com.oem.evwarranty.domain.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String username, String action, String resourceType, Long resourceId, String details) {
        AuditLog log = AuditLog.builder()
                .username(username)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsForResource(String type, Long id) {
        return auditLogRepository.findByResourceTypeAndResourceIdOrderByCreatedAtDesc(type, id);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsForUser(String username) {
        return auditLogRepository.findByUsernameOrderByCreatedAtDesc(username);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getLogsForUser(String username, Pageable pageable) {
        return auditLogRepository.findByUsername(username, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> findAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
}
