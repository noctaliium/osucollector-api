package com.osucollector.api.usercard;

import com.osucollector.api.card.Card;
import com.osucollector.api.card.Mark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    @Query("""
    SELECT uc FROM UserCard uc
    JOIN uc.card c
    WHERE uc.user.id = :userId
    AND (:countryCode IS NULL OR c.countryCode = :countryCode)
    AND (:favorite IS NULL OR uc.favorite = :favorite)
    ORDER BY
        CASE c.rarity
            WHEN 'mythic'    THEN 1
            WHEN 'legendary' THEN 2
            WHEN 'epic'      THEN 3
            WHEN 'rare'      THEN 4
            WHEN 'uncommon'  THEN 5
            WHEN 'common'    THEN 6
            WHEN 'special'   THEN 7
            ELSE 8
        END
    """)
    Page<UserCard> findUserCollection(
        @Param("userId")      String userId,
        @Param("countryCode") String countryCode,
        @Param("favorite")    Boolean favorite,
        Pageable pageable
    );

    @Query("SELECT DISTINCT c.countryCode FROM UserCard uc JOIN uc.card c WHERE uc.user.id = :userId AND c.countryCode IS NOT NULL ORDER BY c.countryCode")
    List<String> findDistinctCountryCodes(@Param("userId") String userId);

}