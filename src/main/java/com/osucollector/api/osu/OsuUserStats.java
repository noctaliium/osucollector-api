package com.osucollector.api.osu;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record OsuUserStats(
        Integer    osuUserId,
        String     username,
        String     title,
        String     countryCode,
        String     avatarUrl,
        BigDecimal pp,
        Integer    globalRank,
        Integer    countryRank,
        BigDecimal accuracy,
        Boolean    isRanked,
        Integer    followerCount,
        Integer    mappingFollowerCount,
        Integer    badgeCount,
        Integer    rankedMapCount,
        Integer    lovedMapCount,
        Integer    firstPlaceCount
) {
    @SuppressWarnings("unchecked")
    public static OsuUserStats fromApiResponse(Map<String, Object> response) {
        Integer osuUserId = ((Number) response.get("id")).intValue();
        String username   = (String) response.get("username");
        String title      = (String) response.get("title");
        String countryCode = (String) response.get("country_code");
        String avatarUrl  = (String) response.get("avatar_url");

        List<?> badges   = (List<?>) response.getOrDefault("badges", List.of());
        int badgeCount   = badges.size();

        Integer followerCount        = toInt(response.get("follower_count"));
        Integer mappingFollowerCount = toInt(response.get("mapping_follower_count"));

        Integer rankedMapCount = toInt(response.get("ranked_and_approved_beatmapset_count"));
        Integer lovedMapCount  = toInt(response.get("loved_beatmapset_count"));

        Map<String, Object> statistics = (Map<String, Object>) response.get("statistics");

        if (statistics == null) {
            return new OsuUserStats(
                osuUserId, username, title, countryCode, avatarUrl,
                null, null, null, null, false,
                followerCount, mappingFollowerCount, badgeCount,
                rankedMapCount, lovedMapCount, null
            );
        }

        BigDecimal pp       = toBigDecimal(statistics.get("pp"));
        Integer globalRank  = toInt(statistics.get("global_rank"));
        Integer countryRank = toInt(statistics.get("country_rank"));
        BigDecimal accuracy = toBigDecimal(statistics.get("hit_accuracy"));
        Boolean isRanked    = (Boolean) statistics.getOrDefault("is_ranked", false);
        Integer firstPlaceCount = toInt(response.get("scores_first_count"));

        return new OsuUserStats(
                osuUserId, username, title, countryCode, avatarUrl,
                pp, globalRank, countryRank, accuracy, isRanked,
                followerCount, mappingFollowerCount, badgeCount,
                rankedMapCount, lovedMapCount, firstPlaceCount
        );
    }

    private static Integer toInt(Object value) {
        return value != null ? ((Number) value).intValue() : null;
    }

    private static BigDecimal toBigDecimal(Object value) {
        return value != null ? BigDecimal.valueOf(((Number) value).doubleValue()) : null;
    }
}