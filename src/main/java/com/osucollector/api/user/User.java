package com.osucollector.api.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true)
    private Integer osuUserId;

    @Column(length = 512)
    private String osuRefreshToken;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.player;

    @Builder.Default
    @Column(nullable = false)
    private Short stackedPacks = 0;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime lastPackTickAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private Integer totalPacksOpened = 0;

    private LocalDateTime lastTradeAt;

    @Builder.Default
    @Column(nullable = false)
    private Integer coins = 0;

    public enum Role {
        player, admin
    }
}