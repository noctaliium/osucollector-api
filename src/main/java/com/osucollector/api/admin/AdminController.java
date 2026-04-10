package com.osucollector.api.admin;

import com.osucollector.api.card.Card;
import com.osucollector.api.card.CardDto;
import com.osucollector.api.card.CardRepository;
import com.osucollector.api.osu.OsuApiService;
import com.osucollector.api.osu.OsuImportService;
import com.osucollector.api.osu.OsuUserStats;
import com.osucollector.api.pack.PackService;
import com.osucollector.api.user.User;
import com.osucollector.api.user.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminRepository adminRepository;
    private final CardRepository cardRepository;
    private final OsuImportService osuImportService;
    private final OsuApiService osuApiService;
    private final SyncStatusService syncStatusService;
    private final PackService packService;
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
    
    @PostMapping("/debug/force-rarity")
    public ResponseEntity<Void> forceNextRarity(@RequestParam Card.Rarity rarity) {
        packService.setForcedNextRarity(rarity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cards/{id}/blacklist")
    public ResponseEntity<Void> blacklistCard(@PathVariable Short id) {
        adminService.blacklistCard(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import/player/{osuUserId}")
    public ResponseEntity<Void> importSinglePlayer(@PathVariable Integer osuUserId) {
        new Thread(() -> {
            try {
                OsuUserStats stats = osuApiService.fetchFullUserStats(osuUserId);
                if (stats != null) osuImportService.saveCard(stats);
            } catch (Exception e) {
                log.warn("Failed to import player {}: {}", osuUserId, e.getMessage());
            }
        }).start();
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/import/next-unranked")
    public ResponseEntity<Void> importNextUnranked() {
        new Thread(() -> {
            try {
                for (int page = 41; page <= 50; page++) {
                    List<Map<String, Object>> ranking = osuApiService.fetchRankingPage(page);
                    for (Map<String, Object> entry : ranking) {
                        Integer osuUserId = ((Number) ((Map<?, ?>) entry.get("user")).get("id")).intValue();
                        if (!cardRepository.existsByOsuUserId(osuUserId)) {
                            OsuUserStats stats = osuApiService.fetchFullUserStats(osuUserId);
                            if (stats != null) osuImportService.saveCard(stats);
                            log.info("Imported replacement player: {}", stats.username());
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to import next unranked: {}", e.getMessage());
            }
        }).start();
        return ResponseEntity.accepted().build();
    }
}