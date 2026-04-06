package com.osucollector.api.user;

import java.time.LocalDateTime;

public record UserDto(
        String id,
        String username,
        Integer osuUserId,
        User.Role role,
        Short stackedPacks,
        Integer totalPacksOpened,
        LocalDateTime lastTradeAt,
        Integer coins
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getOsuUserId(),
                user.getRole(),
                user.getStackedPacks(),
                user.getTotalPacksOpened(),
                user.getLastTradeAt(),
                user.getCoins()
        );
    }
}