package com.osucollector.api.admin;

import com.osucollector.api.card.Card;
import java.math.BigDecimal;

public record CreateCardRequest(
        String        playerName,
        String        title,
        String        countryCode,
        Card.Gamemode gamemode,
        Card.Rarity   rarity,
        BigDecimal    perfPoint,
        Integer       globalRank,
        BigDecimal    accuracy,
        Boolean       isActive,
        Boolean       isRanked,
        String        imageUrl,
        Integer       osuUserId,
        Integer       followerCount,
        Integer       mappingFollowerCount,
        Integer       badgeCount,
        Integer       rankedMapCount,
        Integer       lovedMapCount,
        Integer       firstPlaceCount
) {}