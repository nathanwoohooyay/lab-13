package com.neueda.leap.merchantportal;

public class WebhookSignatureException extends RuntimeException {
    public WebhookSignatureException(String message) {
        super(message);
    }

    public WebhookSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
