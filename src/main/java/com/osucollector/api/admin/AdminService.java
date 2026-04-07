package com.osucollector.api.admin;

import com.osucollector.api.card.*;
import com.osucollector.api.user.*;
import com.osucollector.api.usercard.RainbowCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository    adminRepository;
    private final CardRepository     cardRepository;
    private final UserRepository     userRepository;
    private final RainbowCardRepository rainbowCardRepository;

    public AdminStatsDto getGlobalStats() {
        long totalUsers        = userRepository.count();
        long totalCards        = cardRepository.count();
        long totalPacksOpened  = adminRepository.sumTotalPacksOpened();
        long totalRainbowCards = rainbowCardRepository.count();

        List<Object[]> mostOwned  = adminRepository.findMostOwnedCard();
        List<Object[]> rarestOwned = adminRepository.findRarestOwnedCard();

        AdminStatsDto.TopCardDto mostOwnedCard = mostOwned.isEmpty() ? null
                : new AdminStatsDto.TopCardDto(
                        (String) mostOwned.get(0)[0],
                        (long)   mostOwned.get(0)[1]
                );

        AdminStatsDto.TopCardDto rarestOwnedCard = rarestOwned.isEmpty() ? null
                : new AdminStatsDto.TopCardDto(
                        (String) rarestOwned.get(0)[0],
                        (long)   rarestOwned.get(0)[1]
                );

        return new AdminStatsDto(
                totalUsers,
                totalCards,
                totalPacksOpened,
                totalRainbowCards,
                mostOwnedCard,
                rarestOwnedCard
        );
    }

    @Transactional
    public CardDto createCard(CreateCardRequest request) {
        Card card = Card.builder()
                .playerName(request.playerName())
                .gamemode(request.gamemode())
                .rarity(request.rarity())
                .perfPoint(request.perfPoint())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .imageUrl(request.imageUrl())
                .osuUserId(request.osuUserId())
                .build();

        return CardDto.from(cardRepository.save(card));
    }

    @Transactional
    public CardDto updateCard(Short id, CreateCardRequest request) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        if (request.playerName() != null) card.setPlayerName(request.playerName());
        if (request.gamemode()    != null) card.setGamemode(request.gamemode());
        if (request.rarity()      != null) card.setRarity(request.rarity());
        if (request.perfPoint()   != null) card.setPerfPoint(request.perfPoint());
        if (request.isActive()    != null) card.setIsActive(request.isActive());
        if (request.imageUrl()    != null) card.setImageUrl(request.imageUrl());
        if (request.osuUserId()   != null) card.setOsuUserId(request.osuUserId());

        return CardDto.from(cardRepository.save(card));
    }

    @Transactional
    public void toggleCardActive(Short id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        card.setIsActive(!card.getIsActive());
        cardRepository.save(card);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDto::from)
                .toList();
    }

    @Transactional
    public UserDto updateUserRole(String userId, User.Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setRole(role);
        return UserDto.from(userRepository.save(user));
    }
}