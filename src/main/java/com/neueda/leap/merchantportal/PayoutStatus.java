package com.neueda.leap.merchantportal;

/**
 * Valid payout lifecycle statuses with enforced state machine transitions.
 */
public enum PayoutStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    PAID("PAID"),
    SETTLED("SETTLED");

    private final String value;

    PayoutStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PayoutStatus fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Status value cannot be null");
        }

        for (PayoutStatus status : PayoutStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid payout status: " + value);
    }

    /**
     * Valid transitions: PENDING->APPROVED/REJECTED, APPROVED->PAID, PAID->SETTLED.
     */
    public boolean isValidTransition(PayoutStatus targetStatus) {
        if (this == targetStatus) {
            return true;
        }

        switch (this) {
            case PENDING:
                return targetStatus == APPROVED || targetStatus == REJECTED;
            case APPROVED:
                return targetStatus == PAID;
            case PAID:
                return targetStatus == SETTLED;
            case REJECTED:
            case SETTLED:
                return false;
            default:
                return false;
        }
    }
}
