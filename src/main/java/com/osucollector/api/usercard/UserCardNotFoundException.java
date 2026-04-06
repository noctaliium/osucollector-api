package com.osucollector.api.usercard;

public class UserCardNotFoundException extends RuntimeException {
    public UserCardNotFoundException(String userId, Short cardId) {
        super("Card " + cardId + " not found in collection of user " + userId);
    }
}