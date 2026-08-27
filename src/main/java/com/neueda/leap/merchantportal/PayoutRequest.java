package com.neueda.leap.merchantportal;

public class PayoutRequest {
    private Long id;
    private Long merchantId;
    private Long requestedByUserId;
    private PayoutStatus approvalStatus;
    private Long approvedByUserId;
    private double amount;

    public PayoutRequest(Long id, Long merchantId, Long requestedByUserId, double amount) {
        this.id = id;
        this.merchantId = merchantId;
        this.requestedByUserId = requestedByUserId;
        this.amount = amount;
        this.approvalStatus = PayoutStatus.PENDING;
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
    public double getAmount() { return amount; }
}
