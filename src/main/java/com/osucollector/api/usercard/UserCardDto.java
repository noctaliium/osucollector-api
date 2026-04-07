package com.osucollector.api.usercard;

import com.osucollector.api.card.CardDto;
import com.osucollector.api.card.Mark;
import java.time.LocalDateTime;
import java.util.List;

public record UserCardDto(
        CardDto card,
        Short quantityNormal,
        Short quantityFoil,
        List<RainbowCardDto> rainbowCards,
        Mark mark,
        LocalDateTime firstObtainedAt
) {
    public static UserCardDto from(UserCard userCard) {
        return new UserCardDto(
                CardDto.from(userCard.getCard()),
                userCard.getQuantityNormal(),
                userCard.getQuantityFoil(),
                userCard.getRainbowCards().stream()
                        .map(RainbowCardDto::from)
                        .toList(),
                userCard.getMark(),
                userCard.getFirstObtainedAt()
        );
    }
}