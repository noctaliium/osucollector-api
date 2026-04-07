package com.osucollector.api.usercard;

import com.osucollector.api.card.Card;
import com.osucollector.api.card.Mark;
import com.osucollector.api.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCard {

    @EmbeddedId
    private UserCardId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cardId")
    @JoinColumn(name = "card_id")
    private Card card;

    @Builder.Default
    @Column(nullable = false)
    private Short quantityNormal = 0;

    @Builder.Default
    @Column(nullable = false)
    private Short quantityFoil = 0;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<RainbowCard> rainbowCards;

    @Enumerated(EnumType.STRING)
    private Mark mark;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime firstObtainedAt = LocalDateTime.now();
}