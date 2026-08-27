package com.neueda.leap.merchantportal;

import java.math.BigDecimal;

public class PayoutRequest {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000.00");

    private Long id;
    private Long merchantId;
    private Long requestedByUserId;
    private PayoutStatus approvalStatus;
    private Long approvedByUserId;
    private BigDecimal amount;

    public PayoutRequest(Long id, Long merchantId, Long requestedByUserId, BigDecimal amount) {
        this.id = id;
        this.merchantId = merchantId;
        this.requestedByUserId = requestedByUserId;
        this.amount = validateAmount(amount);
        this.approvalStatus = PayoutStatus.PENDING;
    }

    private static BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new IllegalArgumentException("Amount exceeds maximum allowed: " + MAX_AMOUNT);
        }
        return amount;
    }

    public Long getId() { return id; }
    public Long getMerchantId() { return merchantId; }
    public Long getRequestedByUserId() { return requestedByUserId; }
    public PayoutStatus getApprovalStatus() { return approvalStatus; }

    /**
     * Rejects any transition that doesn't follow the payout state machine.
     */
    public void setApprovalStatus(PayoutStatus status) {
        if (!this.approvalStatus.isValidTransition(status)) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition from " + this.approvalStatus + " to " + status);
        }
        this.approvalStatus = status;
    }

    public void setApprovalStatus(String statusValue) {
        setApprovalStatus(PayoutStatus.fromValue(statusValue));
    }

    public Long getApprovedByUserId() { return approvedByUserId; }
    public void setApprovedByUserId(Long id) { this.approvedByUserId = id; }
    public BigDecimal getAmount() { return amount; }
}
