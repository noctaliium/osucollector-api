package com.osucollector.api.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping("/osu/{osuUserId}")
    public ResponseEntity<UserDto> getUserByOsuUserId(@PathVariable Integer osuUserId) {
        return ResponseEntity.ok(userService.getUserByOsuUserId(osuUserId));
    }

    @GetMapping("/{id}/packs")
    public ResponseEntity<Short> getAvailablePacks(@PathVariable String id) {
        return ResponseEntity.ok(userService.calculateAvailablePacks(id));
    }

    @GetMapping("/{id}/can-trade")
    public ResponseEntity<Boolean> canTrade(@PathVariable String id) {
        return ResponseEntity.ok(userService.canTrade(id));
    }
}