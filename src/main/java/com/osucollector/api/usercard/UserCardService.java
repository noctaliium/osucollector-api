package com.osucollector.api.usercard;

import com.osucollector.api.card.Card;
import com.osucollector.api.card.CardNotFoundException;
import com.osucollector.api.card.CardRepository;
import com.osucollector.api.card.Mark;
import com.osucollector.api.card.Variant;
import com.osucollector.api.user.User;
import com.osucollector.api.user.UserRepository;
import com.osucollector.api.user.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCardService {

    private final UserCardRepository userCardRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    public List<UserCardDto> getUserCollection(String userId) {
        return userCardRepository.findByUserId(userId)
                .stream()
                .map(UserCardDto::from)
                .toList();
    }

    public List<UserCardDto> getUserCollectionByRarity(String userId, Card.Rarity rarity) {
        return userCardRepository.findByUserIdAndCardRarity(userId, rarity)
                .stream()
                .map(UserCardDto::from)
                .toList();
    }

    public List<UserCardDto> getUserCollectionByGamemode(String userId, Card.Gamemode gamemode) {
        return userCardRepository.findByUserIdAndCardGamemode(userId, gamemode)
                .stream()
                .map(UserCardDto::from)
                .toList();
    }

    public List<UserCardDto> getUserCollectionByMark(String userId, Mark mark) {
        return userCardRepository.findByUserIdAndMark(userId, mark)
                .stream()
                .map(UserCardDto::from)
                .toList();
    }

    // Cards

    @Transactional
    public UserCardDto addCardToCollection(String userId, Short cardId, Variant variant) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new com.osucollector.api.card.CardNotFoundException(cardId));

        UserCard userCard = userCardRepository
                .findByUserIdAndCardId(userId, cardId)
                .orElseGet(() -> UserCard.builder()
                        .id(new UserCardId(userId, cardId))
                        .user(user)
                        .card(card)
                        .build());

        switch (variant) {
            case normal  -> userCard.setQuantityNormal((short) (userCard.getQuantityNormal() + 1));
            case foil    -> userCard.setQuantityFoil((short) (userCard.getQuantityFoil() + 1));
            case rainbow -> throw new IllegalArgumentException(
                "Rainbow cards must be handled separately via PackService"
            );
        }

        return UserCardDto.from(userCardRepository.save(userCard));
    }

    // Marks

    @Transactional
    public UserCardDto updateMark(String userId, Short cardId, Mark mark) {
        UserCard userCard = userCardRepository
                .findByUserIdAndCardId(userId, cardId)
                .orElseThrow(() -> new UserCardNotFoundException(userId, cardId));

        userCard.setMark(mark);
        return UserCardDto.from(userCardRepository.save(userCard));
    }

    @Transactional
    public UserCardDto removeMark(String userId, Short cardId) {
        UserCard userCard = userCardRepository
                .findByUserIdAndCardId(userId, cardId)
                .orElseThrow(() -> new UserCardNotFoundException(userId, cardId));

        userCard.setMark(null);
        return UserCardDto.from(userCardRepository.save(userCard));
    }

    public UserCardDto getOrCreateUserCard(String userId, Short cardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        UserCard userCard = userCardRepository
                .findByUserIdAndCardId(userId, cardId)
                .orElseGet(() -> UserCard.builder()
                        .id(new UserCardId(userId, cardId))
                        .user(user)
                        .card(card)
                        .build());

        return UserCardDto.from(userCardRepository.save(userCard));
    }

    public long getUniqueCardCount(String userId) {
        return userCardRepository.countByUserId(userId);
    }
}