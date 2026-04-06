package com.osucollector.api.card;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<List<CardDto>> getAllCards() {
        return ResponseEntity.ok(cardService.getAllCards());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCardById(@PathVariable Short id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @GetMapping("/player/{playerName}")
    public ResponseEntity<CardDto> getCardByPlayerName(@PathVariable String playerName) {
        return ResponseEntity.ok(cardService.getCardByPlayerName(playerName));
    }

    @GetMapping("/gamemode/{gamemode}")
    public ResponseEntity<List<CardDto>> getCardsByGamemode(@PathVariable Card.Gamemode gamemode) {
        return ResponseEntity.ok(cardService.getCardsByGamemode(gamemode));
    }

    @GetMapping("/rarity/{rarity}")
    public ResponseEntity<List<CardDto>> getCardsByRarity(@PathVariable Card.Rarity rarity) {
        return ResponseEntity.ok(cardService.getCardsByRarity(rarity));
    }

    @GetMapping("/active")
    public ResponseEntity<List<CardDto>> getActiveCards() {
        return ResponseEntity.ok(cardService.getActiveCards());
    }
}