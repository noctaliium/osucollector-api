package com.osucollector.api.pack;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/packs")
@RequiredArgsConstructor
public class PackController {

    private final PackService packService;

    @PostMapping("/open")
    public ResponseEntity<PackOpeningResult> openPack(
            @PathVariable String userId,
            @RequestParam(required = false, defaultValue = "all") String packType) {
        // packType is here for later on when we will have different types of packs
        return ResponseEntity.ok(packService.openPack(userId));
    }
}