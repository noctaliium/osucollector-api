package com.osucollector.api.osu;

import com.osucollector.api.admin.SyncStatusService;
import com.osucollector.api.card.Card;
import com.osucollector.api.card.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OsuApiService {

    private final CardRepository     cardRepository;
    private final RestClient.Builder restClientBuilder;
    private final SyncStatusService syncStatusService;

    @Value("${osu.client-id}")
    private String clientId;

    @Value("${osu.client-secret}")
    private String clientSecret;

    private String accessToken;

    private void refreshAccessToken() {
        RestClient client = restClientBuilder.build();

        Map response = client.post()
                .uri("https://osu.ppy.sh/oauth/token")
                .body(Map.of(
                        "client_id",     clientId,
                        "client_secret", clientSecret,
                        "grant_type",    "client_credentials",
                        "scope",         "public"
                ))
                .retrieve()
                .body(Map.class);

        accessToken = (String) response.get("access_token");
        log.info("osu! access token refreshed");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchRankingPage(int page) {
        if (accessToken == null) refreshAccessToken();

        try {
            RestClient client = restClientBuilder.build();

            Map<String, Object> response = client.get()
                    .uri("https://osu.ppy.sh/api/v2/rankings/fruits/performance?page={page}", page)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            return (List<Map<String, Object>>) response.get("ranking");

        } catch (Exception e) {
            log.warn("Failed to fetch ranking page {}: {}", page, e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("401")) {
                refreshAccessToken();
            }
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public OsuUserStats fetchFullUserStats(Integer osuUserId) {
        if (accessToken == null) refreshAccessToken();

        try {
            RestClient client = restClientBuilder.build();

            Map<String, Object> response = client.get()
                    .uri("https://osu.ppy.sh/api/v2/users/{id}/fruits", osuUserId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            return OsuUserStats.fromApiResponse(response);

        } catch (Exception e) {
            log.warn("Failed to fetch full stats for user {}: {}", osuUserId, e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("401")) {
                refreshAccessToken();
            }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public OsuUserStats fetchUserStats(Integer osuUserId, Card.Gamemode gamemode) {
        if (accessToken == null) refreshAccessToken();

        try {
            RestClient client = restClientBuilder.build();
            String mode = gamemodeToApiMode(gamemode);

            Map<String, Object> response = client.get()
                    .uri("https://osu.ppy.sh/api/v2/users/{id}/{mode}", osuUserId, mode)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            return OsuUserStats.fromApiResponse(response);

        } catch (Exception e) {
            log.warn("Failed to fetch stats for user {}: {}", osuUserId, e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("401")) {
                refreshAccessToken();
            }
            return null;
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledStatsUpdate() {
        manualStatsUpdate();
    }

    public void manualStatsUpdate() {
        if (!syncStatusService.startSync()) {
            log.warn("Sync already running, skipping");
            return;
        }

        log.info("Starting osu! stats update...");
        List<Card> cards = cardRepository.findAll();
        int total   = cards.size();
        int updated = 0;
        int failed  = 0;

        for (Card card : cards) {
            if (card.getOsuUserId() == null) continue;

            try {
                OsuUserStats stats = fetchUserStats(card.getOsuUserId(), card.getGamemode());

                if (stats != null) {
                    card.setPerfPoint(stats.pp());
                    card.setGlobalRank(stats.globalRank());
                    card.setCountryRank(stats.countryRank());
                    card.setAccuracy(stats.accuracy());
                    card.setIsRanked(stats.isRanked());
                    card.setImageUrl(stats.avatarUrl());
                    card.setFollowerCount(stats.followerCount());
                    card.setMappingFollowerCount(stats.mappingFollowerCount());
                    card.setBadgeCount(stats.badgeCount());
                    card.setRankedMapCount(stats.rankedMapCount());
                    card.setLovedMapCount(stats.lovedMapCount());
                    card.setFirstPlaceCount(stats.firstPlaceCount());
                    cardRepository.save(card);
                    updated++;
                }

                // Send progress update every 50 cards
                if (updated % 50 == 0) {
                    syncStatusService.sendProgress(updated, total);
                }

                Thread.sleep(50);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("Failed to update card {}: {}", card.getPlayerName(), e.getMessage());
                failed++;
            }
        }

        syncStatusService.sendComplete(updated, failed);
        syncStatusService.endSync();
        log.info("Stats update complete — updated: {}, failed: {}", updated, failed);
    }

    private String gamemodeToApiMode(Card.Gamemode gamemode) {
        return switch (gamemode) {
            case standard       -> "osu";
            case taiko          -> "taiko";
            case catch_the_beat -> "fruits";
            case mania          -> "mania";
        };
    }
}