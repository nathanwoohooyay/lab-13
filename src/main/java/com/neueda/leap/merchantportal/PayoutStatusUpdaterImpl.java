package com.neueda.leap.merchantportal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Validates status values and state machine transitions before persisting.
 */
@Service
public class PayoutStatusUpdaterImpl implements PayoutStatusUpdater {

    private static final Logger logger = LoggerFactory.getLogger(PayoutStatusUpdaterImpl.class);

    private PayoutRepository payoutRepository;
    private AuditLogger auditLogger;

    @Autowired
    public PayoutStatusUpdaterImpl(PayoutRepository payoutRepository, AuditLogger auditLogger) {
        this.payoutRepository = payoutRepository;
        this.auditLogger = auditLogger;
    }

    @Override
    public void markSettled(Long payoutId, String status) {
        if (payoutId == null) {
            throw new IllegalArgumentException("Payout ID cannot be null");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        PayoutStatus newStatus;
        try {
            newStatus = PayoutStatus.fromValue(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            auditLogger.logAuthorizationFailure(null, "payout/" + payoutId, "Invalid status value: " + status);
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        PayoutRequest payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new IllegalArgumentException("Payout not found: " + payoutId));

        PayoutStatus currentStatus = payout.getApprovalStatus();

        if (!currentStatus.isValidTransition(newStatus)) {
            logger.warn("Invalid status transition for payout {}: {} -> {}", payoutId, currentStatus, newStatus);
            throw new InvalidStatusTransitionException(
                    "Invalid status transition: " + currentStatus + " -> " + newStatus);
        }

        payout.setApprovalStatus(newStatus);
        payoutRepository.save(payout);

        auditLogger.logStatusTransition(payoutId, currentStatus.getValue(), newStatus.getValue(), null);
    }
}
