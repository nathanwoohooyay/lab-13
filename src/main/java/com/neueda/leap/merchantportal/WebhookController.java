package com.neueda.leap.merchantportal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    private static final String SIGNATURE_ALGORITHM = "HmacSHA256";

    // Tracks processed idempotency keys to reject webhook replays.
    private static final Set<String> processedWebhookIds = ConcurrentHashMap.newKeySet();

    private final PayoutStatusUpdater payoutStatusUpdater;
    private final AuditLogger auditLogger;

    @Value("${webhook.secret.key:change-me-in-production}")
    private String webhookSecretKey;

    @Autowired
    public WebhookController(PayoutStatusUpdater payoutStatusUpdater, AuditLogger auditLogger) {
        this.payoutStatusUpdater = payoutStatusUpdater;
        this.auditLogger = auditLogger;
    }

    @PostMapping("/api/webhooks/payment-status")
    public ResponseEntity<String> handlePaymentStatusWebhook(
            @RequestBody PaymentStatusEvent event,
            @RequestHeader(value = "Authorization", required = false) String signature,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-Forwarded-For", required = false) String sourceIP) {

        String clientIP = sourceIP != null ? sourceIP : "unknown";

        if (event == null || event.getPayoutId() == null) {
            return ResponseEntity.badRequest().body("Invalid event data");
        }

        if (!isValidSignature(event, signature)) {
            auditLogger.logWebhookSignatureFailure(clientIP, "Invalid HMAC signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid webhook signature");
        }

        if (idempotencyKey != null && !processedWebhookIds.add(idempotencyKey)) {
            auditLogger.logDuplicateWebhook(idempotencyKey, event.getPayoutId());
            return ResponseEntity.ok("Webhook already processed");
        }

        auditLogger.logWebhook(event.getPayoutId(), event.getStatus(), clientIP, true, idempotencyKey);

        try {
            payoutStatusUpdater.markSettled(event.getPayoutId(), event.getStatus());
            return ResponseEntity.ok("Webhook processed");
        } catch (InvalidStatusTransitionException | IllegalArgumentException e) {
            logger.warn("Webhook rejected for payout {}: {}", event.getPayoutId(), e.getMessage());
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        }
    }

    private boolean isValidSignature(PaymentStatusEvent event, String providedSignature) {
        if (providedSignature == null || providedSignature.trim().isEmpty()) {
            return false;
        }

        try {
            String signatureValue = providedSignature.replace("Bearer ", "").trim();
            String payload = "payoutId=" + event.getPayoutId() + "&status=" + event.getStatus();
            String calculatedSignature = calculateHmacSha256(payload, webhookSecretKey);
            return constantTimeEquals(signatureValue, calculatedSignature);
        } catch (Exception e) {
            logger.error("Error validating webhook signature", e);
            return false;
        }
    }

    private String calculateHmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance(SIGNATURE_ALGORITHM);
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), SIGNATURE_ALGORITHM));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private boolean constantTimeEquals(String a, String b) {
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

        if (aBytes.length != bBytes.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        return result == 0;
    }
}
