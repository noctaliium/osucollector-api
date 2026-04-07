package com.osucollector.api.card;

import java.math.BigDecimal;

public record CardDto(
        Short id,
        String playerName,
        String title,
        String countryCode,
        Card.Gamemode gamemode,
        Card.Rarity rarity,
        BigDecimal perfPoint,
        Integer globalRank,
        BigDecimal accuracy,
        Boolean isActive,
        Boolean isRanked,
        String imageUrl,
        Integer followerCount,
        Integer mappingFollowerCount,
        Integer badgeCount,
        Integer rankedMapCount,
        Integer lovedMapCount,
        Integer firstPlaceCount
) {
    public static CardDto from(Card card) {
        return new CardDto(
                card.getId(),
                card.getPlayerName(),
                card.getTitle(),
                card.getCountryCode(),
                card.getGamemode(),
                card.getRarity(),
                card.getPerfPoint(),
                card.getGlobalRank(),
                card.getAccuracy(),
                card.getIsActive(),
                card.getIsRanked(),
                card.getImageUrl(),
                card.getFollowerCount(),
                card.getMappingFollowerCount(),
                card.getBadgeCount(),
                card.getRankedMapCount(),
                card.getLovedMapCount(),
                card.getFirstPlaceCount()
        );
    }
}