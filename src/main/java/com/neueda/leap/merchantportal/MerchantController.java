package com.neueda.leap.merchantportal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MerchantController {

    @Autowired
    private PayoutRepository payoutRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping("/api/payouts/{payoutId}")
    public PayoutRequest getPayout(@PathVariable Long payoutId) {
        Long merchantId = securityUtils.getCurrentUserId();
        return payoutRepository.findByIdAndMerchantId(payoutId, merchantId)
                .orElseThrow(() -> new RuntimeException("Payout not found"));
    }
}
