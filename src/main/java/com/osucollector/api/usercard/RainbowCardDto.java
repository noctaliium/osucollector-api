package com.osucollector.api.usercard;

import java.time.LocalDateTime;

public record RainbowCardDto(
        String id,
        Integer serialNumber,
        LocalDateTime obtainedAt
) {
    public static RainbowCardDto from(RainbowCard rainbowCard) {
        return new RainbowCardDto(
                rainbowCard.getId(),
                rainbowCard.getSerialNumber(),
                rainbowCard.getObtainedAt()
        );
    }
}