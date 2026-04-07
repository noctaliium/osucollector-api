package com.osucollector.api.usercard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RainbowCardRepository extends JpaRepository<RainbowCard, String> {

    List<RainbowCard> findByOwnerId(String ownerId);

    List<RainbowCard> findByOwnerIdAndCardId(String ownerId, Short cardId);

    // Get the next serial number for a given card ID
    @Query("SELECT COALESCE(MAX(rc.serialNumber), 0) + 1 FROM RainbowCard rc WHERE rc.card.id = :cardId")
    int getNextSerialNumber(@Param("cardId") Short cardId);
}