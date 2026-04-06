package com.osucollector.api.pack;

import com.osucollector.api.card.Card;
import com.osucollector.api.card.CardRepository;
import com.osucollector.api.card.Variant;
import com.osucollector.api.user.User;
import com.osucollector.api.user.UserNotFoundException;
import com.osucollector.api.user.UserRepository;
import com.osucollector.api.usercard.UserCardRepository;
import com.osucollector.api.usercard.UserCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PackService {

    private final UserRepository     userRepository;
    private final CardRepository     cardRepository;
    private final UserCardRepository userCardRepository;
    private final UserCardService    userCardService;

    private static final int    CARDS_PER_PACK    = 5;
    private static final int    COINS_PER_PACK    = 20;
    private static final Random RANDOM            = new Random();

    private static final Map<Card.Rarity, Double> RARITY_WEIGHTS = Map.of(
            Card.Rarity.common,   59.9,
            Card.Rarity.uncommon, 30.0,
            Card.Rarity.rare,      9.0,
            Card.Rarity.epic,      1.0,
            Card.Rarity.legendary, 0.1
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

            drawnCards.add(new PackOpeningResult.DrawnCard(
                    userCardService.addCardToCollection(userId, card.getId(), variant),
                    isNew
            ));
        }

        // Update user data
        user.setStackedPacks((short) (user.getStackedPacks() - 1));
        user.setTotalPacksOpened(user.getTotalPacksOpened() + 1);
        user.setCoins(user.getCoins() + COINS_PER_PACK);
        userRepository.save(user);

        return new PackOpeningResult(drawnCards, COINS_PER_PACK);
    }

    private Card drawCard() {
        Card.Rarity rarity = drawRarity();
        List<Card> pool    = cardRepository.findByRarityAndIsActiveTrue(rarity);

        if (pool.isEmpty()) {
            pool = cardRepository.findByRarityAndIsActiveTrue(Card.Rarity.common);
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

        return Card.Rarity.common; // fallback
    }

    private Variant drawVariant(int currentIndex, int foilIndex) {
        // 1/4096 for any card in the pack to be rainbow, if it happen to be on the guaranteed foil slot, it will override it

        if (currentIndex == foilIndex) {
            if (RANDOM.nextInt(4096) == 0) return Variant.rainbow;
            return Variant.foil;
        }

        if (RANDOM.nextInt(4096) == 0) {
            return Variant.rainbow;
        }

        return Variant.normal;
    }
}