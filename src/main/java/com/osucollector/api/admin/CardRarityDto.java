package com.osucollector.api.admin;

import com.osucollector.api.card.Card;
import java.math.BigDecimal;

public record CardRarityDto(
        Short          id,
        String         playerName,
        String         countryCode,
        Card.Rarity    rarity,
        BigDecimal     perfPoint,
        Integer        globalRank,
        Integer        followerCount,
        Integer        mappingFollowerCount,
        Integer        badgeCount,
        Integer        rankedMapCount,
        Integer        lovedMapCount,
        Integer        firstPlaceCount,
        String         title,
        int            score,
        Card.Rarity    suggestedRarity
) {
    public static CardRarityDto from(Card card, int score, Card.Rarity suggested) {
        return new CardRarityDto(
                card.getId(),
                card.getPlayerName(),
                card.getCountryCode(),
                card.getRarity(),
                card.getPerfPoint(),
                card.getGlobalRank(),
                card.getFollowerCount(),
                card.getMappingFollowerCount(),
                card.getBadgeCount(),
                card.getRankedMapCount(),
                card.getLovedMapCount(),
                card.getFirstPlaceCount(),
                card.getTitle(),
                score,
                suggested
        );
    }
}