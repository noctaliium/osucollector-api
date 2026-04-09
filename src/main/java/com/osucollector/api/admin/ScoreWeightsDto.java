package com.osucollector.api.admin;

public record ScoreWeightsDto(
        double ppDivisor,
        int    ppMax,
        double followersDivisor,
        int    followersMax,
        double mappingFollowersDivisor,
        int    mappingFollowersMax,
        int    badgePts,
        int    badgesMax,
        int    titlePts,
        double mapsMultiplier,
        int    mapsMax,
        double firstPlacesMultiplier,
        int    firstPlacesMax
) {}