package com.osucollector.api.card;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, length = 50)
    private String playerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gamemode gamemode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rarity rarity;

    @Column(precision = 10, scale = 2, nullable = true)
    private BigDecimal perfPoint;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    private String imageUrl;

    @Column(unique = true)
    private Integer osuUserId;

    @Column(length = 100)
    private String title; 

    @Column(length = 10)
    private String countryCode;

    @Column
    private Integer globalRank;

    @Column
    private Integer countryRank;

    @Column(precision = 6, scale = 2)
    private BigDecimal accuracy;

    @Column
    @Builder.Default
    private Boolean isRanked = true;

    @Column
    private Integer followerCount;

    @Column
    private Integer mappingFollowerCount;   // subscribers

    @Column
    private Integer badgeCount;

    @Column
    private Integer rankedMapCount;

    @Column
    private Integer lovedMapCount;

    @Column
    private Integer firstPlaceCount;

    public enum Gamemode {
        standard, taiko, catch_the_beat, mania
    }

    public enum Rarity {
        common, uncommon, rare, epic, legendary, special
    }
}
