package com.osucollector.api.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return UserDto.from(user);
    }

    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        return UserDto.from(user);
    }

    public UserDto getUserByOsuUserId(Integer osuUserId) {
        User user = userRepository.findByOsuUserId(osuUserId)
                .orElseThrow(() -> new UserNotFoundException(osuUserId));
        return UserDto.from(user);
    }

    // OAuth osu!

    public UserDto loginOrRegister(Integer osuUserId, String username, String refreshToken) {
        User user = userRepository.findByOsuUserId(osuUserId)
                .map(existing -> updateExistingUser(existing, username, refreshToken))
                .orElseGet(() -> createNewUser(osuUserId, username, refreshToken));

        return UserDto.from(userRepository.save(user));
    }

    private User updateExistingUser(User user, String username, String refreshToken) {
        user.setUsername(username);
        user.setOsuRefreshToken(refreshToken);
        return user;
    }

    private User createNewUser(Integer osuUserId, String username, String refreshToken) {
        return User.builder()
                .osuUserId(osuUserId)
                .username(username)
                .osuRefreshToken(refreshToken)
                .stackedPacks((short) 10)
                .build();
    }

    // Packs

    public short calculateAvailablePacks(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        long minutesElapsed = java.time.Duration.between(
                user.getLastPackTickAt(),
                LocalDateTime.now()
        ).toMinutes();

        int earnedPacks = (int) Math.min(
                minutesElapsed / 10,
                15 - user.getStackedPacks()
        );

        if (earnedPacks > 0) {
            user.setStackedPacks((short) (user.getStackedPacks() + earnedPacks));
            user.setLastPackTickAt(
                user.getLastPackTickAt().plusMinutes((long) earnedPacks * 10)
            );
            userRepository.save(user);
        }

        return user.getStackedPacks();
    }

    // Trades 

    public boolean canTrade(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        boolean hasOpenedEnoughPacks = user.getTotalPacksOpened() >= 15;
        boolean cooldownExpired = user.getLastTradeAt() == null
                || user.getLastTradeAt().isBefore(LocalDateTime.now().minusHours(1));

        return hasOpenedEnoughPacks && cooldownExpired;
    }

    public User loginOrRegisterEntity(Integer osuUserId, String username, String refreshToken) {
        User user = userRepository.findByOsuUserId(osuUserId)
                .map(existing -> updateExistingUser(existing, username, refreshToken))
                .orElseGet(() -> createNewUser(osuUserId, username, refreshToken));

        return userRepository.save(user);
    }
}