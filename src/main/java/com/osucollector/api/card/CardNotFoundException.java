package com.osucollector.api.card;

public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(Short id) {
        super("Card not found with id: " + id);
    }

    public CardNotFoundException(String playerName) {
        super("Card not found for player: " + playerName);
    }
}