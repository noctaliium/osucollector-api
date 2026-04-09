package com.osucollector.api.admin;

import com.osucollector.api.card.Card;
import com.osucollector.api.card.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RarityScoreService {

    private final CardRepository cardRepository;

    // Constants for score calc
    private static final double PP_DIVISOR              = 3.0;
    private static final int    PP_MAX                  = 5000;
    private static final double FOLLOWERS_DIVISOR       = 5.0;
    private static final int    FOLLOWERS_MAX           = 2000;
    private static final double MAPPING_FOL_DIVISOR     = 2.0;
    private static final int    MAPPING_FOL_MAX         = 1000;
    private static final int    BADGE_PTS               = 100;
    private static final int    BADGES_MAX              = 500;
    private static final int    TITLE_PTS               = 300;
    private static final double MAPS_MULTIPLIER         = 10.0;
    private static final int    MAPS_MAX                = 500;
    private static final double FIRST_PLACES_MULTIPLIER = 2.0;
    private static final int    FIRST_PLACES_MAX        = 500;

    // Rarity ratios
    private static final double EPIC_RATIO     = 0.05;   // top 5%
    private static final double RARE_RATIO     = 0.15;   // top 15%
    private static final double UNCOMMON_RATIO = 0.30;   // top 30%

    public int calculateScore(Card card) {
        int score = 0;

        if (card.getPerfPoint() != null)
            score += (int) Math.min(card.getPerfPoint().doubleValue() / PP_DIVISOR, PP_MAX);

        if (card.getFollowerCount() != null)
            score += (int) Math.min(card.getFollowerCount() / FOLLOWERS_DIVISOR, FOLLOWERS_MAX);

        if (card.getMappingFollowerCount() != null)
            score += (int) Math.min(card.getMappingFollowerCount() / MAPPING_FOL_DIVISOR, MAPPING_FOL_MAX);

        if (card.getBadgeCount() != null)
            score += Math.min(card.getBadgeCount() * BADGE_PTS, BADGES_MAX);

        if (card.getTitle() != null && !card.getTitle().isBlank())
            score += TITLE_PTS;

        int maps = 0;
        if (card.getRankedMapCount() != null) maps += card.getRankedMapCount();
        if (card.getLovedMapCount()   != null) maps += card.getLovedMapCount();
        score += (int) Math.min(maps * MAPS_MULTIPLIER, MAPS_MAX);

        if (card.getFirstPlaceCount() != null)
            score += (int) Math.min(card.getFirstPlaceCount() * FIRST_PLACES_MULTIPLIER, FIRST_PLACES_MAX);

        return score;
    }

    // frontend data display
    public ScoreWeightsDto getWeights() {
        return new ScoreWeightsDto(
                PP_DIVISOR,   PP_MAX,
                FOLLOWERS_DIVISOR,   FOLLOWERS_MAX,
                MAPPING_FOL_DIVISOR, MAPPING_FOL_MAX,
                BADGE_PTS,    BADGES_MAX,
                TITLE_PTS,
                MAPS_MULTIPLIER,     MAPS_MAX,
                FIRST_PLACES_MULTIPLIER, FIRST_PLACES_MAX
        );
    }

    // Suggest based on distrib
    public Card.Rarity suggestRarity(int score, List<Integer> allScores) {
        List<Integer> sorted = allScores.stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        int total = sorted.size();
        if (total == 0) return Card.Rarity.common;

        int epicIdx     = (int) Math.round(total * EPIC_RATIO) - 1;
        int rareIdx     = (int) Math.round(total * EPIC_RATIO + total * RARE_RATIO) - 1;
        int uncommonIdx = (int) Math.round(total * EPIC_RATIO + total * RARE_RATIO + total * UNCOMMON_RATIO) - 1;

        int epicMin     = sorted.get(Math.min(epicIdx,     total - 1));
        int rareMin     = sorted.get(Math.min(rareIdx,     total - 1));
        int uncommonMin = sorted.get(Math.min(uncommonIdx, total - 1));

        if (score >= epicMin)     return Card.Rarity.epic;
        if (score >= rareMin)     return Card.Rarity.rare;
        if (score >= uncommonMin) return Card.Rarity.uncommon;
        return Card.Rarity.common;
    }

    public int applyAllSuggestions() {
        List<Card> eligibleCards = cardRepository.findAll().stream()
                .filter(c -> c.getRarity() != Card.Rarity.epic      &&
                            c.getRarity() != Card.Rarity.legendary  &&
                            c.getRarity() != Card.Rarity.special)
                .sorted((a, b) -> Integer.compare(
                    calculateScore(b), calculateScore(a)))
                .toList();

        int updated = 0;

        int total         = eligibleCards.size();
        int epicCount     = (int) Math.round(total * EPIC_RATIO);
        int rareCount     = (int) Math.round(total * RARE_RATIO);
        int uncommonCount = (int) Math.round(total * UNCOMMON_RATIO);

        for (int i = 0; i < eligibleCards.size(); i++) {
            Card card = eligibleCards.get(i);
            Card.Rarity newRarity;

            if (i < epicCount) newRarity = Card.Rarity.epic;
            else if (i < epicCount + rareCount) newRarity = Card.Rarity.rare;
            else if (i < epicCount + rareCount + uncommonCount) newRarity = Card.Rarity.uncommon;
            else newRarity = Card.Rarity.common;

            if (card.getRarity() != newRarity) {
                card.setRarity(newRarity);
                cardRepository.save(card);
                updated++;
            }
        }

        return updated;
    }
}