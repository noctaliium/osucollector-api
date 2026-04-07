package com.osucollector.api.admin;

import com.osucollector.api.card.CardDto;
import com.osucollector.api.user.User;
import com.osucollector.api.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getGlobalStats() {
        return ResponseEntity.ok(adminService.getGlobalStats());
    }

    @PostMapping("/cards")
    public ResponseEntity<CardDto> createCard(@RequestBody CreateCardRequest request) {
        return ResponseEntity.status(201).body(adminService.createCard(request));
    }

    @PatchMapping("/cards/{id}")
    public ResponseEntity<CardDto> updateCard(
            @PathVariable Short id,
            @RequestBody CreateCardRequest request) {
        return ResponseEntity.ok(adminService.updateCard(id, request));
    }

    @PatchMapping("/cards/{id}/toggle")
    public ResponseEntity<Void> toggleCardActive(@PathVariable Short id) {
        adminService.toggleCardActive(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserDto> updateUserRole(
            @PathVariable String id,
            @RequestParam User.Role role) {
        return ResponseEntity.ok(adminService.updateUserRole(id, role));
    }
}