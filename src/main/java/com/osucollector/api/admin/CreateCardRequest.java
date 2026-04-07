package com.osucollector.api.admin;

import com.osucollector.api.card.Card;
import java.math.BigDecimal;

public record CreateCardRequest(
        String       playerName,
        Card.Gamemode gamemode,
        Card.Rarity   rarity,
        BigDecimal    perfPoint,
        Boolean       isActive,
        String        imageUrl,
        Integer       osuUserId
) {}