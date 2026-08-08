package com.oem.evwarranty.domain.audit;


import com.oem.evwarranty.domain.audit.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByResourceTypeAndResourceIdOrderByCreatedAtDesc(String resourceType, Long resourceId);

    List<AuditLog> findByUsernameOrderByCreatedAtDesc(String username);
}


