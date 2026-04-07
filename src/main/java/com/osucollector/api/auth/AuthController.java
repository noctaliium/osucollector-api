package com.osucollector.api.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OsuOAuthService osuOAuthService;

    // Appelé automatiquement par Spring après le callback osu!
    @GetMapping("/callback")
    public ResponseEntity<AuthResponse> callback(OAuth2AuthenticationToken authToken) {
        String jwt = osuOAuthService.handleOAuthCallback(authToken);
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    // Endpoint pour initier la connexion depuis Vue
    @GetMapping("/login")
    public ResponseEntity<Void> login() {
        // Spring Security redirige automatiquement vers osu! OAuth
        return ResponseEntity.status(302).build();
    }
}