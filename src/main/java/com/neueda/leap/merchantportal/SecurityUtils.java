package com.neueda.leap.merchantportal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Extracts and validates the authenticated user from the Spring Security context.
 */
@Component
public class SecurityUtils {

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user found in security context");
        }

        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Invalid user ID format: " + authentication.getName());
        }
    }

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role));
    }

    public void verifyCurrentUser(Long userId) {
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new UnauthorizedException(
                    "User " + currentUserId + " is not authorized to perform action for user " + userId
            );
        }
    }

    public void requireApproverRole() {
        if (!hasRole("ROLE_APPROVER")) {
            throw new UnauthorizedException("User does not have required ROLE_APPROVER permission");
        }
    }
}
