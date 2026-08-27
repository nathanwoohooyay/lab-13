package com.neueda.leap.merchantportal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PayoutApprovalService {

    private PayoutRepository payoutRepository;
    private SecurityUtils securityUtils;
    private AuditLogger auditLogger;

    @Autowired
    public PayoutApprovalService(PayoutRepository payoutRepository,
                                  SecurityUtils securityUtils,
                                  AuditLogger auditLogger) {
        this.payoutRepository = payoutRepository;
        this.securityUtils = securityUtils;
        this.auditLogger = auditLogger;
    }

    public void approve(Long payoutId, Long approvingUserId) {
        // Caller must hold the approver role and be approving on their own behalf.
        securityUtils.requireApproverRole();
        securityUtils.verifyCurrentUser(approvingUserId);

        PayoutRequest payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new RuntimeException("Payout not found"));

        payout.setApprovalStatus(PayoutStatus.APPROVED);
        payout.setApprovedByUserId(approvingUserId);
        payoutRepository.save(payout);

        auditLogger.logApproval(payoutId, approvingUserId, payout.getAmount());
    }
}
