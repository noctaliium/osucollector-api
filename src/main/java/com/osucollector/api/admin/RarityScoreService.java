package com.osucollector.api.admin;

import com.osucollector.api.card.Card;
import com.osucollector.api.card.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RarityScoreService {

    private final CardRepository cardRepository;

    // Constants for score calc
    private static final double PP_DIVISOR              = 2.0;
    private static final int    PP_MAX                  = 15000;
    private static final double FOLLOWERS_DIVISOR       = 6.0;
    private static final int    FOLLOWERS_MAX           = 2000;
    private static final double MAPPING_FOL_DIVISOR     = 2.0;
    private static final int    MAPPING_FOL_MAX         = 1000;
    private static final int    BADGE_PTS               = 100;
    private static final int    BADGES_MAX              = 500;
    private static final int    TITLE_PTS               = 800;
    private static final double MAPS_MULTIPLIER         = 5.0;
    private static final int    MAPS_MAX                = 2000;
    private static final double FIRST_PLACES_MULTIPLIER = 2.0;
    private static final int    FIRST_PLACES_MAX        = 1500;

    // Rarity ratios
    private static final double MYTHIC_RATIO   = 0.0025;
    private static final double LEGENDARY_RATIO = 0.01;
    private static final double EPIC_RATIO     = 0.05;
    private static final double RARE_RATIO     = 0.15;
    private static final double UNCOMMON_RATIO = 0.30;    

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

        int mythicCount    = Math.max(1, (int) Math.round(total * MYTHIC_RATIO));
        int legendaryCount = Math.max(1, (int) Math.round(total * LEGENDARY_RATIO));
        int epicCount      = Math.max(1, (int) Math.round(total * EPIC_RATIO));
        int rareCount      = Math.max(1, (int) Math.round(total * RARE_RATIO));
        int uncommonCount  = Math.max(1, (int) Math.round(total * UNCOMMON_RATIO));

        int mythicIdx    = mythicCount - 1;
        int legendaryIdx = mythicCount + legendaryCount - 1;
        int epicIdx      = mythicCount + legendaryCount + epicCount - 1;
        int rareIdx      = mythicCount + legendaryCount + epicCount + rareCount - 1;
        int uncommonIdx  = mythicCount + legendaryCount + epicCount + rareCount + uncommonCount - 1;

        int mythicMin    = sorted.get(Math.min(mythicIdx,    total - 1));
        int legendaryMin = sorted.get(Math.min(legendaryIdx, total - 1));
        int epicMin      = sorted.get(Math.min(epicIdx,      total - 1));
        int rareMin      = sorted.get(Math.min(rareIdx,      total - 1));
        int uncommonMin  = sorted.get(Math.min(uncommonIdx,  total - 1));

        if (score >= mythicMin)    return Card.Rarity.mythic;
        if (score >= legendaryMin) return Card.Rarity.legendary;
        if (score >= epicMin)      return Card.Rarity.epic;
        if (score >= rareMin)      return Card.Rarity.rare;
        if (score >= uncommonMin)  return Card.Rarity.uncommon;
        return Card.Rarity.common;
    }

    public int applyAllSuggestions() {
        List<Card> allCards = cardRepository.findAll();

        List<Integer> allScores = allCards.stream()
                .filter(c -> c.getRarity() != Card.Rarity.special)
                .map(this::calculateScore)
                .toList();

        List<Card> eligibleCards = allCards.stream()
                .filter(c -> c.getRarity() != Card.Rarity.special)
                .sorted((a, b) -> Integer.compare(calculateScore(b), calculateScore(a)))
                .toList();

        int total         = eligibleCards.size();
        int mythicCount   = Math.max(1, (int) Math.round(total * MYTHIC_RATIO));
        int legendaryCount = Math.max(1, (int) Math.round(total * LEGENDARY_RATIO));
        int epicCount     = Math.max(1, (int) Math.round(total * EPIC_RATIO));
        int rareCount     = Math.max(1, (int) Math.round(total * RARE_RATIO));
        int uncommonCount = Math.max(1, (int) Math.round(total * UNCOMMON_RATIO));

        int updated = 0;

        for (int i = 0; i < eligibleCards.size(); i++) {
            Card card = eligibleCards.get(i);
            Card.Rarity newRarity;

            if      (i < mythicCount)                                                          newRarity = Card.Rarity.mythic;
            else if (i < mythicCount + legendaryCount)                                         newRarity = Card.Rarity.legendary;
            else if (i < mythicCount + legendaryCount + epicCount)                             newRarity = Card.Rarity.epic;
            else if (i < mythicCount + legendaryCount + epicCount + rareCount)                 newRarity = Card.Rarity.rare;
            else if (i < mythicCount + legendaryCount + epicCount + rareCount + uncommonCount) newRarity = Card.Rarity.uncommon;
            else                                                                               newRarity = Card.Rarity.common;

            if (card.getRarity() != newRarity) {
                card.setRarity(newRarity);
                cardRepository.save(card);
                updated++;
            }
        }

        return updated;
    }
}