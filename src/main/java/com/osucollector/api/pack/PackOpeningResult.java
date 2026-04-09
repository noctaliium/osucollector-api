package com.osucollector.api.pack;

import com.osucollector.api.card.Variant;
import com.osucollector.api.usercard.UserCardDto;
import java.util.List;

public record PackOpeningResult(
        List<DrawnCard> cards,
        int coinsEarned
) {
    public record DrawnCard(
            UserCardDto userCard,
            boolean isNew,
            Variant variant,
            Integer serialNumber
    ) {}
}