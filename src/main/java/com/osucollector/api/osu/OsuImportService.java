package com.osucollector.api.osu;

import com.osucollector.api.card.Card;
import com.osucollector.api.card.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OsuImportService {

    private final OsuApiService  osuApiService;
    private final CardRepository cardRepository;

    public void importTop2000() throws InterruptedException {
        log.info("Starting top 2000 osu!catch import...");
        int imported = 0;
        int failed = 0;

        for (int page = 1; page <= 40; page++) {
            List<Map<String, Object>> ranking = osuApiService.fetchRankingPage(page);

            for (Map<String, Object> entry : ranking) {
                Integer osuUserId = ((Number) ((Map<?, ?>) entry.get("user")).get("id")).intValue();

                if (cardRepository.existsByOsuUserId(osuUserId)) continue;

                OsuUserStats stats = osuApiService.fetchFullUserStats(osuUserId);
                if (stats == null) continue;

                try {
                    saveCard(stats);
                    imported++;
                } catch (Exception e) {
                    log.warn("Failed to save card {}: {}", stats.username(), e.getMessage());
                    failed++;
                }

                Thread.sleep(100);
            }

            log.info("Page {}/40 done — {} cards imported so far", page, imported);
            Thread.sleep(200);
        }

        log.info("Import complete — {} cards imported", imported);
    }

    @Transactional
    public void saveCard(OsuUserStats stats) {
        Card card = buildCardFromStats(stats);
        cardRepository.save(card);
    }

    private Card buildCardFromStats(OsuUserStats stats) {
        return Card.builder()
                .playerName(stats.username())
                .gamemode(Card.Gamemode.catch_the_beat)
                .rarity(Card.Rarity.common)
                .perfPoint(stats.pp())
                .globalRank(stats.globalRank())
                .accuracy(stats.accuracy())
                .isRanked(stats.isRanked())
                .isActive(true)
                .imageUrl(stats.avatarUrl())
                .title(stats.title())
                .countryCode(stats.countryCode())
                .followerCount(stats.followerCount())
                .mappingFollowerCount(stats.mappingFollowerCount())
                .badgeCount(stats.badgeCount())
                .rankedMapCount(stats.rankedMapCount())
                .lovedMapCount(stats.lovedMapCount())
                .firstPlaceCount(stats.firstPlaceCount())
                .osuUserId(stats.osuUserId())
                .build();
    }
}