package com.oem.evwarranty.common.aspect;

import com.oem.evwarranty.common.annotation.Auditable;
import com.oem.evwarranty.domain.audit.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Aspect for automatically intercepting methods annotated with @Auditable
 * and persisting audit log records to PostgreSQL.
 */
@Aspect
@Component
public class AuditAspect {

    private final AuditLogService auditLogService;

    public AuditAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            String username = getCurrentUsername();
            Long resourceId = extractResourceId(joinPoint.getArgs(), result);
            String details = "Executed " + joinPoint.getSignature().getName() + " with args: " + Arrays.toString(joinPoint.getArgs());

            auditLogService.log(
                    username,
                    auditable.action(),
                    auditable.resourceType(),
                    resourceId,
                    details
            );
        } catch (Exception ignored) {
            // Audit logging failure should never disrupt primary business logic execution
        }

        return result;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    private Long extractResourceId(Object[] args, Object result) {
        if (result != null) {
            Long id = tryExtractId(result);
            if (id != null) return id;
        }

        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof Long) {
                    return (Long) arg;
                }
                Long id = tryExtractId(arg);
                if (id != null) return id;
            }
        }

        return null;
    }

    private Long tryExtractId(Object target) {
        if (target == null) return null;
        try {
            Method getIdMethod = target.getClass().getMethod("getId");
            Object val = getIdMethod.invoke(target);
            if (val instanceof Long) {
                return (Long) val;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
