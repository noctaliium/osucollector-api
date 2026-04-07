package com.osucollector.api.usercard;

import com.osucollector.api.card.Card;
import com.osucollector.api.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rainbow_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RainbowCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Numéro global unique par carte (ex: Motion n°001)
    @Column(nullable = false)
    private Integer serialNumber;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime obtainedAt = LocalDateTime.now();
}