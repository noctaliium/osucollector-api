package com.osucollector.api.card;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CardRepository extends JpaRepository<Card, Short> {
    List<Card> findByGamemode(Card.Gamemode gamemode);
    List<Card> findByRarity(Card.Rarity rarity);
    List<Card> findByIsActiveTrue();
    Card findByPlayerName(String playerName);
}