package com.osucollector.api.auth;

import com.osucollector.api.user.User;
import com.osucollector.api.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OsuAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService  jwtService;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest  request,
            HttpServletResponse response,
            Authentication      authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Integer osuUserId = oAuth2User.getAttribute("id");
        String  username  = oAuth2User.getAttribute("username");

        User user = userService.loginOrRegisterEntity(osuUserId, username, null);
        String jwt = jwtService.generateToken(user);

        response.sendRedirect(frontendUrl + "/auth/success?token=" + jwt);
    }
}