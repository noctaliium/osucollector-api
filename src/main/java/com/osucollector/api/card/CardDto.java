package com.osucollector.api.card;

import java.math.BigDecimal;

public record CardDto(
    Short id,
    String playerName,
    Card.Gamemode gamemode,
    Card.Rarity rarity,
    BigDecimal perfPoint,
    Boolean isActive,
    String imageUrl
) {
    public static CardDto from(Card card) {
        return new CardDto(
            card.getId(),
            card.getPlayerName(),
            card.getGamemode(),
            card.getRarity(),
            card.getPerfPoint(),
            card.getIsActive(),
            card.getImageUrl()
        );
    }
}