package com.osucollector.api.pack;

import com.osucollector.api.card.Card;
import com.osucollector.api.card.CardRepository;
import com.osucollector.api.card.Variant;
import com.osucollector.api.user.User;
import com.osucollector.api.user.UserNotFoundException;
import com.osucollector.api.user.UserRepository;
import com.osucollector.api.usercard.RainbowCard;
import com.osucollector.api.usercard.RainbowCardRepository;
import com.osucollector.api.usercard.UserCardDto;
import com.osucollector.api.usercard.UserCardRepository;
import com.osucollector.api.usercard.UserCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PackService {

    private final UserRepository     userRepository;
    private final CardRepository     cardRepository;
    private final UserCardRepository userCardRepository;
    private final RainbowCardRepository rainbowCardRepository;
    private final UserCardService    userCardService;

    private static final int    CARDS_PER_PACK    = 5;
    private static final int    COINS_PER_PACK    = 20;
    private static final Random RANDOM            = new Random();

    private Card.Rarity forcedNextRarity = null;

    private static final Map<Card.Rarity, Double> RARITY_WEIGHTS;

    static {
        RARITY_WEIGHTS = new java.util.LinkedHashMap<>();
        RARITY_WEIGHTS.put(Card.Rarity.mythic,    0.1);
        RARITY_WEIGHTS.put(Card.Rarity.legendary, 0.5);
        RARITY_WEIGHTS.put(Card.Rarity.epic,      2.0);
        RARITY_WEIGHTS.put(Card.Rarity.rare,     10.0);
        RARITY_WEIGHTS.put(Card.Rarity.uncommon, 40.0);
        RARITY_WEIGHTS.put(Card.Rarity.common,   47.4);
    }

    private static final List<Card.Rarity> RARITY_ORDER = List.of(
        Card.Rarity.common,
        Card.Rarity.uncommon,
        Card.Rarity.rare,
        Card.Rarity.epic,
        Card.Rarity.legendary,
        Card.Rarity.special
    );  

    @Transactional
    public PackOpeningResult openPack(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getStackedPacks() <= 0) {
            throw new NoPackAvailableException();
        }

        // Draw the foiled card randomly
        int foilIndex = RANDOM.nextInt(CARDS_PER_PACK);

        List<PackOpeningResult.DrawnCard> drawnCards = new ArrayList<>();

        for (int i = 0; i < CARDS_PER_PACK; i++) {
            Card card    = drawCard();
            Variant variant = drawVariant(i, foilIndex);
            boolean isNew   = !userCardRepository.existsByUserIdAndCardId(userId, card.getId());

            UserCardDto userCardDto;

            if (variant == Variant.rainbow) {
                    int serial = rainbowCardRepository.getNextSerialNumber(card.getId());
                    rainbowCardRepository.save(RainbowCard.builder()
                            .card(card)
                            .owner(user)
                            .serialNumber(serial)
                            .build());
                    userCardDto = userCardService.getOrCreateUserCard(userId, card.getId());
                    drawnCards.add(new PackOpeningResult.DrawnCard(userCardDto, isNew, variant, serial));
                } else {
                    userCardDto = userCardService.addCardToCollection(userId, card.getId(), variant);
                    drawnCards.add(new PackOpeningResult.DrawnCard(userCardDto, isNew, variant, null));
                }
        }

        drawnCards.sort(Comparator.comparingInt(
            drawn -> RARITY_ORDER.indexOf(drawn.userCard().card().rarity())
        ));

        updatePityCounters(user, drawnCards);

        // Update user data
        user.setStackedPacks((short) (user.getStackedPacks() - 1));
        user.setTotalPacksOpened(user.getTotalPacksOpened() + 1);
        user.setCoins(user.getCoins() + COINS_PER_PACK);
        userRepository.save(user);

        return new PackOpeningResult(drawnCards, COINS_PER_PACK);
    }

    private Card drawCard() {
        Card.Rarity rarity;

        if (forcedNextRarity != null) {
            rarity = forcedNextRarity;
            forcedNextRarity = null;  // reset après usage
        } else {
            rarity = drawRarity();
        }

        List<Card> pool = cardRepository.findByRarityAndIsActiveTrue(rarity);

        if (pool.isEmpty()) {
            pool = cardRepository.findByRarityAndIsActiveTrue(Card.Rarity.common);
        }

        if (pool.isEmpty()) {
            pool = cardRepository.findAll().stream()
                    .filter(Card::getIsActive)
                    .toList();
        }

        if (pool.isEmpty()) {
            throw new NoPackAvailableException();
        }

        return pool.get(RANDOM.nextInt(pool.size()));
    }

    private Card.Rarity drawRarity() {
        double roll  = RANDOM.nextDouble() * 100;
        double cumul = 0;

        for (Map.Entry<Card.Rarity, Double> entry : RARITY_WEIGHTS.entrySet()) {
            cumul += entry.getValue();
            if (roll < cumul) return entry.getKey();
        }

        return Card.Rarity.common;
    }

    private Variant drawVariant(int currentIndex, int foilIndex) {
        // 1/4096 for any card in the pack to be rainbow, if it happen to be on the guaranteed foil slot, it will override it

        if (currentIndex == foilIndex) {
            // Rainbow chance is doubled on the guaranteed foil slot
            if (RANDOM.nextInt(2048) == 0) return Variant.rainbow;
            return Variant.foil;
        }

        if (RANDOM.nextInt(4096) == 0) {
            return Variant.rainbow;
        }

        return Variant.normal;
    }

    private void updatePityCounters(User user, List<PackOpeningResult.DrawnCard> drawnCards) {
        boolean gotEpic = drawnCards.stream()
                .anyMatch(d -> d.userCard().card().rarity() == Card.Rarity.epic
                        || d.userCard().card().rarity() == Card.Rarity.legendary);

        boolean gotLegendary = drawnCards.stream()
                .anyMatch(d -> d.userCard().card().rarity() == Card.Rarity.legendary);

        user.setPacksWithoutEpic(gotEpic ? 0 : user.getPacksWithoutEpic() + 1);
        user.setPacksWithoutLegendary(gotLegendary ? 0 : user.getPacksWithoutLegendary() + 1);
    }

    public void setForcedNextRarity(Card.Rarity rarity) {
        this.forcedNextRarity = rarity;
    }
}