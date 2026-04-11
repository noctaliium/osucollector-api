package com.osucollector.api.user;


public record UserDto(
        String     id,
        String     username,
        Integer    osuUserId,
        User.Role  role,
        Short      stackedPacks,
        Integer    totalPacksOpened,
        String     lastTradeAt,
        String     lastPackTickAt,
        Integer    coins
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId().toString(),
                user.getUsername(),
                user.getOsuUserId(),
                user.getRole(),
                user.getStackedPacks(),
                user.getTotalPacksOpened(),
                user.getLastTradeAt()     != null ? user.getLastTradeAt().toString()     : null,
                user.getLastPackTickAt()  != null ? user.getLastPackTickAt().toString()  : null,
                user.getCoins()
        );
    }
}