package com.oem.evwarranty.domain.audit;


import com.oem.evwarranty.domain.audit.AuditLog;
import com.oem.evwarranty.domain.audit.AuditLogRepository;
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
        if (log == null)
            return;
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsForResource(String type, Long id) {
        return auditLogRepository.findByResourceTypeAndResourceIdOrderByCreatedAtDesc(type, id);
    }
}


