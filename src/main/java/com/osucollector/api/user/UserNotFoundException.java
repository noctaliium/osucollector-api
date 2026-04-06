package com.osucollector.api.user;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String id) {
        super("User not found with id: " + id);
    }

    public UserNotFoundException(Integer osuUserId) {
        super("User not found with osu! id: " + osuUserId);
    }
}