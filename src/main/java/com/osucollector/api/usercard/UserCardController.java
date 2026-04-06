package com.osucollector.api.usercard;

import com.osucollector.api.card.Card;
import com.osucollector.api.card.Mark;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/cards")
@RequiredArgsConstructor
public class UserCardController {

    private final UserCardService userCardService;

    @GetMapping
    public ResponseEntity<List<UserCardDto>> getUserCollection(@PathVariable String userId) {
        return ResponseEntity.ok(userCardService.getUserCollection(userId));
    }

    @GetMapping("/rarity/{rarity}")
    public ResponseEntity<List<UserCardDto>> getByRarity(
            @PathVariable String userId,
            @PathVariable Card.Rarity rarity) {
        return ResponseEntity.ok(userCardService.getUserCollectionByRarity(userId, rarity));
    }

    @GetMapping("/gamemode/{gamemode}")
    public ResponseEntity<List<UserCardDto>> getByGamemode(
            @PathVariable String userId,
            @PathVariable Card.Gamemode gamemode) {
        return ResponseEntity.ok(userCardService.getUserCollectionByGamemode(userId, gamemode));
    }

    @GetMapping("/mark/{mark}")
    public ResponseEntity<List<UserCardDto>> getByMark(
            @PathVariable String userId,
            @PathVariable Mark mark) {
        return ResponseEntity.ok(userCardService.getUserCollectionByMark(userId, mark));
    }

    @PatchMapping("/{cardId}/mark")
    public ResponseEntity<UserCardDto> updateMark(
            @PathVariable String userId,
            @PathVariable Short cardId,
            @RequestParam Mark mark) {
        return ResponseEntity.ok(userCardService.updateMark(userId, cardId, mark));
    }

    @DeleteMapping("/{cardId}/mark")
    public ResponseEntity<UserCardDto> removeMark(
            @PathVariable String userId,
            @PathVariable Short cardId) {
        return ResponseEntity.ok(userCardService.removeMark(userId, cardId));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUniqueCardCount(@PathVariable String userId) {
        return ResponseEntity.ok(userCardService.getUniqueCardCount(userId));
    }
}