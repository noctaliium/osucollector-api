package com.osucollector.api.admin;

import com.osucollector.api.card.CardDto;
import com.osucollector.api.osu.OsuApiService;
import com.osucollector.api.osu.OsuImportService;
import com.osucollector.api.user.User;
import com.osucollector.api.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OsuImportService osuImportService;
    private final OsuApiService osuApiService;
    private final SyncStatusService syncStatusService;
    private final RarityScoreService rarityScoreService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getGlobalStats() {
        return ResponseEntity.ok(adminService.getGlobalStats());
    }

    @PostMapping("/cards")
    public ResponseEntity<CardDto> createCard(@RequestBody CreateCardRequest request) {
        return ResponseEntity.status(201).body(adminService.createCard(request));
    }

    @PatchMapping("/cards/{id}")
    public ResponseEntity<CardDto> updateCard(
            @PathVariable Short id,
            @RequestBody CreateCardRequest request) {
        return ResponseEntity.ok(adminService.updateCard(id, request));
    }

    @PatchMapping("/cards/{id}/toggle")
    public ResponseEntity<Void> toggleCardActive(@PathVariable Short id) {
        adminService.toggleCardActive(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserDto> updateUserRole(
            @PathVariable String id,
            @RequestParam User.Role role) {
        return ResponseEntity.ok(adminService.updateUserRole(id, role));
    }

    @PostMapping("/import/top2000")
    public ResponseEntity<Void> importTop2000() {
        // Start the import in a new thread to work in the background
        new Thread(() -> {
            try {
                osuImportService.importTop2000();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/sync-stats")
    public ResponseEntity<String> syncStats() {
        if (syncStatusService.isSyncRunning()) {
            return ResponseEntity.status(409).body("Sync already running");
        }

        new Thread(() -> osuApiService.manualStatsUpdate()).start();
        return ResponseEntity.accepted().body("Sync started");
    }

    @GetMapping("/sync-stats/stream")
    public SseEmitter streamSyncStatus() {
        return syncStatusService.createEmitter();
    }

    @GetMapping("/cards/scores")
    public ResponseEntity<List<CardRarityDto>> getCardsWithScores() {
        return ResponseEntity.ok(adminService.getCardsWithScores());
    }

    @GetMapping("/score-weights")
    public ResponseEntity<ScoreWeightsDto> getScoreWeights() {
        return ResponseEntity.ok(rarityScoreService.getWeights());
    }

    @PostMapping("/cards/apply-suggestions")
    public ResponseEntity<Integer> applySuggestions() {
        return ResponseEntity.ok(adminService.applyAllRaritySuggestions());
    }
}