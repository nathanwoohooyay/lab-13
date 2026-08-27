package com.neueda.leap.merchantportal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized audit trail logging for payout financial operations.
 */
@Component
public class AuditLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public void logApproval(Long payoutId, Long approvingUserId, BigDecimal amount) {
        auditLog.info("APPROVAL | PayoutId: {} | ApprovingUserId: {} | Amount: {} | Timestamp: {}",
                payoutId, approvingUserId, amount, LocalDateTime.now().format(formatter));
    }

    public void logWebhook(Long payoutId, String status, String sourceIP, boolean signatureValid, String idempotencyKey) {
        auditLog.info("WEBHOOK | PayoutId: {} | Status: {} | SourceIP: {} | SignatureValid: {} | IdempotencyKey: {} | Timestamp: {}",
                payoutId, status, sourceIP, signatureValid, idempotencyKey, LocalDateTime.now().format(formatter));
    }

    public void logStatusTransition(Long payoutId, String fromStatus, String toStatus, Long userId) {
        auditLog.info("STATUS_TRANSITION | PayoutId: {} | FromStatus: {} | ToStatus: {} | UserId: {} | Timestamp: {}",
                payoutId, fromStatus, toStatus, userId, LocalDateTime.now().format(formatter));
    }

    public void logAuthorizationFailure(Long userId, String resource, String reason) {
        auditLog.warn("AUTHORIZATION_FAILURE | UserId: {} | Resource: {} | Reason: {} | Timestamp: {}",
                userId, resource, reason, LocalDateTime.now().format(formatter));
    }

    public void logWebhookSignatureFailure(String sourceIP, String reason) {
        auditLog.warn("WEBHOOK_SIGNATURE_FAILURE | SourceIP: {} | Reason: {} | Timestamp: {}",
                sourceIP, reason, LocalDateTime.now().format(formatter));
    }

    public void logDuplicateWebhook(String idempotencyKey, Long payoutId) {
        auditLog.info("DUPLICATE_WEBHOOK | IdempotencyKey: {} | PayoutId: {} | Timestamp: {}",
                idempotencyKey, payoutId, LocalDateTime.now().format(formatter));
    }
}
