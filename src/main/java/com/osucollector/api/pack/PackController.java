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
    public ResponseEntity<PackOpeningResult> openPack(@PathVariable String userId) {
        return ResponseEntity.ok(packService.openPack(userId));
    }
}