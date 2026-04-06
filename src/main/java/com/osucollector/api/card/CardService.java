package com.osucollector.api.card;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    public List<CardDto> getAllCards() {
        return cardRepository.findAll()
                .stream()
                .map(CardDto::from)
                .toList();
    }

    public CardDto getCardById(Short id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        return CardDto.from(card);
    }

    public CardDto getCardByPlayerName(String playerName) {
        Card card = cardRepository.findByPlayerName(playerName);
        if (card == null) throw new CardNotFoundException(playerName);
        return CardDto.from(card);
    }

    public List<CardDto> getCardsByGamemode(Card.Gamemode gamemode) {
        return cardRepository.findByGamemode(gamemode)
                .stream()
                .map(CardDto::from)
                .toList();
    }

    public List<CardDto> getCardsByRarity(Card.Rarity rarity) {
        return cardRepository.findByRarity(rarity)
                .stream()
                .map(CardDto::from)
                .toList();
    }

    public List<CardDto> getActiveCards() {
        return cardRepository.findByIsActiveTrue()
                .stream()
                .map(CardDto::from)
                .toList();
    }
}