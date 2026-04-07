package com.osucollector.api.auth;

import com.osucollector.api.user.User;
import com.osucollector.api.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OsuOAuthService {

    private final UserService                userService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final JwtService                 jwtService;

    public String handleOAuthCallback(OAuth2AuthenticationToken authToken) {
        OAuth2User oauthUser = authToken.getPrincipal();

        Integer osuUserId = oauthUser.getAttribute("id");
        String  username  = oauthUser.getAttribute("username");

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authToken.getAuthorizedClientRegistrationId(),
                authToken.getName()
        );

        String refreshToken = client.getRefreshToken() != null
                ? client.getRefreshToken().getTokenValue()
                : null;

        User user = userService.loginOrRegisterEntity(osuUserId, username, refreshToken);

        // Génère et retourne le JWT
        return jwtService.generateToken(user);
    }
}