package com.osucollector.api.usercard;

import com.osucollector.api.card.Card;
import com.osucollector.api.card.Mark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserCardRepository extends JpaRepository<UserCard, UserCardId> {
    List<UserCard> findByUserId(String userId);

    List<UserCard> findByUserIdAndCardRarity(String userId, Card.Rarity rarity);
    List<UserCard> findByUserIdAndCardGamemode(String userId, Card.Gamemode gamemode);
    List<UserCard> findByUserIdAndMark(String userId, Mark mark);

    Optional<UserCard> findByUserIdAndCardId(String userId, Short cardId);

    boolean existsByUserIdAndCardId(String userId, Short cardId);

    long countByUserId(String userId);

    @Query("SELECT COUNT(DISTINCT uc.user.id) FROM UserCard uc WHERE uc.card.id = :cardId")
    long countPlayersOwningCard(@Param("cardId") Short cardId);
}