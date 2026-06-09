package com.project.auth_server.security.oauth2;

import com.project.auth_server.service.RefreshTokenService;
import com.project.auth_server.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getEmail();

        String accessToken  = jwtTokenProvider.generateToken(email);
        String refreshToken = refreshTokenService.createRefreshToken(email);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("accessToken",  accessToken)
            .queryParam("refreshToken", refreshToken)
            .build().toUriString();

        log.info("OAuth2 login success: {} → redirecting to frontend", email);

        if (response.isCommitted()) {
            log.debug("Response already committed, cannot redirect");
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}