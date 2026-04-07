package com.osucollector.api.admin;

import com.osucollector.api.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface AdminRepository extends JpaRepository<User, String> {

    @Query("SELECT COALESCE(SUM(u.totalPacksOpened), 0) FROM User u")
    long sumTotalPacksOpened();

    @Query("""
            SELECT uc.card.playerName, COUNT(DISTINCT uc.user.id)
            FROM UserCard uc
            GROUP BY uc.card.id, uc.card.playerName
            ORDER BY COUNT(DISTINCT uc.user.id) DESC
            LIMIT 1
            """)
    List<Object[]> findMostOwnedCard();

    @Query("""
            SELECT uc.card.playerName, COUNT(DISTINCT uc.user.id)
            FROM UserCard uc
            WHERE uc.card.rarity NOT IN ('common', 'uncommon')
            GROUP BY uc.card.id, uc.card.playerName
            ORDER BY COUNT(DISTINCT uc.user.id) ASC
            LIMIT 1
            """)
    List<Object[]> findRarestOwnedCard();
}