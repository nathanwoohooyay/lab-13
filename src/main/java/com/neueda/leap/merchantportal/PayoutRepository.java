package com.neueda.leap.merchantportal;

import java.util.List;
import java.util.Optional;

public interface PayoutRepository {
    Optional<PayoutRequest> findById(Long payoutId);

    /** Scopes lookup to payouts owned by the given merchant. */
    Optional<PayoutRequest> findByIdAndMerchantId(Long payoutId, Long merchantId);

    /** Scopes lookup to payouts requested by the given user. */
    Optional<PayoutRequest> findByIdAndRequestedByUserId(Long payoutId, Long requestedByUserId);

    List<PayoutRequest> findAllApproved();

    PayoutRequest save(PayoutRequest payout);
}
