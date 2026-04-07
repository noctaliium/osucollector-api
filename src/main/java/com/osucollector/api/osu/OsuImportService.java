package com.osucollector.api.osu;

import com.osucollector.api.card.Card;
import com.osucollector.api.card.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OsuImportService {

    private final OsuApiService  osuApiService;
    private final CardRepository cardRepository;

    // ─── Import initial du top 2000 ──────────────────────────────────────────────

    @Transactional
    public void importTop2000() throws InterruptedException {
        log.info("Starting top 2000 osu!catch import...");
        int imported = 0;

        // 40 pages x 50 joueurs = 2000
        for (int page = 1; page <= 40; page++) {
            List<Map<String, Object>> ranking = osuApiService.fetchRankingPage(page);

            for (Map<String, Object> entry : ranking) {
                Integer osuUserId = ((Number) ((Map<?, ?>) entry.get("user")).get("id")).intValue();

                // Skip si la carte existe déjà
                if (cardRepository.existsByOsuUserId(osuUserId)) continue;

                OsuUserStats stats = osuApiService.fetchFullUserStats(osuUserId);
                if (stats == null) continue;

                Card card = buildCardFromStats(stats);
                cardRepository.save(card);
                imported++;

                // 100ms entre chaque requête pour rester bien sous la limite
                Thread.sleep(100);
            }

            log.info("Page {}/40 done — {} cards imported so far", page, imported);
            Thread.sleep(200); // pause entre les pages
        }

        log.info("Import complete — {} cards imported", imported);
    }

    private Card buildCardFromStats(OsuUserStats stats) {
        return Card.builder()
                .playerName(stats.username())
                .gamemode(Card.Gamemode.catch_the_beat)
                .rarity(Card.Rarity.common)   // rareté par défaut, à ajuster manuellement
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