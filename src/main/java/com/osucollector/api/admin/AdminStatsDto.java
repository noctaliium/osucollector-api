package com.osucollector.api.admin;

public record AdminStatsDto(
        long totalUsers,
        long totalCards,
        long totalPacksOpened,
        long totalRainbowCards,
        TopCardDto mostOwnedCard,
        TopCardDto rarestOwnedCard
) {
    public record TopCardDto(
            String playerName,
            long   ownerCount
    ) {}
}