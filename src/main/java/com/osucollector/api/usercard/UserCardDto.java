package com.osucollector.api.usercard;

import com.osucollector.api.card.CardDto;
import com.osucollector.api.card.Mark;
import java.time.LocalDateTime;

public record UserCardDto(
        CardDto card,
        Short quantityNormal,
        Short quantityFoil,
        Mark mark,
        Boolean favorite,
        LocalDateTime firstObtainedAt
) {
    public static UserCardDto from(UserCard userCard) {
        return new UserCardDto(
                CardDto.from(userCard.getCard()),
                userCard.getQuantityNormal(),
                userCard.getQuantityFoil(),
                userCard.getMark(),
                userCard.getFavorite(),
                userCard.getFirstObtainedAt()
        );
    }
}